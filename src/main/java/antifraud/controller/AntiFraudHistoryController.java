package antifraud.controller;

import antifraud.persistence.service.TransactionService;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/antifraud/history")
@Validated
public class AntiFraudHistoryController {
    private final TransactionService transactionService;

    @Autowired
    public AntiFraudHistoryController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/{number}")
    public ResponseEntity<?> getAllByNumber(@PathVariable("number") @LuhnCheck String number) {
        return ResponseEntity.ok(transactionService.getAllByNumber(number));
    }
}
