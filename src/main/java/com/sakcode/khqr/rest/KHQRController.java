package com.sakcode.khqr.rest;


import com.sakcode.khqr.payload.KhqrResponse;
import com.sakcode.khqr.services.KhqrGenerator;
import com.sakcode.khqr.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class KHQRController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private KhqrGenerator khqrGenerator;

    @GetMapping("/khqr")
    public ResponseEntity<?> getKHQR() {
        long startTime = System.currentTimeMillis();
        System.out.println("Calling to generate KHQR");
        KhqrResponse response = reportService.exportMe();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution Time: " + executionTime + " ms");
        System.out.println("Completed generate KHQR");
        return ResponseEntity.ok(response);
    }

    @GetMapping("khqr2")
    public ResponseEntity<?> getKHQR2() {
        long startTime = System.currentTimeMillis();
        KhqrResponse response = new KhqrResponse();
        response.setStatus("success");
        response.setPdfBase64(null);
        response.setImageBase64(khqrGenerator.GenerateKHQR());

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Execution Time: " + executionTime + " ms");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/image")
    public ResponseEntity<?> getKHQRImage() {
        System.out.println("Calling to generate KHQR");
        KhqrResponse response = reportService.exportMe();

        System.out.println("Completed generate KHQR");
        return ResponseEntity.ok(response.getImageBase64());
    }

    @GetMapping("/pdf")
    public ResponseEntity<?> getKHQRPdf() {

        System.out.println("Calling to generate KHQR");
        KhqrResponse response = reportService.exportMe();

        System.out.println("Completed generate KHQR");
        return ResponseEntity.ok(response.getPdfBase64());
    }
}
