package tbank.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tbank.project.dto.TranslationResponse;
import tbank.project.service.TranslationService;

@RestController
@Validated
@RequiredArgsConstructor
public class TranslationController {

    private static final int MAX_TEXT_LENGTH = 5000;

    private final TranslationService translationService;

    @GetMapping("/translate")
    public ResponseEntity<TranslationResponse> translateWords(
            @RequestParam("words") @NotBlank @Size(max = MAX_TEXT_LENGTH) String text,
            @RequestParam("sourceLanguage") @NotBlank String sourceLanguage,
            @RequestParam("targetLanguage") @NotBlank String targetLanguage,
            HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();
        TranslationResponse response = translationService.giveTranslation(clientIp, text, sourceLanguage, targetLanguage);
        return ResponseEntity.ok(response);
    }
}
