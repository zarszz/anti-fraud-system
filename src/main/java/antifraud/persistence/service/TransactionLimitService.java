package antifraud.persistence.service;

import antifraud.constant.TransactionLimitType;
import antifraud.persistence.model.TransactionLimit;
import antifraud.persistence.repository.TransactionLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class TransactionLimitService {
    private final TransactionLimitRepository transactionLimitRepository;

    @Value("${trx_initial_limit_allowed_value}")
    private int trxInitialALlowedLimitValue;

    @Value("${trx_initial_limit_manual_value}")
    private int trxInitialManualLimitValue;

    @Autowired
    public TransactionLimitService(TransactionLimitRepository transactionLimitRepository) {
        this.transactionLimitRepository = transactionLimitRepository;
    }

    @Transactional
    public TransactionLimit getLatestTransactionLimit(TransactionLimitType transactionLimitType) {
        var trxLimit =  transactionLimitRepository.findTopByOrderByIdDescWhereTransactionLimitType(
                transactionLimitType
        );
        if (trxLimit.isEmpty()) {
            var newTrxLimit = new TransactionLimit();
            newTrxLimit.setTransactionLimitType(transactionLimitType);
            if (transactionLimitType.equals(TransactionLimitType.ALLOWED)) {
                newTrxLimit.setLimit(trxInitialALlowedLimitValue);
            }
            if (transactionLimitType.equals(TransactionLimitType.MANUAL)) {
                newTrxLimit.setLimit(trxInitialManualLimitValue);
            }
            return transactionLimitRepository.saveAndFlush(newTrxLimit);
        }
        return trxLimit.get(0);
    }

    @Transactional
    public TransactionLimit createNewTransactionLimit(double newLimit, TransactionLimitType transactionLimitType) {
        var trxLimit = new TransactionLimit();
        trxLimit.setLimit(newLimit);
        trxLimit.setTransactionLimitType(transactionLimitType);
        return transactionLimitRepository.saveAndFlush(trxLimit);
    }
}
