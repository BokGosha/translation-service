package tbank.project.dto;

import lombok.Data;
import tbank.project.model.Translation;

@Data
public class TranslationResponse {

    private String text;

    public static TranslationResponse from(Translation translation) {
        TranslationResponse translationResponse = new TranslationResponse();
        translationResponse.setText(translation.getTranslatedText());
        return translationResponse;
    }
}
