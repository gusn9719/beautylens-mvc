package kr.ac.kopo.face.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FacePythonClient {

    private static final String BASE_URL = "http://127.0.0.1:8090";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EnrollResult enroll(int memberId, List<String> images) {
        Map<String, Object> body = new HashMap<>();
        body.put("memberId", memberId);
        body.put("images", images);
        JsonNode node = post("/face/enroll", body);
        EnrollResult result = new EnrollResult();
        result.embeddingJson = node.get("embedding").toString();
        result.modelName = node.path("modelName").asText();
        result.validImageCount = node.path("validImageCount").asInt();
        result.message = node.path("message").asText();
        return result;
    }

    public VerifyResult verify(String image, String storedEmbeddingJson) {
        try {
            JsonNode embedding = objectMapper.readTree(storedEmbeddingJson);
            Map<String, Object> body = new HashMap<>();
            body.put("image", image);
            body.put("storedEmbedding", embedding);
            JsonNode node = post("/face/verify", body);
            VerifyResult result = new VerifyResult();
            result.verified = node.path("verified").asBoolean(false);
            result.similarity = node.path("similarity").asDouble(0.0);
            result.threshold = node.path("threshold").asDouble(0.0);
            result.modelName = node.path("modelName").asText();
            result.message = node.path("message").asText();
            return result;
        } catch (JsonProcessingException e) {
            throw new FaceAuthException(500, "stored face embedding is invalid");
        }
    }

    private JsonNode post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = parse(response.body());
            if (response.statusCode() >= 400) {
                String message = node.has("detail") ? node.get("detail").asText() : "face server request failed";
                throw new FaceAuthException(response.statusCode(), message);
            }
            return node;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new FaceAuthException(503, "face authentication server is unavailable");
        }
    }

    private JsonNode parse(String body) throws JsonProcessingException {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(body);
    }

    public static class EnrollResult {
        private String embeddingJson;
        private String modelName;
        private int validImageCount;
        private String message;

        public String getEmbeddingJson() { return embeddingJson; }
        public String getModelName() { return modelName; }
        public int getValidImageCount() { return validImageCount; }
        public String getMessage() { return message; }
    }

    public static class VerifyResult {
        private boolean verified;
        private double similarity;
        private double threshold;
        private String modelName;
        private String message;

        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        public double getSimilarity() { return similarity; }
        public void setSimilarity(double similarity) { this.similarity = similarity; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
