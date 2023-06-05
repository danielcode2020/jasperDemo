package com.example.jasperdemo.controller;


import com.example.jasperdemo.CookieStatic;
import com.example.jasperdemo.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.Get;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/jasper-server")
public class JasperServerResource {
    private static final String jasperServerUrl = "http://localhost:8080/jasperserver/rest_v2";

    private final RestTemplate restTemplate;

    public JasperServerResource(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticateJasperServer(@RequestBody AuthDto authDto){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("j_username", authDto.username());
        map.add("j_password", authDto.password());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(jasperServerUrl+"/login", requestEntity, String.class);
        Pattern pattern = Pattern.compile("JSESSIONID=([^;]+)");
        Matcher matcher =  pattern.matcher(Objects.requireNonNull(responseEntity.getHeaders().get("Set-Cookie")).get(0));
        if (matcher.find()) {
            // Extract the JSESSIONID value
            CookieStatic.cookieValue = matcher.group(1);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body("JSESSIONID:" + CookieStatic.cookieValue);
    }

    @GetMapping("/available-resources")
    public ResponseEntity<String> getAvailableResources(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "JSESSIONID=" + CookieStatic.cookieValue);
        headers.set("Accept", "application/json");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(jasperServerUrl + "/resources", HttpMethod.GET, requestEntity, String.class);
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getBody());
        return ResponseEntity.ok().body(responseEntity.getBody());
    }

    @PostMapping("/upload-file-to-reports-interactive")
    public void uploadReport(@RequestBody ReportDto dto){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "JSESSIONID=" + CookieStatic.cookieValue);
        headers.set("Content-Type", "application/repository.reportUnit+json");

        Map<String, Object> jrxmlFile = new HashMap<>();
        jrxmlFile.put("label", dto.label());
        jrxmlFile.put("type", dto.type());
        jrxmlFile.put("content", dto.data());

        Map<String, Object> jrxml = new HashMap<>();
        jrxml.put("jrxmlFile", jrxmlFile);

        Map<String, Object> uploadDto = new HashMap<>();
        uploadDto.put("jrxml", jrxml);
        uploadDto.put("label",dto.label());

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            System.out.println(objectMapper.writeValueAsString(uploadDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<String> entity = new HttpEntity<>(uploadDto.toString(), headers);

        String responseEntity = restTemplate.postForObject(jasperServerUrl + "/resources/reports/interactive",entity,String.class);
        System.out.println(responseEntity);
    }

    @PostMapping("/")
    public void myPiva(@RequestBody ReportDto dto){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/repository.reportUnit+json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\n    \"jrxml\": {\n        \"jrxmlFile\": {\n            \"label\": \"%s\" ,\n            \"type\":\"%s\",\n            \"content\": \"%s\"\n        }\n    },\n        \"label\" : \"%s\"\n}"
                .formatted(dto.label(),dto.type(),dto.data(),dto.label()));
        Request request = new Request.Builder()
                .url("http://localhost:8080/jasperserver/rest_v2/resources/reports/interactive")
                .method("POST", body)
                .addHeader("Content-Type", "application/repository.reportUnit+json")
                .addHeader("Cookie", "userLocale=en_US; JSESSIONID=" + CookieStatic.cookieValue)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/logout")
    public void logout(){
        CookieStatic.cookieValue = "";
    }
}
