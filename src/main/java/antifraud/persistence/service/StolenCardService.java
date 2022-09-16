package antifraud.persistence.service;

import antifraud.constant.PatternUtils;
import antifraud.controller.exception.DataExistException;
import antifraud.dto.request.CreateStolenCardDto;
import antifraud.persistence.model.StolenCard;
import antifraud.persistence.repository.StolenCardRepository;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StolenCardService {
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public StolenCard save(CreateStolenCardDto createStolenCardDto) {

        var card = stolenCardRepository.findByNumber(createStolenCardDto.getNumber());
        if (card.isPresent()) throw new DataExistException("Stolen card already exists");

        var StolenCard = new StolenCard();
        StolenCard.setNumber(createStolenCardDto.getNumber());
        return stolenCardRepository.saveAndFlush(StolenCard);
    }

    public void delete(String cardNumber) {
        var isMatch = LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(cardNumber);
        if (!isMatch) throw new ValidationException("Invalid card number");
        var cardData = stolenCardRepository.findByNumber(cardNumber);
        if (cardData.isEmpty()) throw new NoSuchElementException("card number not found");
        stolenCardRepository.delete(cardData.get());
    }

    public List<StolenCard> getAll() {
        return stolenCardRepository
                .findAll()
                .stream()
                .sorted(Comparator.comparing(StolenCard::getId))
                .collect(Collectors.toList());
    }

    public Optional<StolenCard> findByNumber(String number) {
        return stolenCardRepository.findByNumber(number);
    }
}
