package antifraud.persistence.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "suspicious_ip", uniqueConstraints = {
    @UniqueConstraint(columnNames = "ip")
})
public class SuspiciousIp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String ip;
}
