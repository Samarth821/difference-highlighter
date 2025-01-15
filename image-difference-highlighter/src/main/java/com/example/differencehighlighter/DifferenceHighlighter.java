package com.example.differencehighlighter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class DifferenceHighlighter {
    static {
        try {
            // Set the Java library path to OpenCV's native library directory
            System.setProperty("java.library.path", "C:\\opencv\\build\\java\\x64");

            // Reload the library path by loading OpenCV
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Paths to input images
        String imagePath1 = "C:\\Users\\Samarth\\Downloads\\A.png";
        String imagePath2 = "C:\\Users\\Samarth\\Downloads\\B.png";
        String outputPath = "C:\\Users\\Samarth\\Downloads\\Output.png";

        // Process both images
        processImages(imagePath1, imagePath2, outputPath);
    }

    private static void processImages(String image1, String image2, String outputImage) {
        // Read the images using OpenCV
        Mat img1 = Imgcodecs.imread(image1);
        Mat img2 = Imgcodecs.imread(image2);

        // Check if images are loaded properly
        if (img1.empty() || img2.empty()) {
            System.out.println("Error: Unable to load images.");
            return;
        }

        // Extract text from both images using Tesseract
        String text1 = extractTextWithTesseract(image1);
        String text2 = extractTextWithTesseract(image2);

        System.out.println("Extracted Text from Image 1:\n" + text1);
        System.out.println("Extracted Text from Image 2:\n" + text2);

        // Detect differences (handwritten text areas) in the second image
        List<Rect> handwrittenAreas = detectHandwrittenText(img2);

        // Draw bounding boxes around detected handwritten areas (highlight differences)
        for (Rect rect : handwrittenAreas) {
            Imgproc.rectangle(img2, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 3); // Highlight differences in green
        }

        // Save the output image with highlighted differences
        Imgcodecs.imwrite(outputImage, img2);
        System.out.println("Differences highlighted in: " + outputImage);
    }

    // Method to extract text from an image using Tesseract OCR
    private static String extractTextWithTesseract(String imagePath) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        try {
            return tesseract.doOCR(new File(imagePath));
        } catch (TesseractException e) {
            System.err.println("Error extracting text: " + e.getMessage());
            return "";
        }
    }

    // Method to detect handwritten text areas using OpenCV
    private static List<Rect> detectHandwrittenText(Mat img) {
        // Convert the image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // Perform GaussianBlur to reduce noise and smooth the image
        Imgproc.GaussianBlur(gray, gray, new org.opencv.core.Size(5, 5), 0);

        // Perform Canny edge detection to detect edges (potential handwritten text)
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 50, 150);

        // Find contours in the image to detect areas of handwritten text
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // List to hold bounding rectangles around detected areas
        List<Rect> handwrittenAreas = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            // Filter small noise and keep significant contours
            if (rect.height > 20 && rect.width > 20) { // Adjust the thresholds as necessary
                handwrittenAreas.add(rect);
            }
        }

        // Ensure that newly added text and smaller variations are also detected
        List<Rect> finalAreas = new ArrayList<>();
        for (Rect rect : handwrittenAreas) {
            if (rect.width > 30 || rect.height > 30) {
                finalAreas.add(rect);
            }
        }

        return finalAreas;
    }
}