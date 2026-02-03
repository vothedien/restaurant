package com.restaurant.service.admin;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.exception.BusinessRuleException;

@Service
public class ImgBbService {

    @Value("${imgbb.apiKey}")
    private String apiKey;

    @Value("${imgbb.useDisplayUrl:true}")
    private boolean useDisplayUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public ImgBbService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File ảnh rỗng.");
        }

        long maxBytes = 32L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException("Ảnh vượt quá 32MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessRuleException("Chỉ cho phép upload file ảnh (image/*).");
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("image", base64);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new BusinessRuleException("Upload ImgBB thất bại.");
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            boolean success = root.path("success").asBoolean(false);
            if (!success) {
                throw new BusinessRuleException("ImgBB trả về success=false.");
            }

            JsonNode data = root.path("data");
            String displayUrl = data.path("display_url").asText(null);
            String urlDirect = data.path("url").asText(null); 
            String chosen = useDisplayUrl ? displayUrl : urlDirect;
            if (chosen == null || chosen.isBlank()) {
                chosen = (displayUrl != null && !displayUrl.isBlank()) ? displayUrl : urlDirect;
            }
            if (chosen == null || chosen.isBlank()) {
                throw new BusinessRuleException("Không lấy được link ảnh từ ImgBB response.");
            }

            return chosen;

        } catch (Exception e) {
            throw new BusinessRuleException("Upload ImgBB lỗi: " + e.getMessage());
        }
    }
}
