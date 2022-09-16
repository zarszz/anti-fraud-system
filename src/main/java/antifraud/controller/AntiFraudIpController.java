package antifraud.controller;

import antifraud.dto.request.CreateSuspiciousIpDto;
import antifraud.dto.response.DeleteSuspiciousDataResponseDto;
import antifraud.persistence.service.SuspiciousIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class AntiFraudIpController {
    private final SuspiciousIpService suspiciousIpService;

    @Autowired
    public AntiFraudIpController(SuspiciousIpService suspiciousIpService) {
        this.suspiciousIpService = suspiciousIpService;
    }

    @PostMapping
    public ResponseEntity<?> addSuspiciousIp(@RequestBody @Valid CreateSuspiciousIpDto createSuspiciousIpDto) {
        return ResponseEntity.ok(suspiciousIpService.save(createSuspiciousIpDto));
    }

    @DeleteMapping("/{ip}")
    public ResponseEntity<?> deleteSuspiciousIp(@PathVariable String ip) {
        suspiciousIpService.delete(ip);
        var response = new DeleteSuspiciousDataResponseDto("IP", ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(suspiciousIpService.getAll());
    }
}
