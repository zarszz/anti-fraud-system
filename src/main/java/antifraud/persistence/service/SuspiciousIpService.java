package antifraud.persistence.service;

import antifraud.constant.PatternUtils;
import antifraud.controller.exception.DataExistException;
import antifraud.dto.request.CreateSuspiciousIpDto;
import antifraud.persistence.model.SuspiciousIp;
import antifraud.persistence.repository.SuspiciousIpRepository;
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
public class SuspiciousIpService {
    private final SuspiciousIpRepository suspiciousIpRepository;

    @Autowired
    public SuspiciousIpService(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    public SuspiciousIp save(CreateSuspiciousIpDto createSuspiciousIpDto) {
        var ip = suspiciousIpRepository.findByIp(createSuspiciousIpDto.getIp());
        if (ip.isPresent()) throw new DataExistException("Suspicious IP already exists");

        var suspiciousIp = new SuspiciousIp();
        suspiciousIp.setIp(createSuspiciousIpDto.getIp());
        return suspiciousIpRepository.saveAndFlush(suspiciousIp);
    }

    public void delete(String ip) {
        var isMatch = Pattern.matches(PatternUtils.IP_ADDRESS_PATTERN, ip);
        if (!isMatch) throw new ValidationException("Invalid IP address");
        var ipData = suspiciousIpRepository.findByIp(ip);
        if (ipData.isEmpty()) throw new NoSuchElementException("IP address not found");
        suspiciousIpRepository.delete(ipData.get());
    }

    public List<SuspiciousIp> getAll() {
        return suspiciousIpRepository
                .findAll()
                .stream()
                .sorted(Comparator.comparing(SuspiciousIp::getIp))
                .collect(Collectors.toList());
    }

    public Optional<SuspiciousIp> findByIp(String ip) {
        return suspiciousIpRepository.findByIp(ip);
    }
}
