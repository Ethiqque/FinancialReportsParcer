package com.ethiqque.financialparser.controller;

import com.ethiqque.financialparser.service.FinancialReportParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/financial-report")
public class FinancialReportController {

    @Autowired
    private FinancialReportParser reportParser;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFinancialReport(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading file: {}", file.getOriginalFilename());

            File tempFile = convertMultiPartToFile(file);
            log.info("Converted file: {}", tempFile.getName());

            Map<String, Object> parsedData = reportParser.parsePdf(tempFile);
            log.info("Parsed Data: {}", parsedData);

            if (parsedData == null || parsedData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "No data extracted"));
            }

            if (tempFile.exists()) {
                tempFile.delete();
            }

            return ResponseEntity.ok(parsedData);
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "Error processing PDF"));
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        log.info("Saving file to temporary location: {}", convFile.getAbsolutePath());
        file.transferTo(convFile);
        return convFile;
    }
}
