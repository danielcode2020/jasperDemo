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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/jasper-server")
public class JasperServerResource {
    private final Logger log = LoggerFactory.getLogger(JasperServerResource.class);
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

//    @PostMapping("/login-auto")
//    public void loginAutomatically(){
//        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
//        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "j_username=" +applicationProperties.username() + "&j_password=" +applicationProperties.password() );
//        Request request = new Request.Builder()
//                .url(applicationProperties.jasperServerUrl()+ "/login")
//                .method("POST", body)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();
//        try {
//            Response response = client.newCall(request).execute();
//            Pattern pattern = Pattern.compile("JSESSIONID=([^;]+)");
//            Matcher matcher = pattern.matcher(response.headers("Set-Cookie").get(0));
//            if (matcher.find()) {
//                // Extract the JSESSIONID value
//                CookieStatic.cookieValue = matcher.group(1);
//            } else {
//                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        log.info("Logged in automatically");
//    }

    @GetMapping("/get-available-resources")
    public ResponseEntity<String> getAvailableResources() throws IOException {
        log.info("REST request to get available resources");
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() +"/resources")
                .method("GET", null)
                .addHeader("Content-Type", "application/repository.reportUnit+json")
                .build();
        Response response = null;
        String body = null;
        try {
            response = client.newCall(request).execute();
            body = Objects.requireNonNull(response.peekBody(Long.MAX_VALUE)).string();
            System.out.println(body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/upload-report")
    public ResponseEntity<JasperReport> uploadReport(@RequestBody ReportDto dto){
        log.info("REST request to upload a report with name {} ",dto.label());
        DataSource dataSource = dataSourceRepository.findById(dto.dataSourceId())
                .orElseThrow(() ->new RuntimeException("Datasource not found with this id"));
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/repository.reportUnit+json");
        okhttp3.RequestBody body = okhttp3.RequestBody
                .create(mediaType, "{\n    \"label\" : \"%s\",\n    \"jrxml\": {\n        \"jrxmlFile\": {\n            \"label\": \"%s\",\n            \"type\":\"%s\",\n            \"content\": \"%s\"\n        }\n    },\n    \"dataSource\": {\n        \"dataSourceReference\": {\n            \"uri\": \"%s\"\n        }\n    }\n\n}"
                        .formatted(dto.label(),dto.label(),dto.type(),dto.data(),dataSource.getUri()));
//                .create(mediaType, "{\n    \"jrxml\": {\n        \"jrxmlFile\": {\n            \"label\": \"%s\" ,\n            \"type\":\"%s\",\n            \"content\": \"%s\"\n        }\n    },\n        \"label\" : \"%s\"\n}"
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() + "/resources/reports/interactive?j_username="+applicationProperties.username() + "&j_password=" +applicationProperties.password() )
                .method("POST", body)
                .addHeader("Content-Type", "application/repository.reportUnit+json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 201){
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
            System.out.println(response.body());
            System.out.println(response.code());
            System.out.println(response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Exception occured");
    }

    @PostMapping("/export-report")
//    public ResponseEntity<byte[]> exportReport(@RequestBody ExportReport dto){
    public ResponseEntity<byte[]> exportReport(@RequestBody ExportReport dto){
        JasperReport report = jasperReportRepository.findById(dto.id()).orElseThrow(()->new RuntimeException("Report not found"));
        String requestId;
        String exportId;
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/xml");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "<reportExecutionRequest>\n    <reportUnitUri>%s</reportUnitUri>\n    <async>true</async>\n    <freshData>false</freshData>\n    <saveDataSnapshot>false</saveDataSnapshot>\n    <outputFormat>%s</outputFormat>\n    <interactive>true</interactive>\n</reportExecutionRequest>"
                .formatted(report.getReportUnitUri(),dto.format()));
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() +"/reportExecutions?j_username="+applicationProperties.username() + "&j_password=" +applicationProperties.password())
                .method("POST", body)
                .addHeader("Content-Type", "application/xml")
                .build();
        try {
            Response response = client.newCall(request).execute();
            log.info("executing call to export");
            String respBody = Objects.requireNonNull(response.body()).string();
            List<String> Cookielist = response.headers().values("Set-Cookie");
            String jsessionId = (Cookielist .get(0).split(";"))[0];
            log.info(respBody);
            if (response.code() == 200){
                log.info("received response");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    log.error("thread exception sleep");
                    throw new RuntimeException(e);
                }
                JSONObject jsonObject = new JSONObject(respBody);
                requestId = jsonObject.getString("requestId");

                JSONArray exportsArray = jsonObject.getJSONArray("exports");
                JSONObject exportsObject = exportsArray.getJSONObject(0);
                exportId = exportsObject.getString("id");

                log.info("requestId : {} , exportId : {}",requestId,exportId);
                log.info("jsessionId : {} ", jsessionId);

                Request requestReportStatus = new Request.Builder()
                        .url(applicationProperties.jasperServerUrl() + "/reportExecutions/" + requestId +"/status/?j_username="+applicationProperties.username() + "&j_password=" +applicationProperties.password())
                        .method("GET", null)
                        .addHeader("Cookie", "userLocale=en_US;" + jsessionId)
                        .build();
                Response responseReportStatus = client.newCall(requestReportStatus).execute();
                String respReportStatus = Objects.requireNonNull(responseReportStatus.body()).string();
                log.info(respReportStatus);
                if (responseReportStatus.code()==200) {
                    JSONObject jsonObject1 = new JSONObject(respReportStatus);
                    String value = jsonObject1.getString("value");
                    log.info("value : {}", value);
                    if (value.equals("ready")){

                        Request request2 = new Request.Builder()
                                .url(applicationProperties.jasperServerUrl() +"/reportExecutions/" + requestId + "/exports/"+ exportId + "/outputResource?j_username="+applicationProperties.username() + "&j_password=" +applicationProperties.password())
                                .method("GET", null)
                                .addHeader("Cookie", "userLocale=en_US; " + jsessionId)
                                .build();
                        Response response2 = client.newCall(request2).execute();
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=test."+dto.format())
                                .body(Objects.requireNonNull(response2.body()).bytes());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Some error occured");
    }


    /*

        {
        "label":"postman_db_docstore____2",
        "driverClass":"org.postgresql.Driver",
        "password":"daniel",
        "username":"daniel",
        "connectionUrl":"jdbc:postgresql://localhost:5432/docstore_playground"

        }
     */
    @PostMapping("/add-new-data-source")
    public ResponseEntity<DataSource> addNewDataSource(@RequestBody DataSourceDto dto){
        log.debug("Request to add new data source");
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/repository.jdbcDataSource+json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "\n{\n    \"label\":\"%s\",\n\"driverClass\":\"%s\",\n\"password\":\"%s\",\n\"username\":\"%s\",\n\"connectionUrl\":\"%s\"\n\n}\n"
                .formatted(dto.label(),dto.driverClass(),dto.password(),dto.username(),dto.connectionUrl()));
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl()+"/resources/datasources?j_username=" +applicationProperties.username() + "&j_password=" +applicationProperties.password() )
                .method("POST", body)
                .addHeader("Content-Type", "application/repository.jdbcDataSource+json")
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
            if (response.code() == 401){
                throw new RuntimeException("Unauthorized on jasper server");
            }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Exception occured");
    }

}
