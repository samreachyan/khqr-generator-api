package com.sakcode.khqr.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class KhqrGenerator {

    @Value("${asset.image}")
    private String DIR;

    @Value("${asset.resource}")
    private String ASSET_DIR;

    int width = 300;
    int height = 430;

    public String GenerateKHQR() {
        // Create a BufferedImage with the desired dimensions
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get the graphics context of the image
        Graphics2D g = image.createGraphics();

        // Set rendering hints for high quality
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // Draw the first box (red)
        g.setColor(Color.decode("#E1232E"));
        g.fillRect(0, 0, width, 50);

        BufferedImage khqrLogo = readImage();
        // Draw image on the rectangle
        int imageX = width/2 - 35 ; // Adjust the position of the image within the rectangle
        int imageY = 15; // Adjust the position of the image within the rectangle
        int imageWidth = 70; // Adjust the width of the image to fit within the rectangle
        int imageHeight = 18; // Adjust the height of the image to fit within the rectangle
        g.drawImage(khqrLogo, imageX, imageY, imageWidth, imageHeight, null);

        // Draw the second box (white)
        g.setColor(Color.WHITE);
        g.fillRect(0, 50, width, 80);

        // Draw the triangle
        Polygon triangle = new Polygon();
        triangle.addPoint(width - 30, 50); // Upper-left corner
        triangle.addPoint(width, 50); // Upper-right corner
        triangle.addPoint(width, 80); // Bottom corner
        g.setColor(Color.decode("#E1232E"));
        g.fillPolygon(triangle);

        // Draw the text for the second box
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        String merchantName = "Yan Samreach Merchant";
        String amount = "10,000,000";
        g.drawString(merchantName, 25, 85);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        int textWidth = g.getFontMetrics().stringWidth(amount);
        System.out.println("Amt width = " + textWidth);
        g.drawString(amount, 25, 110);

        String ccy = "KHR";
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        System.out.println("Amt width = " + textWidth);
        g.drawString(ccy, textWidth + 30, 110);

        // Draw the third box
        g.setColor(Color.WHITE);
        g.fillRect(0, 130, width, 310);

        // Draw the QR code placeholder
        g.setColor(Color.GRAY);
        BufferedImage qrImage = generateQRImage(); // This one, we can set as Text String into QR Content
//        g.drawRect(25, 155, 250, 250);
        g.drawImage(qrImage, 25, 155, 250, 250, null);

        // Draw dashed line
        float[] dashPattern = {5, 5}; // Specify the dash pattern (5 pixels solid, 5 pixels empty)
        BasicStroke dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f);
        g.setStroke(dashedStroke);
        g.setColor(Color.BLACK);
        g.drawLine(0, 130, width, 130);

        // Dispose of the graphics context
        g.dispose();

        // Save the image to a file
        // Apply rounded corners effect
        image = makeRoundedCorner(image);
//        image = resizeImage(image, 1080, 1560);

        String qrBase64 = "";
        try {
//            File outputFile = new File(DIR + "image.png");
//            ImageIO.write(image, "png", outputFile);
//            System.out.println("Image created successfully.");

            // Convert BufferedImage to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            qrBase64 = Base64.encodeBase64String(outputStream.toByteArray());
        } catch (IOException e) {
            log.error("Exception occurred:", e);
        }

        return qrBase64;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return resizedImage;
    }

    private BufferedImage generateQRImage() {
        BufferedImage qrImage = null;
        String text = "00020101021129700016ftcckhppxxx@ftcc01123000143723970230Foregin Trade Bank of Cambodia5204599953038405802KH5912YAN SAMREACH6010Phnom Penh99170013169642527385563046029";
        int width = 500; // Width of the QR code image
        int height = 500; // Height of the QR code image

        // Configure QR code encoding parameters
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); // Set margin to 0

        // Generate QR code matrix
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            // Write the QR code matrix to a PNG image file
            qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            System.out.println("QR code generated successfully.");
        } catch (Exception e) {
            log.error("Exception occurred:", e);
        }
        return qrImage;
    }

    public BufferedImage readImage() {
        BufferedImage image = null;
        try {
            System.out.println("ASSET:: " + ASSET_DIR);
            // Load the image
            image = ImageIO.read(new File(ASSET_DIR + "KHQRLogo.png")); // the path to your image file
        } catch (IOException e) {
            log.error("Exception occurred:", e);
        }
        return image;
    }

    private BufferedImage makeRoundedCorner(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return output;
    }

}