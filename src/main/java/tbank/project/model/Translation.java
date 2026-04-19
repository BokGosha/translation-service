package tbank.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "translations")
@Getter
@Setter
@ToString
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientIp;
    private String originalText;
    private String translatedText;
}
