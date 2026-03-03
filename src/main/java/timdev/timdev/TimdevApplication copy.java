// package timdev.timdev;

// import java.awt.Color;
// import java.util.List;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// import jakarta.annotation.PostConstruct;
// import static marvin.MarvinPluginCollection.findTextRegions;
// import marvin.image.MarvinImage;
// import marvin.image.MarvinSegment;
// import marvin.io.MarvinImageIO;

// @SpringBootApplication(scanBasePackages = "timdev.timdev.controller")
// @EntityScan(basePackages = "timdev.timdev.entity")
// @EnableJpaRepositories(basePackages = "timdev.timdev.repository")
// @ComponentScan(basePackages = "timdev.timdev")
// public class TimdevApplication {

//     private static final int Gamma = 128; // edge threshold

//     public static void main(String[] args) {
//         SpringApplication.run(TimdevApplication.class, args);
//     }

//     @PostConstruct
//     public void processImage() {
//         try {
//             // Load image using Marvin
//             MarvinImage image = MarvinImageIO.loadImage("uploads/image.png");

//             // Convert to grayscale
//             image = convertToGrayscale(image);

//             // Apply edge detection (optional, enhances text detection)
//             int[][] greys = marvinImageToArray(image);
//             int[][] edges = detectEdges(greys);
//             image = arrayToMarvinImage(edges);

//             // Detect text regions and draw rectangles
//             image = findText(image, 30, 20, 100, 170);

//             // Save output
//             MarvinImageIO.saveImage(image, "uploads/vehicle_out.png");

//             System.out.println("Processing complete. Output saved to uploads/vehicle_out.png");

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }

//     /** Convert MarvinImage to grayscale */
//     private MarvinImage convertToGrayscale(MarvinImage image) {
//         for (int x = 0; x < image.getWidth(); x++) {
//             for (int y = 0; y < image.getHeight(); y++) {
//                 int R = image.getIntComponent0(x, y);
//                 int G = image.getIntComponent1(x, y);
//                 int B = image.getIntComponent2(x, y);
//                 int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
//                 image.setIntColor(x, y, 255, gray, gray, gray);
//             }
//         }
//         return image;
//     }

//     /** Convert MarvinImage to 2D int array */
//     private int[][] marvinImageToArray(MarvinImage image) {
//         int width = image.getWidth();
//         int height = image.getHeight();
//         int[][] greys = new int[width][height];
//         for (int x = 0; x < width; x++) {
//             for (int y = 0; y < height; y++) {
//                 greys[x][y] = image.getIntComponent0(x, y); // grayscale
//             }
//         }
//         return greys;
//     }

//     /** Convert 2D int array to MarvinImage */
//     private MarvinImage arrayToMarvinImage(int[][] array) {
//         int width = array.length;
//         int height = array[0].length;
//         MarvinImage image = new MarvinImage(width, height);
//         for (int x = 0; x < width; x++) {
//             for (int y = 0; y < height; y++) {
//                 int gray = array[x][y];
//                 image.setIntColor(x, y, 255, gray, gray, gray);
//             }
//         }
//         return image;
//     }

//     /** Simple 1D Edge detection along X-axis */
//     private int[][] detectEdges(int[][] detectionBitmap) {
//         int width = detectionBitmap.length;
//         int height = detectionBitmap[0].length;
//         int[][] edges = new int[width][height];

//         for (int y = 0; y < height; y++) {
//             for (int x = 2; x < width - 2; x++) {
//                 int p0 = detectionBitmap[x][y];
//                 int p1 = detectionBitmap[x - 1][y];
//                 int p2 = detectionBitmap[x + 1][y];
//                 int p3 = detectionBitmap[x - 2][y];
//                 int p4 = detectionBitmap[x + 2][y];

//                 int d0 = Math.abs(p1 + p2 - 2 * p0) + Math.abs(p3 + p4 - 2 * p0);
//                 edges[x][y] = (d0 >= Gamma) ? Gamma : d0;
//             }
//         }
//         return edges;
//     }

//     /** Detect text regions and draw red rectangles */
//     public MarvinImage findText(MarvinImage image, int maxWhiteSpace, int maxFontLineWidth, int minTextWidth, int grayScaleThreshold) {
//         List<MarvinSegment> segments = findTextRegions(image, maxWhiteSpace, maxFontLineWidth, minTextWidth, grayScaleThreshold);

//         for (MarvinSegment s : segments) {
//             if (s.height >= 10) {
//                 int x1 = Math.max(0, s.x1);
//                 int y1 = Math.max(0, s.y1 - 20);
//                 int x2 = Math.min(image.getWidth() - 1, s.x2);
//                 int y2 = Math.min(image.getHeight() - 1, s.y2 + 20);

//                 // Draw 3 nested rectangles
//                 image.drawRect(x1, y1, x2 - x1, y2 - y1, Color.RED);
//                 image.drawRect(x1 + 1, y1 + 1, (x2 - x1) - 2, (y2 - y1) - 2, Color.RED);
//                 image.drawRect(x1 + 2, y1 + 2, (x2 - x1) - 4, (y2 - y1) - 4, Color.RED);
//             }
//         }
//         return image;
//     }
// }
