package com.example.jasperdemo.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class JasperReportResource {
    /*
//
//    private final static Logger log = LoggerFactory.getLogger(JasperReportResource.class);
//
//    private static final String ENTITY_NAME = "jasperReport";
//
//
//    private final JasperReportRepository jasperReportRepository;
//    private final JdbcTemplate jdbcTemplate;
//
//    public JasperReportResource(JasperReportRepository jasperReportRepository, JdbcTemplate jdbcTemplate) {
//        this.jasperReportRepository = jasperReportRepository;
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//
//    @PostMapping(value = "/upload-single")
//    public void uploadDocument(@RequestBody ReceivedFileDto file) {
//        log.debug("REST request to save report {} .", file.filename());
//        JasperReport jasperReport = new JasperReport();
//        jasperReport.setData(file.data());
//        jasperReport.setName(file.filename());
//        jasperReportRepository.save(jasperReport);
//    }
//
//    @GetMapping("/report/view/{id}")
//    public void viewReport(@PathVariable("id") Long id) throws JRException {
//
//        JasperReport found = jasperReportRepository.findById(id).orElseThrow();
//        log.debug(Arrays.toString(found.getData()));
//        InputStream in = new ByteArrayInputStream(found.getData());
//        // view content byte[] content as String
////        log.debug(new String(found.getData(), StandardCharsets.UTF_8));
//
//        // creates the .jasper file from .jrxml
//        net.sf.jasperreports.engine.JasperReport report =  JasperCompileManager.compileReport(in);
//
//        // fills the .jasper file
//        JasperPrint jp = JasperFillManager.fillReport(report,
//                new HashMap<>(), // here go the parameters
//                Objects.requireNonNull(getDocstoreConnection()));
//
//        JasperViewer.viewReport(jp,false);
//    }
//
//    @GetMapping("/report/download/{id}")
//    public ResponseEntity<byte[]> downloadReport(@PathVariable("id") Long id) throws JRException {
//
//        JasperReport found = jasperReportRepository.findById(id).orElseThrow();
//        log.debug(Arrays.toString(found.getData()));
//        InputStream in = new ByteArrayInputStream(found.getData());
//        // view content byte[] content as String
////        log.debug(new String(found.getData(), StandardCharsets.UTF_8));
//
//        // creates the .jasper file from .jrxml
//        net.sf.jasperreports.engine.JasperReport report =  JasperCompileManager.compileReport(in);
//
//        // fills the .jasper file
//        JasperPrint jp = JasperFillManager.fillReport(report,
//                new HashMap<>(), // here go the parameters
//                Objects.requireNonNull(getDocstoreConnection()));
//
//
//        byte[] pdfReportData = JasperExportManager.exportReportToPdf(jp);
//
//        ReceivedFileDto file = new ReceivedFileDto(found.getName().replaceFirst("\\.\\w+$", ""),pdfReportData);
//
//        return ResponseEntity
//                .ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.filename())
//                .body(pdfReportData);
//
//    }
//
//    private static Connection getDocstoreConnection(){
//        try {
//            Class.forName("org.postgresql.Driver");
//            String url = "jdbc:postgresql://127.0.0.1:5432/docstore_playground?user=daniel&password=daniel";
//            return DriverManager.getConnection(url);
//        } catch (ClassNotFoundException | SQLException e) {
//            log.error("Connection to db Error");
//            throw new RuntimeException(e);
//        }
//
//    }

     */
}
