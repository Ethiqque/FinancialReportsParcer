package com.ethiqque.financialparser.controller;

import com.ethiqque.financialparser.exception.InvalidFileException;
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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/financial-report")
public class FinancialReportController {

    @Autowired
    private FinancialReportParser reportParser;

    /**
     * Endpoint for uploading a financial report PDF.
     *
     * This method accepts a PDF file through an HTTP POST request, processes the
     * file to extract financial data, and returns the parsed data in JSON format.
     *
     * @param file The uploaded PDF file containing the financial report.
     *             It must be passed as a multipart file in the request.
     * @return ResponseEntity containing a map of parsed financial data. If an
     *         error occurs during processing, an appropriate error message is returned.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFinancialReport(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading file: {}", file.getOriginalFilename());

            Map<String, Object> parsedData = reportParser.parsePdf(file.getInputStream());

            if (parsedData == null || parsedData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "No data extracted"));
            }

            return ResponseEntity.ok(parsedData);
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage());
            throw  new InvalidFileException(e.getMessage());
        }
    }
}
