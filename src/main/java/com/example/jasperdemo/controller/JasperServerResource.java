package com.example.jasperdemo.controller;


import com.example.jasperdemo.config.ApplicationProperties;
import com.example.jasperdemo.domain.DataSource;
import com.example.jasperdemo.domain.JasperReport;
import com.example.jasperdemo.repository.DataSourceRepository;
import com.example.jasperdemo.repository.JasperReportRepository;
import com.example.jasperdemo.service.DataSourceDto;
import com.example.jasperdemo.service.ReportDto;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;
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
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() +"/resources")
                .method("GET", null)
                .addHeader("Content-Type", "application/repository.reportUnit+json")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().body(Objects.requireNonNull(response.body().string()));
    }

    @PostMapping("/upload-report")
    public ResponseEntity<JasperReport> uploadReport(@RequestBody ReportDto dto){
        DataSource dataSource = dataSourceRepository.findById(dto.dataSourceId())
                .orElseThrow(() ->new RuntimeException("Datasource not found with this id"));

        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/repository.reportUnit+json");
        okhttp3.RequestBody body = okhttp3.RequestBody
                .create(mediaType, "{\\n    \\\"label\\\" : \\\"tes1333t\\\",\\n    \\\"jrxml\\\": {\\n        \\\"jrxmlFile\\\": {\\n            \\\"label\\\": \\\"docstore_report\\\",\\n            \\\"type\\\":\\\"jrxml\\\",\\n            \\\"content\\\": \\\"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPCEtLSBDcmVhdGVkIHdpdGggSmFzcGVyc29mdCBTdHVkaW8gdmVyc2lvbiA2LjIwLjMuZmluYWwgdXNpbmcgSmFzcGVyUmVwb3J0cyBMaWJyYXJ5IHZlcnNpb24gNi4yMC4zLTQxNWY5NDI4Y2ZmZGI2ODA1YzZmODViYmIyOWViYWYxODgxM2EyYWIgIC0tPgo8amFzcGVyUmVwb3J0IHhtbG5zPSJodHRwOi8vamFzcGVycmVwb3J0cy5zb3VyY2Vmb3JnZS5uZXQvamFzcGVycmVwb3J0cyIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnNjaGVtYUxvY2F0aW9uPSJodHRwOi8vamFzcGVycmVwb3J0cy5zb3VyY2Vmb3JnZS5uZXQvamFzcGVycmVwb3J0cyBodHRwOi8vamFzcGVycmVwb3J0cy5zb3VyY2Vmb3JnZS5uZXQveHNkL2phc3BlcnJlcG9ydC54c2QiIG5hbWU9ImRvY3N0b3JlX3NhbXBsZSIgcGFnZVdpZHRoPSI1OTUiIHBhZ2VIZWlnaHQ9Ijg0MiIgY29sdW1uV2lkdGg9IjUzNSIgbGVmdE1hcmdpbj0iMjAiIHJpZ2h0TWFyZ2luPSIyMCIgdG9wTWFyZ2luPSIyMCIgYm90dG9tTWFyZ2luPSIyMCIgdXVpZD0iMzkzODYxZmYtM2IzMi00NzA2LThiODMtZGViMjI5NDI2ZGI2Ij4KICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZGF0YS5zcWwudGFibGVzIiB2YWx1ZT0iWkc5amRXMWxiblFnTERFMUxERTFMREExTnpnMVptRTNMVEEyWmpRdE5HTXdZUzA1WXpVNUxUWmxaRFV6TlRreE5XVmlOanM9Ii8+CiAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLmRhdGEuZGVmYXVsdGRhdGFhZGFwdGVyIiB2YWx1ZT0iRG9jc3RvcmUgbG9jYWwgcHNxbCIvPgogICAgPHF1ZXJ5U3RyaW5nIGxhbmd1YWdlPSJTUUwiPgogICAgICAgIDwhW0NEQVRBW1NFTEVDVCAqCkZST00gcHVibGljLmRvY3VtZW50XV0+CiAgICA8L3F1ZXJ5U3RyaW5nPgogICAgPGZpZWxkIG5hbWU9ImlkIiBjbGFzcz0iamF2YS51dGlsLlVVSUQiPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQubmFtZSIgdmFsdWU9ImlkIi8+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC5sYWJlbCIgdmFsdWU9ImlkIi8+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC50cmVlLnBhdGgiIHZhbHVlPSJkb2N1bWVudCIvPgogICAgPC9maWVsZD4KICAgIDxmaWVsZCBuYW1lPSJuYW1lIiBjbGFzcz0iamF2YS5sYW5nLlN0cmluZyI+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC5uYW1lIiB2YWx1ZT0ibmFtZSIvPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQubGFiZWwiIHZhbHVlPSJuYW1lIi8+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC50cmVlLnBhdGgiIHZhbHVlPSJkb2N1bWVudCIvPgogICAgPC9maWVsZD4KICAgIDxmaWVsZCBuYW1lPSJyZWNvcmRfdHlwZSIgY2xhc3M9ImphdmEubGFuZy5TdHJpbmciPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQubmFtZSIgdmFsdWU9InJlY29yZF90eXBlIi8+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC5sYWJlbCIgdmFsdWU9InJlY29yZF90eXBlIi8+CiAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5maWVsZC50cmVlLnBhdGgiIHZhbHVlPSJkb2N1bWVudCIvPgogICAgPC9maWVsZD4KICAgIDxmaWVsZCBuYW1lPSJ1c2VyX2lkIiBjbGFzcz0iamF2YS5sYW5nLkxvbmciPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQubmFtZSIgdmFsdWU9InVzZXJfaWQiLz4KICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLmZpZWxkLmxhYmVsIiB2YWx1ZT0idXNlcl9pZCIvPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQudHJlZS5wYXRoIiB2YWx1ZT0iZG9jdW1lbnQiLz4KICAgIDwvZmllbGQ+CiAgICA8ZmllbGQgbmFtZT0iY3JlYXRlZF9ieSIgY2xhc3M9ImphdmEubGFuZy5TdHJpbmciPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQubmFtZSIgdmFsdWU9ImNyZWF0ZWRfYnkiLz4KICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLmZpZWxkLmxhYmVsIiB2YWx1ZT0iY3JlYXRlZF9ieSIvPgogICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uZmllbGQudHJlZS5wYXRoIiB2YWx1ZT0iZG9jdW1lbnQiLz4KICAgIDwvZmllbGQ+CiAgICA8YmFja2dyb3VuZD4KICAgICAgICA8YmFuZC8+CiAgICA8L2JhY2tncm91bmQ+CiAgICA8dGl0bGU+CiAgICAgICAgPGJhbmQgaGVpZ2h0PSI3MiI+CiAgICAgICAgICAgIDxmcmFtZT4KICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IG1vZGU9Ik9wYXF1ZSIgeD0iLTIwIiB5PSItMjAiIHdpZHRoPSI1OTUiIGhlaWdodD0iOTIiIGJhY2tjb2xvcj0iIzAwNjY5OSIgdXVpZD0iYzQ3OTY3MGMtYzE5YS00YTZkLTlkMDAtZmY3MTFkNTQyNmJhIi8+CiAgICAgICAgICAgICAgICA8c3RhdGljVGV4dD4KICAgICAgICAgICAgICAgICAgICA8cmVwb3J0RWxlbWVudCB4PSIyMCIgeT0iMjAiIHdpZHRoPSIyMzQiIGhlaWdodD0iNDMiIGZvcmVjb2xvcj0iI0ZGRkZGRiIgdXVpZD0iYmQ4MTA5NzUtYWE5Ni00ZjZiLTg1OWYtNGQ3MGVkMWRlNDljIi8+CiAgICAgICAgICAgICAgICAgICAgPHRleHRFbGVtZW50PgogICAgICAgICAgICAgICAgICAgICAgICA8Zm9udCBzaXplPSIzNCIgaXNCb2xkPSJ0cnVlIi8+CiAgICAgICAgICAgICAgICAgICAgPC90ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgICAgICA8dGV4dD48IVtDREFUQVtUSVRMRV1dPjwvdGV4dD4KICAgICAgICAgICAgICAgIDwvc3RhdGljVGV4dD4KICAgICAgICAgICAgICAgIDxzdGF0aWNUZXh0PgogICAgICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IHg9IjM5NSIgeT0iNDMiIHdpZHRoPSIxODAiIGhlaWdodD0iMjAiIGZvcmVjb2xvcj0iI0ZGRkZGRiIgdXVpZD0iYzJjY2YyZTItMjkxOS00NjQxLWEwN2EtMWIwZjE2MmYxOGUyIi8+CiAgICAgICAgICAgICAgICAgICAgPHRleHRFbGVtZW50IHRleHRBbGlnbm1lbnQ9IlJpZ2h0Ij4KICAgICAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiIGlzQm9sZD0iZmFsc2UiLz4KICAgICAgICAgICAgICAgICAgICA8L3RleHRFbGVtZW50PgogICAgICAgICAgICAgICAgICAgIDx0ZXh0PjwhW0NEQVRBW0FkZCBhIGRlc2NyaXB0aW9uIGhlcmVdXT48L3RleHQ+CiAgICAgICAgICAgICAgICA8L3N0YXRpY1RleHQ+CiAgICAgICAgICAgIDwvZnJhbWU+CiAgICAgICAgPC9iYW5kPgogICAgPC90aXRsZT4KICAgIDxwYWdlSGVhZGVyPgogICAgICAgIDxiYW5kIGhlaWdodD0iMTMiLz4KICAgIDwvcGFnZUhlYWRlcj4KICAgIDxjb2x1bW5IZWFkZXI+CiAgICAgICAgPGJhbmQgaGVpZ2h0PSIyMSI+CiAgICAgICAgICAgIDxsaW5lPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgeD0iLTIwIiB5PSIyMCIgd2lkdGg9IjU5NSIgaGVpZ2h0PSIxIiBmb3JlY29sb3I9IiM2NjY2NjYiIHV1aWQ9Ijc0ZGRiZGFhLTMxOTEtNDNkNC1iYzNkLWZlMmIxNWYzZWNlNCIvPgogICAgICAgICAgICA8L2xpbmU+CiAgICAgICAgICAgIDxzdGF0aWNUZXh0PgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgbW9kZT0iT3BhcXVlIiB4PSIwIiB5PSIwIiB3aWR0aD0iMTExIiBoZWlnaHQ9IjIwIiBmb3JlY29sb3I9IiMwMDY2OTkiIGJhY2tjb2xvcj0iI0U2RTZFNiIgdXVpZD0iZjIxYjBlOWMtM2NmZi00MTZhLTgwNTYtODE4YTk5YTJiZDM0Ij4KICAgICAgICAgICAgICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLnNwcmVhZHNoZWV0LmNvbm5lY3Rpb25JRCIgdmFsdWU9IjQ2ZTVjOTkzLTg1ZGItNGQwZi05YWZmLTljNDg2OGE5MDM4MSIvPgogICAgICAgICAgICAgICAgPC9yZXBvcnRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRFbGVtZW50IHRleHRBbGlnbm1lbnQ9IkNlbnRlciI+CiAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiIGlzQm9sZD0idHJ1ZSIvPgogICAgICAgICAgICAgICAgPC90ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgIDx0ZXh0PjwhW0NEQVRBW2lkXV0+PC90ZXh0PgogICAgICAgICAgICA8L3N0YXRpY1RleHQ+CiAgICAgICAgICAgIDxzdGF0aWNUZXh0PgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgbW9kZT0iT3BhcXVlIiB4PSIxMTEiIHk9IjAiIHdpZHRoPSIxMTEiIGhlaWdodD0iMjAiIGZvcmVjb2xvcj0iIzAwNjY5OSIgYmFja2NvbG9yPSIjRTZFNkU2IiB1dWlkPSI0ZWYyOWIyNy1lZTQ1LTRhMTctOTk1NC1lYTY0NWMzNThmNDkiPgogICAgICAgICAgICAgICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uc3ByZWFkc2hlZXQuY29ubmVjdGlvbklEIiB2YWx1ZT0iYmE5OTdlOTItOGU0Mi00MjBjLWJmNWMtMzVkYjAyMDhmYjg5Ii8+CiAgICAgICAgICAgICAgICA8L3JlcG9ydEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEVsZW1lbnQgdGV4dEFsaWdubWVudD0iQ2VudGVyIj4KICAgICAgICAgICAgICAgICAgICA8Zm9udCBzaXplPSIxNCIgaXNCb2xkPSJ0cnVlIi8+CiAgICAgICAgICAgICAgICA8L3RleHRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHQ+PCFbQ0RBVEFbbmFtZV1dPjwvdGV4dD4KICAgICAgICAgICAgPC9zdGF0aWNUZXh0PgogICAgICAgICAgICA8c3RhdGljVGV4dD4KICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IG1vZGU9Ik9wYXF1ZSIgeD0iMjIyIiB5PSIwIiB3aWR0aD0iMTExIiBoZWlnaHQ9IjIwIiBmb3JlY29sb3I9IiMwMDY2OTkiIGJhY2tjb2xvcj0iI0U2RTZFNiIgdXVpZD0iMjAwYThiOTItMzMzNC00NDgyLTg2MTItZDFhYWEyZGJmY2M2Ij4KICAgICAgICAgICAgICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLnNwcmVhZHNoZWV0LmNvbm5lY3Rpb25JRCIgdmFsdWU9ImU0NzVlYzFmLThjMmYtNDFhNy1hMjQ2LTliOTFhYTg2N2I4YyIvPgogICAgICAgICAgICAgICAgPC9yZXBvcnRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRFbGVtZW50IHRleHRBbGlnbm1lbnQ9IkNlbnRlciI+CiAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiIGlzQm9sZD0idHJ1ZSIvPgogICAgICAgICAgICAgICAgPC90ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgIDx0ZXh0PjwhW0NEQVRBW3JlY29yZF90eXBlXV0+PC90ZXh0PgogICAgICAgICAgICA8L3N0YXRpY1RleHQ+CiAgICAgICAgICAgIDxzdGF0aWNUZXh0PgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgbW9kZT0iT3BhcXVlIiB4PSIzMzMiIHk9IjAiIHdpZHRoPSIxMTEiIGhlaWdodD0iMjAiIGZvcmVjb2xvcj0iIzAwNjY5OSIgYmFja2NvbG9yPSIjRTZFNkU2IiB1dWlkPSJlYTgyYTRlOC1mNmU4LTQ3ZWMtYmQ0My1hN2ZiM2ZjZTUxYTkiPgogICAgICAgICAgICAgICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uc3ByZWFkc2hlZXQuY29ubmVjdGlvbklEIiB2YWx1ZT0iZDBlZjZkODEtMzFmZi00MmYzLWIwMTYtMjNlZGRjOTEwYzA3Ii8+CiAgICAgICAgICAgICAgICA8L3JlcG9ydEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEVsZW1lbnQgdGV4dEFsaWdubWVudD0iQ2VudGVyIj4KICAgICAgICAgICAgICAgICAgICA8Zm9udCBzaXplPSIxNCIgaXNCb2xkPSJ0cnVlIi8+CiAgICAgICAgICAgICAgICA8L3RleHRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHQ+PCFbQ0RBVEFbdXNlcl9pZF1dPjwvdGV4dD4KICAgICAgICAgICAgPC9zdGF0aWNUZXh0PgogICAgICAgICAgICA8c3RhdGljVGV4dD4KICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IG1vZGU9Ik9wYXF1ZSIgeD0iNDQ0IiB5PSIwIiB3aWR0aD0iMTExIiBoZWlnaHQ9IjIwIiBmb3JlY29sb3I9IiMwMDY2OTkiIGJhY2tjb2xvcj0iI0U2RTZFNiIgdXVpZD0iZWM4ZTE0MTAtMmUxNi00YzVlLTlmNzktOTQ5NjYxYWUyNGQ3Ij4KICAgICAgICAgICAgICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLnNwcmVhZHNoZWV0LmNvbm5lY3Rpb25JRCIgdmFsdWU9ImRiNmJhM2Q5LWEyMTMtNGEwNC1iNWEyLTZlYTJmYTRhZjg2MiIvPgogICAgICAgICAgICAgICAgPC9yZXBvcnRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRFbGVtZW50IHRleHRBbGlnbm1lbnQ9IkNlbnRlciI+CiAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiIGlzQm9sZD0idHJ1ZSIvPgogICAgICAgICAgICAgICAgPC90ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgIDx0ZXh0PjwhW0NEQVRBW2NyZWF0ZWRfYnldXT48L3RleHQ+CiAgICAgICAgICAgIDwvc3RhdGljVGV4dD4KICAgICAgICA8L2JhbmQ+CiAgICA8L2NvbHVtbkhlYWRlcj4KICAgIDxkZXRhaWw+CiAgICAgICAgPGJhbmQgaGVpZ2h0PSIyMCI+CiAgICAgICAgICAgIDxsaW5lPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgcG9zaXRpb25UeXBlPSJGaXhSZWxhdGl2ZVRvQm90dG9tIiB4PSIwIiB5PSIxOSIgd2lkdGg9IjU1NSIgaGVpZ2h0PSIxIiB1dWlkPSI2YjY0MWZhYy03ZGQ3LTQ5ZDEtOWFiMC1mMTA1NjgyN2JhMjUiLz4KICAgICAgICAgICAgPC9saW5lPgogICAgICAgICAgICA8dGV4dEZpZWxkIHRleHRBZGp1c3Q9IlN0cmV0Y2hIZWlnaHQiPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgeD0iMCIgeT0iMCIgd2lkdGg9IjExMSIgaGVpZ2h0PSIyMCIgdXVpZD0iZmU2NDNiOTItYTQwNy00MzQ5LWE5ZmYtZGEyMjkxMTI5MTAxIj4KICAgICAgICAgICAgICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLnNwcmVhZHNoZWV0LmNvbm5lY3Rpb25JRCIgdmFsdWU9IjQ2ZTVjOTkzLTg1ZGItNGQwZi05YWZmLTljNDg2OGE5MDM4MSIvPgogICAgICAgICAgICAgICAgPC9yZXBvcnRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRFbGVtZW50PgogICAgICAgICAgICAgICAgICAgIDxmb250IHNpemU9IjE0Ii8+CiAgICAgICAgICAgICAgICA8L3RleHRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRGaWVsZEV4cHJlc3Npb24+PCFbQ0RBVEFbJEZ7aWR9XV0+PC90ZXh0RmllbGRFeHByZXNzaW9uPgogICAgICAgICAgICA8L3RleHRGaWVsZD4KICAgICAgICAgICAgPHRleHRGaWVsZCB0ZXh0QWRqdXN0PSJTdHJldGNoSGVpZ2h0Ij4KICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IHg9IjExMSIgeT0iMCIgd2lkdGg9IjExMSIgaGVpZ2h0PSIyMCIgdXVpZD0iNTkzNmUzZTAtM2JlOS00MmZkLTlkMGMtOGM0NmNhZTczMjYwIj4KICAgICAgICAgICAgICAgICAgICA8cHJvcGVydHkgbmFtZT0iY29tLmphc3BlcnNvZnQuc3R1ZGlvLnNwcmVhZHNoZWV0LmNvbm5lY3Rpb25JRCIgdmFsdWU9ImJhOTk3ZTkyLThlNDItNDIwYy1iZjVjLTM1ZGIwMjA4ZmI4OSIvPgogICAgICAgICAgICAgICAgPC9yZXBvcnRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRFbGVtZW50PgogICAgICAgICAgICAgICAgICAgIDxmb250IHNpemU9IjE0Ii8+CiAgICAgICAgICAgICAgICA8L3RleHRFbGVtZW50PgogICAgICAgICAgICAgICAgPHRleHRGaWVsZEV4cHJlc3Npb24+PCFbQ0RBVEFbJEZ7bmFtZX1dXT48L3RleHRGaWVsZEV4cHJlc3Npb24+CiAgICAgICAgICAgIDwvdGV4dEZpZWxkPgogICAgICAgICAgICA8dGV4dEZpZWxkIHRleHRBZGp1c3Q9IlN0cmV0Y2hIZWlnaHQiPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgeD0iMjIyIiB5PSIwIiB3aWR0aD0iMTExIiBoZWlnaHQ9IjIwIiB1dWlkPSIxNjhkMWIyOS05ZjA2LTQ5Y2EtODQ5My1jOGMyOGQ5Mzg0ODAiPgogICAgICAgICAgICAgICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uc3ByZWFkc2hlZXQuY29ubmVjdGlvbklEIiB2YWx1ZT0iZTQ3NWVjMWYtOGMyZi00MWE3LWEyNDYtOWI5MWFhODY3YjhjIi8+CiAgICAgICAgICAgICAgICA8L3JlcG9ydEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEVsZW1lbnQ+CiAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiLz4KICAgICAgICAgICAgICAgIDwvdGV4dEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEZpZWxkRXhwcmVzc2lvbj48IVtDREFUQVskRntyZWNvcmRfdHlwZX1dXT48L3RleHRGaWVsZEV4cHJlc3Npb24+CiAgICAgICAgICAgIDwvdGV4dEZpZWxkPgogICAgICAgICAgICA8dGV4dEZpZWxkIHRleHRBZGp1c3Q9IlN0cmV0Y2hIZWlnaHQiPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgeD0iMzMzIiB5PSIwIiB3aWR0aD0iMTExIiBoZWlnaHQ9IjIwIiB1dWlkPSI3ZGQ3OTY3Mi1hZGUxLTQwNDQtYjk2MS0wYzk0M2Y2NmQzNmMiPgogICAgICAgICAgICAgICAgICAgIDxwcm9wZXJ0eSBuYW1lPSJjb20uamFzcGVyc29mdC5zdHVkaW8uc3ByZWFkc2hlZXQuY29ubmVjdGlvbklEIiB2YWx1ZT0iZDBlZjZkODEtMzFmZi00MmYzLWIwMTYtMjNlZGRjOTEwYzA3Ii8+CiAgICAgICAgICAgICAgICA8L3JlcG9ydEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEVsZW1lbnQ+CiAgICAgICAgICAgICAgICAgICAgPGZvbnQgc2l6ZT0iMTQiLz4KICAgICAgICAgICAgICAgIDwvdGV4dEVsZW1lbnQ+CiAgICAgICAgICAgICAgICA8dGV4dEZpZWxkRXhwcmVzc2lvbj48IVtDREFUQVskRnt1c2VyX2lkfV1dPjwvdGV4dEZpZWxkRXhwcmVzc2lvbj4KICAgICAgICAgICAgPC90ZXh0RmllbGQ+CiAgICAgICAgICAgIDx0ZXh0RmllbGQgdGV4dEFkanVzdD0iU3RyZXRjaEhlaWdodCI+CiAgICAgICAgICAgICAgICA8cmVwb3J0RWxlbWVudCB4PSI0NDQiIHk9IjAiIHdpZHRoPSIxMTEiIGhlaWdodD0iMjAiIHV1aWQ9IjE4NDg5ZWY1LWY3MmQtNDMyZC04MjNiLWRiNmE0NDYyNDA3OSI+CiAgICAgICAgICAgICAgICAgICAgPHByb3BlcnR5IG5hbWU9ImNvbS5qYXNwZXJzb2Z0LnN0dWRpby5zcHJlYWRzaGVldC5jb25uZWN0aW9uSUQiIHZhbHVlPSJkYjZiYTNkOS1hMjEzLTRhMDQtYjVhMi02ZWEyZmE0YWY4NjIiLz4KICAgICAgICAgICAgICAgIDwvcmVwb3J0RWxlbWVudD4KICAgICAgICAgICAgICAgIDx0ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgICAgICA8Zm9udCBzaXplPSIxNCIvPgogICAgICAgICAgICAgICAgPC90ZXh0RWxlbWVudD4KICAgICAgICAgICAgICAgIDx0ZXh0RmllbGRFeHByZXNzaW9uPjwhW0NEQVRBWyRGe2NyZWF0ZWRfYnl9XV0+PC90ZXh0RmllbGRFeHByZXNzaW9uPgogICAgICAgICAgICA8L3RleHRGaWVsZD4KICAgICAgICA8L2JhbmQ+CiAgICA8L2RldGFpbD4KICAgIDxjb2x1bW5Gb290ZXI+CiAgICAgICAgPGJhbmQvPgogICAgPC9jb2x1bW5Gb290ZXI+CiAgICA8cGFnZUZvb3Rlcj4KICAgICAgICA8YmFuZCBoZWlnaHQ9IjE3Ij4KICAgICAgICAgICAgPHRleHRGaWVsZD4KICAgICAgICAgICAgICAgIDxyZXBvcnRFbGVtZW50IG1vZGU9Ik9wYXF1ZSIgeD0iMCIgeT0iNCIgd2lkdGg9IjUxNSIgaGVpZ2h0PSIxMyIgYmFja2NvbG9yPSIjRTZFNkU2IiB1dWlkPSJiYWY1ZTQ2Yi03OTYxLTRkMmEtODlkOS1kY2ZlMmViM2E3NDkiLz4KICAgICAgICAgICAgICAgIDx0ZXh0RWxlbWVudCB0ZXh0QWxpZ25tZW50PSJSaWdodCIvPgogICAgICAgICAgICAgICAgPHRleHRGaWVsZEV4cHJlc3Npb24+PCFbQ0RBVEFbIlBhZ2UgIiskVntQQUdFX05VTUJFUn0rIiBvZiJdXT48L3RleHRGaWVsZEV4cHJlc3Npb24+CiAgICAgICAgICAgIDwvdGV4dEZpZWxkPgogICAgICAgICAgICA8dGV4dEZpZWxkIGV2YWx1YXRpb25UaW1lPSJSZXBvcnQiPgogICAgICAgICAgICAgICAgPHJlcG9ydEVsZW1lbnQgbW9kZT0iT3BhcXVlIiB4PSI1MTUiIHk9IjQiIHdpZHRoPSI0MCIgaGVpZ2h0PSIxMyIgYmFja2NvbG9yPSIjRTZFNkU2IiB1dWlkPSJhNTQ1MDZmMi1kMWU1LTQ2MmEtOWU1Yy0xYjVlZTZhNGNjMzEiLz4KICAgICAgICAgICAgICAgIDx0ZXh0RmllbGRFeHByZXNzaW9uPjwhW0NEQVRBWyIgIiArICRWe1BBR0VfTlVNQkVSfV1dPjwvdGV4dEZpZWxkRXhwcmVzc2lvbj4KICAgICAgICAgICAgPC90ZXh0RmllbGQ+CiAgICAgICAgICAgIDx0ZXh0RmllbGQgcGF0dGVybj0iRUVFRUUgZGQgTU1NTU0geXl5eSI+CiAgICAgICAgICAgICAgICA8cmVwb3J0RWxlbWVudCB4PSIwIiB5PSI0IiB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEzIiB1dWlkPSIwMWVmODQyNS02MzgwLTRjN2UtODgyZC04YTllZWU0ODZlNjciLz4KICAgICAgICAgICAgICAgIDx0ZXh0RmllbGRFeHByZXNzaW9uPjwhW0NEQVRBW25ldyBqYXZhLnV0aWwuRGF0ZSgpXV0+PC90ZXh0RmllbGRFeHByZXNzaW9uPgogICAgICAgICAgICA8L3RleHRGaWVsZD4KICAgICAgICA8L2JhbmQ+CiAgICA8L3BhZ2VGb290ZXI+CiAgICA8c3VtbWFyeT4KICAgICAgICA8YmFuZC8+CiAgICA8L3N1bW1hcnk+CjwvamFzcGVyUmVwb3J0Pg==.\\\"\\n        }\\n    },\\n    \\\"dataSource\\\": {\\n        \\\"dataSourceReference\\\": {\\n            \\\"uri\\\": \\\"/datasources/added_from_swagger\\\"\\n        }\\n    }\\n}");
//                .create(mediaType, "{\n    \"jrxml\": {\n        \"jrxmlFile\": {\n            \"label\": \"%s\" ,\n            \"type\":\"%s\",\n            \"content\": \"%s\"\n        }\n    },\n        \"label\" : \"%s\"\n}"
        Request request = new Request.Builder()
                .url(applicationProperties.jasperServerUrl() + "/resources/reports/interactive?j_username="+applicationProperties.username() + "&j_password=" +applicationProperties.password() )
                .method("POST", body)
                .addHeader("Content-Type", "application/repository.reportUnit+json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 201){
                Pattern pattern = Pattern.compile("<uri>(.*)</uri>");
                Matcher matcher = pattern.matcher(Objects.requireNonNull(response.body()).string());
                if (matcher.find()) {
                    String uri = matcher.group(1);
                    JasperReport jasperReport = new JasperReport();
                    jasperReport.setName(dto.label());
                    jasperReport.setReportUnitUri(uri);
                    jasperReport.setDataSource(dataSource);
                    jasperReport.setData(dto.data().getBytes());
                    jasperReport.setDataContentType(dto.type());
                    return ResponseEntity.ok().body(jasperReportRepository.save(jasperReport));
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
