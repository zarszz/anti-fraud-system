package antifraud.persistence.service;

import antifraud.constant.ChangeTrxLimitOperation;
import antifraud.constant.TransactionLimitType;
import antifraud.constant.TransactionResult;
import antifraud.controller.exception.DataExistException;
import antifraud.controller.exception.TransactionLockedException;
import antifraud.controller.exception.UnProcessableEntityException;
import antifraud.dto.request.AddTrxFeedbackDto;
import antifraud.dto.request.CreateTransactionDto;
import antifraud.dto.response.CreateTransactionResponseDto;
import antifraud.dto.response.TrxFeedbackResponseDto;
import antifraud.persistence.model.TransactionHistory;
import antifraud.persistence.repository.TransactionHistoryRepository;
import antifraud.persistence.repository.TransactionLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final UserServices userServices;

    private final SuspiciousIpService suspiciousIpService;

    private final StolenCardService stolenCardService;

    private final TransactionLimitService transactionLimitService;

    private final TransactionHistoryRepository transactionHistoryRepository;

    private final HttpServletRequest httpServletRequest;
    private final TransactionLimitRepository transactionLimitRepository;

    @Autowired
    public TransactionService(
        UserServices userServices,
        SuspiciousIpService suspiciousIpService,
        StolenCardService stolenCardService,
        TransactionLimitService transactionLimitService,
        TransactionHistoryRepository transactionHistoryRepository,
        HttpServletRequest httpServletRequest,
        TransactionLimitRepository transactionLimitRepository) {
        this.userServices = userServices;
        this.suspiciousIpService = suspiciousIpService;
        this.stolenCardService = stolenCardService;
        this.transactionLimitService = transactionLimitService;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.httpServletRequest = httpServletRequest;
        this.transactionLimitRepository = transactionLimitRepository;
    }

    @Transactional
    public CreateTransactionResponseDto createTransaction(CreateTransactionDto createTransactionDto, String username) {
        var user = userServices.loadUserByUsername(username);
        if (!user.isAccountNonLocked()) {
            throw new TransactionLockedException("Transaction failed : user is locked");
        }

        var infos = new ArrayList<String>();

        var transactionHistory = new TransactionHistory();

        var allowedLimit = transactionLimitService.getLatestTransactionLimit(TransactionLimitType.ALLOWED);
        var manualLimit = transactionLimitService.getLatestTransactionLimit(TransactionLimitType.MANUAL);

        if (createTransactionDto.getAmount() <= allowedLimit.getLimit()) {
            transactionHistory.setTransactionResult(TransactionResult.ALLOWED);
        } else if (createTransactionDto.getAmount() > allowedLimit.getLimit() && createTransactionDto.getAmount() <= manualLimit.getLimit()) {
            transactionHistory.setTransactionResult(TransactionResult.MANUAL_PROCESSING);
            if (createTransactionDto.getAmount() != 1000) {
                infos.add("amount");
            }
        } else {
            transactionHistory.setTransactionResult(TransactionResult.PROHIBITED);
            infos.add("amount");
        }
        var countDifferentRegion = 0;
        var countDifferentIp = 0;

        var startDate = createTransactionDto.getDate().minusHours(1);
        var endDate = createTransactionDto.getDate();

        var transactions = transactionHistoryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);

        var allowedTrx = transactions
            .stream()
            .filter(t -> t.getTransactionResult().equals(TransactionResult.ALLOWED))
            .collect(Collectors.toList());

        if (allowedTrx.size() > 0) {
            var lastVerifiedTrx = allowedTrx.get(0);
            var regionMap = new HashMap<String, Object>();
            var ipMap = new HashMap<String, Object>();

            var lastRegion = lastVerifiedTrx.getRegion();
            var lastIP = lastVerifiedTrx.getIp();

            for (var trx : transactions) {
                if (regionMap.containsKey(trx.getRegion())) continue;
                if (ipMap.containsKey(trx.getIp())) continue;

                if (!trx.getRegion().equals(lastRegion)) {
                    regionMap.put(trx.getRegion(), null);
                    countDifferentRegion++;
                }

                if (!trx.getIp().equals(lastIP)) {
                    ipMap.put(trx.getIp(), null);
                    countDifferentIp++;
                }
            }

            var ipDto = createTransactionDto.getIp();
            var regionDto = createTransactionDto.getRegion();

            var lastTrxIp = lastVerifiedTrx.getIp();
            var lastTrxRegion = lastVerifiedTrx.getRegion();

            var isNotSuspiciousIp = transactions
                    .stream()
                    .noneMatch(t -> t.getIp().equalsIgnoreCase(ipDto));
            var isNotSuspiciousRegion = transactions
                    .stream()
                    .noneMatch(t -> t.getRegion().equalsIgnoreCase(regionDto));

            if (!ipDto.equalsIgnoreCase(lastTrxIp) && isNotSuspiciousIp) {
                countDifferentIp++;
            }
            if (!regionDto.equalsIgnoreCase(lastTrxRegion) && isNotSuspiciousRegion) {
                countDifferentRegion++;
            }
        }


        if (countDifferentIp == 2) {
            transactionHistory.setTransactionResult(TransactionResult.MANUAL_PROCESSING);
            infos.add("ip-correlation");
        }

        if (countDifferentRegion == 2) {
            transactionHistory.setTransactionResult(TransactionResult.MANUAL_PROCESSING);
            infos.add("region-correlation");
        }

        if (countDifferentIp > 2) {
            transactionHistory.setTransactionResult(TransactionResult.PROHIBITED);
            infos.add("ip-correlation");
        }

        if (countDifferentRegion > 2) {
            transactionHistory.setTransactionResult(TransactionResult.PROHIBITED);
            infos.add("region-correlation");
        }

        var isSuspiciousIp = suspiciousIpService.findByIp(createTransactionDto.getIp());

        if (isSuspiciousIp.isPresent()) {
            transactionHistory.setTransactionResult(TransactionResult.PROHIBITED);
            infos.add("ip");
        }

        var isStolenCard = stolenCardService.findByNumber(createTransactionDto.getNumber());
        if (isStolenCard.isPresent()) {
            transactionHistory.setTransactionResult(TransactionResult.PROHIBITED);
            infos.add("card-number");
        }

        transactionHistory.setAmount(createTransactionDto.getAmount());
        transactionHistory.setNumber(createTransactionDto.getNumber());
        transactionHistory.setDate(createTransactionDto.getDate());
        transactionHistory.setIp(createTransactionDto.getIp());
        transactionHistory.setRegion(createTransactionDto.getRegion());
        transactionHistoryRepository.save(transactionHistory);

        var responseDto = new CreateTransactionResponseDto();
        responseDto.setResult(transactionHistory.getTransactionResult());

        var sorted = infos.stream().sorted().collect(Collectors.toList());
        responseDto.setInfo(sorted.size() == 0 ? "none" : String.join(", ", sorted));
        return responseDto;
    }

    @Transactional
    public TrxFeedbackResponseDto addFeedback(AddTrxFeedbackDto addTrxFeedbackDto) {
        var feedbackValidity = TransactionResult.valueOf(addTrxFeedbackDto.getFeedback());
        var transactionHistory = transactionHistoryRepository
                .findById(addTrxFeedbackDto.getTransactionId())
                .orElseThrow(() -> new NoSuchElementException("Transaction not found"));
        if (!transactionHistory.getFeedback().equals(TransactionResult.EMPTY)) {
            throw new DataExistException("Feedback already exist");
        }

        if (transactionHistory.getTransactionResult().equals(feedbackValidity)){
            throw new UnProcessableEntityException("Transaction is allowed");
        }

        var currentTrxValidity = transactionHistory.getTransactionResult();
        adjustTransactionLimit(transactionHistory, currentTrxValidity, feedbackValidity);

        transactionHistory.setFeedback(TransactionResult.valueOf(addTrxFeedbackDto.getFeedback()));
        var newTrxHistory = transactionHistoryRepository.saveAndFlush(transactionHistory);
        return TrxFeedbackResponseDto.fromEntity(newTrxHistory);
    }

    public List<TrxFeedbackResponseDto> getAll() {
        return transactionHistoryRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(TrxFeedbackResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TrxFeedbackResponseDto> getAllByNumber(String number) {
        var responses =  transactionHistoryRepository
                .findByNumberOrderByIdAsc(number)
                .stream()
                .map(TrxFeedbackResponseDto::fromEntity)
                .collect(Collectors.toList());
        if (responses.isEmpty()) throw new NoSuchElementException("No trx found for this number");
        return responses;
    }

    private void changeLimit(double trxValue, TransactionLimitType transactionLimitType, ChangeTrxLimitOperation operation) {
        var latestTransactionLimit = transactionLimitService.getLatestTransactionLimit(transactionLimitType);
        var currentLimit = latestTransactionLimit.getLimit();
        double newLimit;
        if (operation.equals(ChangeTrxLimitOperation.INCREASE)) {
            newLimit = increaseLimit(currentLimit, trxValue);
        } else {
            newLimit = decreaseLimit(currentLimit, trxValue);
        }
        transactionLimitService.createNewTransactionLimit(newLimit,transactionLimitType);
    }

    private double increaseLimit(double currentLimit, double trxValue) {
        var newLimit = (0.8 * currentLimit) + (0.2 * trxValue);
        return Double.valueOf(Math.ceil(newLimit)).intValue();
    }

    private int decreaseLimit(double currentLimit, double trxValue) {
        var newLimit = (0.8 * currentLimit) - (0.2 * trxValue);
        return Double.valueOf(Math.ceil(newLimit)).intValue();
    }

    private void adjustTransactionLimit(TransactionHistory transactionHistory, TransactionResult currentTrxValidity, TransactionResult feedbackValidity) {
        if (currentTrxValidity.equals(TransactionResult.ALLOWED) && feedbackValidity.equals(TransactionResult.MANUAL_PROCESSING)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.ALLOWED, ChangeTrxLimitOperation.DECREASE);
        }

        if (currentTrxValidity.equals(TransactionResult.ALLOWED) && feedbackValidity.equals(TransactionResult.PROHIBITED)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.ALLOWED, ChangeTrxLimitOperation.DECREASE);
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.MANUAL, ChangeTrxLimitOperation.DECREASE);
        }

        if (currentTrxValidity.equals(TransactionResult.MANUAL_PROCESSING) && feedbackValidity.equals(TransactionResult.ALLOWED)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.ALLOWED, ChangeTrxLimitOperation.INCREASE);
        }

        if (currentTrxValidity.equals(TransactionResult.MANUAL_PROCESSING) && feedbackValidity.equals(TransactionResult.PROHIBITED)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.MANUAL, ChangeTrxLimitOperation.DECREASE);
        }

        if (currentTrxValidity.equals(TransactionResult.PROHIBITED) && feedbackValidity.equals(TransactionResult.ALLOWED)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.ALLOWED, ChangeTrxLimitOperation.INCREASE);
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.MANUAL, ChangeTrxLimitOperation.INCREASE);
        }

        if (currentTrxValidity.equals(TransactionResult.PROHIBITED) && feedbackValidity.equals(TransactionResult.MANUAL_PROCESSING)) {
            changeLimit(transactionHistory.getAmount(), TransactionLimitType.MANUAL, ChangeTrxLimitOperation.INCREASE);
        }
    }
}
