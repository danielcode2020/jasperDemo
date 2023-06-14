package com.example.jasperdemo.controller;

import com.example.jasperdemo.config.ApplicationProperties;
import com.example.jasperdemo.repository.DataSourceRepository;
import com.example.jasperdemo.repository.JasperReportRepository;
import com.example.jasperdemo.service.ExportReport;
import com.example.jasperdemo.xml.Resource;
import com.example.jasperdemo.xml.ResourceLookup;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class FinalController {
    private final Logger log = LoggerFactory.getLogger(JasperServerResource.class);
    private final ApplicationProperties applicationProperties;
    private final OkHttpClient client;

    public FinalController(ApplicationProperties applicationProperties, OkHttpClient client) {
        this.applicationProperties = applicationProperties;
        this.client = client;
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Resource>> getReportsFromJasperServer(Pageable page){
        // limit (page size)
        // offset (page number)
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() + "/resources?folderUri=/reports/interactive&j_username="
                        +applicationProperties.username()+"&j_password="+ applicationProperties.password()+"&type=ReportUnit&limit="+ page.getPageSize() +"&offset=" + page.getOffset())
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code()==200) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                HttpHeaders headers = new HttpHeaders();
                headers.set("Total-Count", response.header("Total-Count"));
                log.info(responseBody);

                JAXBContext jaxbContext = JAXBContext.newInstance(ResourceLookup.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                StringReader reader = new StringReader(responseBody);
                ResourceLookup resourceLookup = (ResourceLookup) unmarshaller.unmarshal(reader);

                List<Resource> resources = resourceLookup.getResources();
                return new ResponseEntity<>(resources, headers, HttpStatus.OK);
            }
        } catch (IOException | JAXBException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Exception Occured, check if input is correct for pagination");
    }

    @PostMapping("/export-report")
    public ResponseEntity<byte[]> exportReport(@RequestBody ExportReport dto) {
        log.info("REST request to execute a report with uri {} ", dto.unitReportUri());

        okhttp3.RequestBody body = buildRequestBodyForReportExecRequest(dto);

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
                    TimeUnit.MILLISECONDS.sleep(200);
                    try (Response responseReportStatus = requestReportStatus(requestId, jsessionId)) {
                        String respReportStatus = Objects.requireNonNull(responseReportStatus.body()).string();

                        if (responseReportStatus.code() == 200) {
                            JSONObject jsonObject1 = new JSONObject(respReportStatus);
                            String value = jsonObject1.getString("value");

                            if (value.equals("ready")) {
                                try (Response response2 = requestReportOutput(requestId, exportId, jsessionId)) {
                                    byte[] content = Objects.requireNonNull(response2.body()).bytes();
                                    return ResponseEntity.ok()
                                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + dto.unitReportUri() + "_" + getExportTimestamp() + "." + dto.format())
                                            .body(content);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Some error occurred");
    }

    private static okhttp3.RequestBody buildRequestBodyForReportExecRequest(ExportReport dto) {
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
                        """.formatted(dto.unitReportUri(), dto.format()));
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
                .addHeader("ContentType", "application/xml")
                .build();

        return client.newCall(request).execute();
    }

}
