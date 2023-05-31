package com.example.jasperdemo.controller;


import com.example.jasperdemo.CookieStatic;
import com.example.jasperdemo.service.AuthDto;
import com.example.jasperdemo.service.ReceivedFileDto;
import com.example.jasperdemo.service.ReportDto;
import org.apache.commons.collections4.Get;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/jasper-server")
public class JasperServerResource {
    private static final String jasperServerUrl = "http://localhost:8081/jasperserver/rest_v2";

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
    public void uploadReport(@RequestBody ReportDto file){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "JSESSIONID=" + CookieStatic.cookieValue);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    }

    @GetMapping("/logout")
    public void logout(){
        CookieStatic.cookieValue = "";
    }
}
