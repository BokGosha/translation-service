package tbank.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tbank.project.model.Translation;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
}
