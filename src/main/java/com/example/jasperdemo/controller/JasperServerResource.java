package com.example.jasperdemo.controller;


import com.example.jasperdemo.config.ApplicationProperties;
import com.example.jasperdemo.domain.DataSource;
import com.example.jasperdemo.domain.JasperReport;
import com.example.jasperdemo.repository.DataSourceRepository;
import com.example.jasperdemo.repository.JasperReportRepository;
import com.example.jasperdemo.service.DataSourceDto;
import com.example.jasperdemo.service.ExportReport;
import com.example.jasperdemo.service.ReportDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/jasper-server")
public class JasperServerResource {
    private final Logger log = LoggerFactory.getLogger(JasperServerResource.class);
    private static final String CONTENT_TYPE = "ContentType";
    private static final String APPLICATION_REPOSITORY_REPORT_UNIT_JSON = "application/repository.reportUnit+json";
    private final ApplicationProperties applicationProperties;
    private final OkHttpClient client;

    private final JasperReportRepository jasperReportRepository;

    private final DataSourceRepository dataSourceRepository;

    public JasperServerResource(ApplicationProperties applicationProperties, OkHttpClient client, JasperReportRepository jasperReportRepository, DataSourceRepository dataSourceRepository) {
        this.applicationProperties = applicationProperties;
        this.client = client;
        this.jasperReportRepository = jasperReportRepository;
        this.dataSourceRepository = dataSourceRepository;
    }


