package com.sakcode.khqr.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.sakcode.khqr.payload.KhqrResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class ReportService {

    private static final String DIR = "D:/WORKS/FTB/Source code/Java/Learn/khqr/image/";
    private static final String FONT_DIR = "D:/WORKS/FTB/Source code/Java/Learn/khqr/src/main/resources/report/";
    private static final String ext = ".png";

    public KhqrResponse exportMe() {

        KhqrResponse khqrResponse = new KhqrResponse();
        khqrResponse.setStatus("fail");

        try {
            cleanDirectory(DIR);
            initDirectory(DIR);

            String qrPath = generateQR();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("M_NAME", "Your Merchant Name Your Merchant Name");
            parameters.put("AMOUNT", "12,580,000 KHR");
            parameters.put("LOGO_DIR", FONT_DIR + "KHQRLogo.png");
            parameters.put("SHAPE_DIR", FONT_DIR + "Triangle.png");
            parameters.put("QRCODE", qrPath == null ? "" : qrPath);

            //load file and compile it
            File file = ResourceUtils.getFile(FONT_DIR + "KHQR_TEMPLATE.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            JRDesignStyle jrDesignStyle = new JRDesignStyle();
            jrDesignStyle.setDefault(true);
            jasperPrint.addStyle(jrDesignStyle);

            // export
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
            reportConfig.setSizePageToContent(true);
            reportConfig.setForceLineBreakPolicy(false);
            SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
            exportConfig.setMetadataAuthor("Developer");
            exportConfig.setEncrypted(true);
            exportConfig.setAllowedPermissionsHint("PRINTING");
            exporter.setConfiguration(reportConfig);
            exporter.setConfiguration(exportConfig);
            ByteArrayOutputStream pdfReport = new ByteArrayOutputStream();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReport));
            exporter.exportReport();

            byte[] pdfReportBytes = pdfReport.toByteArray();
            String base64PdfReport = Base64.getEncoder().encodeToString(pdfReportBytes);

//            System.out.println("PDF:::: " + base64PdfReport);

            exporter.exportReport();

            String pdfPath = DIR + generateRandoTitle(new Random(),9) + ".pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, pdfPath);

            String base64Image = "";

            try (PDDocument document = PDDocument.load(new File(pdfPath))) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                // Convert each page of the PDF to an image
                for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 300);
                    // Convert the BufferedImage to Base64
                    // Resize the image to 300x430 pixels
                    BufferedImage resizedImage = resizeImage(bufferedImage, 300, 430);
                    base64Image = convertToBase64(resizedImage);
                    System.out.println("Base64 encoded image for page " + (pageIndex + 1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            khqrResponse.setStatus("success");
            khqrResponse.setImageBase64(base64Image);
            khqrResponse.setPdfBase64(base64PdfReport);
            return khqrResponse;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return khqrResponse;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return resizedImage;
    }

    private static String convertToBase64(BufferedImage bufferedImage) {
        String base64Image = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Write the BufferedImage to the output stream
            ImageIO.write(bufferedImage, "png", outputStream);
            // Convert the output stream to byte array
            byte[] imageBytes = outputStream.toByteArray();
            // Encode the byte array to Base64
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64Image;
    }


    private void initDirectory(String DIR) throws IOException {
        Files.createDirectories(Paths.get(DIR));
    }

    private void cleanDirectory(String DIR) {
        try {
            Files.walk(Paths.get(DIR), FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            // Directory does not exist, Do nothing

        }
    }

    private String generateRandoTitle(Random random, int length) {
        return random.ints(48, 122).filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97)).mapToObj(i -> (char) i).limit(length).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    public String generateQR() {
        String qrContent = "00020101021129200016samreachyan@abaa5204599953038405802KH5912SAMREACH YAN6010Phnom Penh63047D3F";
        int width = 270;
        int height = 270;

        try {
            // Set QR code parameters
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // Generate QR code
            BitMatrix bitMatrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height, hintMap);
            // Save the buffered image as a PNG file
            String filePath = DIR + generateRandoTitle(new Random(), 9) + ".png";
            File qrFile = new File(filePath); // Change the file name as needed

            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", qrFile.toPath());

            System.out.println("QR code generated successfully!");
            return filePath;
        } catch (Exception e) {
            System.out.println("Error generating QR code: " + e.getMessage());
        }

        return null;
    }

}
