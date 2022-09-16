package antifraud.persistence.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "stolen_card", uniqueConstraints = {
    @UniqueConstraint(columnNames = "number")
})
public class StolenCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String number;
}
