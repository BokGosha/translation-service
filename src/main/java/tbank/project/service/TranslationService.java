package tbank.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tbank.project.dto.TranslationResponse;
import tbank.project.model.Translation;
import tbank.project.repository.TranslationRepository;
import tbank.project.util.SupportedLanguages;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationProcess translationProcess;
    private final TranslationRepository translationRepository;

    public TranslationResponse giveTranslation(String clientIp, String text, String sourceLanguage, String targetLanguage) {
        checkSupportedLanguages(sourceLanguage, targetLanguage);

        String translatedText = translationProcess.translate(text, sourceLanguage, targetLanguage);

        Translation translation = new Translation();
        translation.setClientIp(clientIp);
        translation.setOriginalText(text);
        translation.setTranslatedText(translatedText);
        translationRepository.save(translation);

        return TranslationResponse.from(translation);
    }

    private void checkSupportedLanguages(String sourceLanguage, String targetLanguage) {
        if (!SupportedLanguages.supportedLanguage(sourceLanguage)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Язык источника " + sourceLanguage + " не поддерживается Google Translate.");
        }

        if (!SupportedLanguages.supportedLanguage(targetLanguage)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Язык перевода " + targetLanguage + " не поддерживается Google Translate.");
        }
    }
}
