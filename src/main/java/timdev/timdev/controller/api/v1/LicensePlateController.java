package timdev.timdev.controller.api.v1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

// import org.apache.http.client.config.RequestConfig;

import lombok.extern.slf4j.Slf4j;

import timdev.timdev.dto.api.PlateDetectionResult;
import timdev.timdev.exception.UnauthorizedException;

@RestController
@RequestMapping("/api/v1/plates")
@Slf4j
public class LicensePlateController {

    private final RestTemplate restTemplate;

    // FastAPI service configuration
    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    @Value("${fastapi.timeout:30000}")
    private int fastApiTimeout;

    public LicensePlateController() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(fastApiTimeout))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(fastApiTimeout))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        this.restTemplate = new RestTemplate(factory);
    }

    @PostMapping("/detect")
    public ResponseEntity<?> detectLicensePlate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("image") MultipartFile image) {
        try {
            if (authHeader != null && !authHeader.isEmpty()) {
                validateToken(authHeader);
            }

            log.info("📸 Received image: {}", image.getOriginalFilename());

            // Create temp file safely
            Path tempFile = Files.createTempFile("plate_", ".jpg");
            Files.copy(image.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("💾 Temporary file created: {}", tempFile.toAbsolutePath());

            try {
                // Send to FastAPI
                PlateDetectionResult detectionResult = sendToFastApi(tempFile.toFile());

                String permanentPath = saveImagePermanently(image);

                Map<String, Object> response = new HashMap<>();
                response.put("success", detectionResult.isSuccess());
                response.put("plates_detected", detectionResult.getPlatesDetected());
                response.put("plates_read", detectionResult.getPlatesRead());
                response.put("results", detectionResult.getResults());
                response.put("processing_time_ms", detectionResult.getProcessingTimeMs());
                response.put("message", detectionResult.getMessage());
                response.put("image_path", permanentPath);

                if (detectionResult.getAnnotatedImageUrl() != null) {
                    response.put("annotated_image_url", "/api/plates/annotated/" +
                            detectionResult.getAnnotatedImageFileName());
                    downloadAnnotatedImage(
                            detectionResult.getAnnotatedImageUrl(),
                            detectionResult.getAnnotatedImageFileName());
                }

                log.info("✅ Detection complete: {} plates found", detectionResult.getPlatesDetected());
                return ResponseEntity.ok(response);

            } finally {
                // Delete temp file
                Files.deleteIfExists(tempFile);
                log.debug("🧹 Temp file deleted");
            }

        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("❌ Detection failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to detect license plate: " + e.getMessage()));
        }
    }

    /**
     * Send image to FastAPI for license plate detection
     */
    private PlateDetectionResult sendToFastApi(File imageFile) throws IOException {
        log.info("📤 Sending image to FastAPI at: {}/detect", fastApiUrl);

        // Create multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap();
        body.add("file", new FileSystemResource(imageFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Set timeout
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(fastApiTimeout))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(fastApiTimeout))
                .build();

        ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory())
                .setConnectTimeout(fastApiTimeout);

        try {
            // Make request to FastAPI
            ResponseEntity<Map> response = restTemplate.exchange(
                    fastApiUrl + "/detect",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            log.info("📥 FastAPI response status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body_response = response.getBody();
                return PlateDetectionResult.fromMap(body_response);
            } else {
                throw new RuntimeException("FastAPI returned error: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            log.error("❌ Cannot connect to FastAPI at {}. Make sure the server is running.", fastApiUrl);
            throw new RuntimeException("License plate detection service is not available. " +
                    "Please ensure the FastAPI server is running at: " + fastApiUrl, e);
        }
    }

    /**
     * Download annotated image from FastAPI
     */
    private void downloadAnnotatedImage(String imageUrl, String fileName) {
        try {
            String fullUrl = fastApiUrl + imageUrl;
            log.info("📥 Downloading annotated image from: {}", fullUrl);

            byte[] imageBytes = restTemplate.getForObject(fullUrl, byte[].class);

            if (imageBytes != null) {
                String projectDir = System.getProperty("user.dir");
                String annotatedDir = projectDir + File.separator + "annotated";

                File directory = new File(annotatedDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String filePath = annotatedDir + File.separator + fileName;
                Files.write(Paths.get(filePath), imageBytes);

                log.info("✅ Annotated image saved to: {}", filePath);
            }
        } catch (Exception e) {
            log.error("❌ Failed to download annotated image: {}", e.getMessage());
        }
    }

    /**
     * Save image permanently in your Spring app
     */
    private String saveImagePermanently(MultipartFile image) throws IOException {
        String projectDir = System.getProperty("user.dir");
        String uploadDir = projectDir + File.separator + "uploads";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "plate_" + System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().substring(0, 8) + ".jpg";

        String filePath = uploadDir + File.separator + fileName;
        File dest = new File(filePath);
        image.transferTo(dest);

        log.info("💾 Image saved permanently: {}", filePath);

        return filePath;
    }

    /**
     * Endpoint to serve annotated images
     */
    @GetMapping("/annotated/{fileName}")
    public ResponseEntity<Resource> getAnnotatedImage(@PathVariable String fileName) {
        try {
            String projectDir = System.getProperty("user.dir");
            String filePath = projectDir + File.separator + "annotated" + File.separator + fileName;

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Failed to serve annotated image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint for FastAPI connection
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    fastApiUrl + "/",
                    Map.class);

            return ResponseEntity.ok(Map.of(
                    "spring_status", "healthy",
                    "fastapi_status", response.getStatusCode() == HttpStatus.OK ? "connected" : "error",
                    "fastapi_response", response.getBody()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "spring_status", "healthy",
                            "fastapi_status", "disconnected",
                            "error", e.getMessage()));
        }
    }

    private void validateToken(String authHeader) {
        // Your token validation logic here
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid token");
        }
    }
}