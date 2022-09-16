package antifraud.controller;

import antifraud.dto.request.CreateStolenCardDto;
import antifraud.dto.response.DeleteSuspiciousDataResponseDto;
import antifraud.persistence.service.StolenCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/antifraud/stolencard")
public class AntiFraudCardController {
    private final StolenCardService stolenCardService;

    @Autowired
    public AntiFraudCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping
    public ResponseEntity<?> addStolenCard(@RequestBody @Valid CreateStolenCardDto createStolenCardDto) {
        return ResponseEntity.ok(stolenCardService.save(createStolenCardDto));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<?> deleteStolenCard(@PathVariable String number) {
        stolenCardService.delete(number);
        var response = new DeleteSuspiciousDataResponseDto("Card", number);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(stolenCardService.getAll());
    }
}
