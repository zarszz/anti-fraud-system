package antifraud.controller;

import antifraud.constant.TransactionResult;
import antifraud.controller.exception.TransactionLockedException;
import antifraud.dto.request.AddTrxFeedbackDto;
import antifraud.dto.request.CreateTransactionDto;
import antifraud.dto.response.CreateTransactionResponseDto;
import antifraud.persistence.service.TransactionService;
import antifraud.persistence.service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/antifraud/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody @Valid CreateTransactionDto createTransactionDto, @AuthenticationPrincipal UserDetails userDetails) {
        var responseDto = transactionService.createTransaction(createTransactionDto, userDetails.getUsername());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping
    public ResponseEntity<?> addFeedback(@RequestBody @Valid AddTrxFeedbackDto addTrxFeedbackDto) {
        var responseDto = transactionService.addFeedback(addTrxFeedbackDto);
        return ResponseEntity.ok(responseDto);
    }
}
