package timdev.timdev.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import timdev.timdev.dto.ExternalDriverDTO;
import timdev.timdev.service.DriverProxyService;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/drivers")
public class DriverProxyController {

    private final DriverProxyService driverProxyService;

    /**
     * Proxy endpoint to search drivers by term
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExternalDriverDTO>> searchDrivers(@RequestParam String term) {
        List<ExternalDriverDTO> drivers = driverProxyService.searchDrivers(term);
        return ResponseEntity.ok(drivers);
    }
}