    @GetMapping("/get-available-resources")
    public ResponseEntity<String> getAvailableResources() {
        log.info("REST request to get available resources");
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() + "/resources")
                .method("GET", null)
                .addHeader(CONTENT_TYPE, APPLICATION_REPOSITORY_REPORT_UNIT_JSON)
                .build();
        Response response;
        String body;
        try {
            response = client.newCall(request).execute();
            body = Objects.requireNonNull(response.peekBody(Long.MAX_VALUE)).string();
            log.error(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/upload-report")
    public ResponseEntity<JasperReport> uploadReport(@RequestBody ReportDto dto) {
        log.info("REST request to upload a report with name {} ", dto.label());
        DataSource dataSource = dataSourceRepository.findById(dto.dataSourceId())
                .orElseThrow(() -> new RuntimeException("Datasource not found with this id"));
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse(APPLICATION_REPOSITORY_REPORT_UNIT_JSON);
        okhttp3.RequestBody body = okhttp3.RequestBody
                .create(mediaType, "{%n    \"label\" : \"%s\",%n    \"jrxml\": {%n        \"jrxmlFile\": {%n            \"label\": \"%s\",%n            \"type\":\"%s\",%n            \"content\": \"%s\"%n        }%n    },%n    \"dataSource\": {%n        \"dataSourceReference\": {%n            \"uri\": \"%s\"%n        }%n    }%n%n}"
                        .formatted(dto.label(), dto.label(), dto.type(), dto.data(), dataSource.getUri()));
        Request request = new Request.Builder()
                .url("%s/resources/reports/interactive?j_username=%s&j_password=%s".formatted(applicationProperties.jasperServerUrl(), applicationProperties.username(), applicationProperties.password()))
                .method("POST", body)
                .addHeader(CONTENT_TYPE, APPLICATION_REPOSITORY_REPORT_UNIT_JSON)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 201) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                log.info(responseBody);
                Pattern pattern = Pattern.compile("<uri>(.*?)</uri>");
                Matcher matcher = pattern.matcher(responseBody);
                if (matcher.find()) {
                    String uri = matcher.group(1);
                    JasperReport jasperReport = new JasperReport();
                    jasperReport.setName(dto.label());
                    jasperReport.setReportUnitUri(uri);
                    jasperReport.setDataSource(dataSource);
                    jasperReport.setData(dto.data().getBytes());
                    jasperReport.setDataContentType(dto.type());
                    JasperReport saved = jasperReportRepository.save(jasperReport);
                    return ResponseEntity.ok().body(saved);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Exception occured");
    }

    @PostMapping("/export-report")
    public ResponseEntity<byte[]> exportReport(@RequestBody ExportReport dto) {
        JasperReport report = jasperReportRepository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Report not found"));
        log.info("REST request to execute a report with id {} name {}", report.getId(), report.getName());

        okhttp3.RequestBody body = buildRequestBodyForReportExecRequest(dto, report);

        try (Response response = requestReportExecution(body)) {
            String respBody = Objects.requireNonNull(response.body()).string();
            List<String> cookieList = response.headers().values("Set-Cookie");
            String jsessionId = cookieList.get(0).split(";")[0];

            if (response.code() == 200) {
                JSONObject jsonObject = new JSONObject(respBody);
                String requestId = jsonObject.getString("requestId");
                JSONArray exportsArray = jsonObject.getJSONArray("exports");
                JSONObject exportsObject = exportsArray.getJSONObject(0);
                String exportId = exportsObject.getString("id");
                while (true) {
                    try (Response responseReportStatus = requestReportStatus(requestId, jsessionId)) {
                        String respReportStatus = Objects.requireNonNull(responseReportStatus.body()).string();

                        if (responseReportStatus.code() == 200) {
                            JSONObject jsonObject1 = new JSONObject(respReportStatus);
                            String value = jsonObject1.getString("value");

                            if (value.equals("ready")) {
                                try (Response response2 = requestReportOutput(requestId, exportId, jsessionId)) {
                                    byte[] content = Objects.requireNonNull(response2.body()).bytes();
                                    return ResponseEntity.ok()
                                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + report.getName() + "_" + getExportTimestamp() + "." + dto.format())
                                            .body(content);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Some error occurred");
    }

    private static okhttp3.RequestBody buildRequestBodyForReportExecRequest(ExportReport dto, JasperReport report) {
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/xml");
        return okhttp3.RequestBody.create(mediaType, """
                        <reportExecutionRequest>
                            <reportUnitUri>%s</reportUnitUri>
                            <async>true</async>
                            <freshData>false</freshData>
                            <saveDataSnapshot>false</saveDataSnapshot>
                            <outputFormat>%s</outputFormat>
                            <interactive>true</interactive>
                        </reportExecutionRequest>
                        """.formatted(report.getReportUnitUri(), dto.format()));
    }

    private static String getExportTimestamp() {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        return dateFormatter.format(new Date());
    }

    private Response requestReportOutput(String requestId, String exportId, String jsessionId) throws IOException {
        log.info("request to JasperServer for report output for request {} and export {}", requestId, exportId);
        Request request2 = new Request.Builder()
                .url("%s/reportExecutions/%s/exports/%s/outputResource?j_username=%s&j_password=%s".formatted(applicationProperties.jasperServerUrl(), requestId, exportId, applicationProperties.username(), applicationProperties.password()))
                .method("GET", null)
                .addHeader("Cookie", "userLocale=en_US; " + jsessionId)
                .build();
        return client.newCall(request2).execute();
    }

    private Response requestReportStatus(String requestId, String jsessionId) throws IOException {
        log.info("request to JasperServer to check status for request {}", requestId);
        Request requestReportStatus = new Request.Builder()
                .url("%s/reportExecutions/%s/status/?j_username=%s&j_password=%s".formatted(applicationProperties.jasperServerUrl(), requestId, applicationProperties.username(), applicationProperties.password()))
                .method("GET", null)
                .addHeader("Cookie", "userLocale=en_US;" + jsessionId)
                .build();
        return client.newCall(requestReportStatus).execute();
    }

    private Response requestReportExecution(okhttp3.RequestBody body) throws IOException {
        log.info("request to JasperServer to get execute a report");
        Request request = new Request.Builder()
                .url("%s/reportExecutions?j_username=%s&j_password=%s".formatted(applicationProperties.jasperServerUrl(), applicationProperties.username(), applicationProperties.password()))
                .method("POST", body)
                .addHeader(CONTENT_TYPE, "application/xml")
                .build();

        return client.newCall(request).execute();
    }


    @PostMapping("/add-new-data-source")
    public ResponseEntity<DataSource> addNewDataSource(@RequestBody DataSourceDto dto) {
        log.debug("Request to add new data source with label {} ", dto.label());
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/repository.jdbcDataSource+json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "%n{%n    \"label\":\"%s\",%n\"driverClass\":\"%s\",%n\"password\":\"%s\",%n\"username\":\"%s\",%n\"connectionUrl\":\"%s\"%n%n}%n"
                .formatted(dto.label(), dto.driverClass(), dto.password(), dto.username(), dto.connectionUrl()));
        Request request = new Request.Builder()
                .url("%s/resources/datasources?j_username=%s&j_password=%s".formatted(applicationProperties.jasperServerUrl(), applicationProperties.username(), applicationProperties.password()))
                .method("POST", body)
                .addHeader(CONTENT_TYPE, "application/repository.jdbcDataSource+json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 201) {
                Pattern pattern = Pattern.compile("<uri>(.*)</uri>");
                Matcher matcher = pattern.matcher(Objects.requireNonNull(response.body()).string());
                if (matcher.find()) {
                    // Extract the JSESSIONID value
                    String uri = matcher.group(1);
                    DataSource dataSource = new DataSource();
                    dataSource.setDriverClass(dto.driverClass());
                    dataSource.setLabel(dto.label());
                    dataSource.setUsername(dto.username());
                    dataSource.setPassword(dto.password());
                    dataSource.setConnectionUrl(dto.connectionUrl());
                    dataSource.setUri(uri);
                    DataSource saved = dataSourceRepository.save(dataSource);
                    return ResponseEntity.ok().body(saved);
                }
                if (response.code() == 401) {
                    throw new RuntimeException("Unauthorized on jasper server");
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Exception occured");
    }

    @GetMapping("/get-datasources")
    public ResponseEntity<List<DataSource>> getDatasources(Pageable pageable) {
        Page<DataSource> page = dataSourceRepository.findAll(pageable);
        return ResponseEntity.ok().body(page.getContent());
    }

    @GetMapping("/get-uploaded reports")
    public ResponseEntity<List<JasperReport>> getUploadedReports(Pageable pageable) {
        Page<JasperReport> page = jasperReportRepository.findAll(pageable);
        return ResponseEntity.ok().body(page.getContent());
    }

}
