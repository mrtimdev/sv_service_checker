package timdev.timdev.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import timdev.timdev.dto.ExternalDriverDTO;

@Service
public class DriverProxyService {
    
    private static final String API_URL = "https://svtms.svtrucking.biz/api/v1/integrations/drivers/search";
    private static final String API_KEY = "1234567890abcdef";

    private final RestTemplate restTemplate;

    public DriverProxyService() {
        this.restTemplate = new RestTemplate();
    }



    /**
     * Get a single driver by external ID
     */
    public ExternalDriverDTO getDriverById(Long externalDriverId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", API_KEY);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = API_URL + "?id=" + externalDriverId;
        ResponseEntity<ExternalDriverDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, ExternalDriverDTO.class);

        return response.getBody();
    }

    /**
     * Search drivers by term for Select2
     */
    public List<ExternalDriverDTO> searchDrivers(String term) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", API_KEY);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        String url = API_URL + "?term=" + term;
        ResponseEntity<ExternalDriverDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, request, ExternalDriverDTO[].class);

        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }
}
