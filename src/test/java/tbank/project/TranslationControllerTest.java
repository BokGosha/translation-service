package tbank.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;
import tbank.project.controller.TranslationController;
import tbank.project.dto.TranslationResponse;
import tbank.project.service.TranslationService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TranslationController.class)
public class TranslationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @Test
    public void testTranslateWords() throws Exception {
        String text = "Hello, world!";
        String sourceLanguage = "en";
        String targetLanguage = "ru";
        String translatedText = "Привет, мир!";

        TranslationResponse response = new TranslationResponse();
        response.setText(translatedText);

        when(translationService.giveTranslation(anyString(), eq(text), eq(sourceLanguage), eq(targetLanguage)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/translate")
                        .param("words", text)
                        .param("sourceLanguage", sourceLanguage)
                        .param("targetLanguage", targetLanguage))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(translatedText));
    }

    @Test
    public void testTranslateWordsWithInvalidSourceLanguage() throws Exception {
        String text = "Hello, world!";
        String sourceLanguage = "xx";
        String targetLanguage = "ru";

        when(translationService.giveTranslation(anyString(), eq(text), eq(sourceLanguage), eq(targetLanguage))).
                thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Язык источника " + sourceLanguage +
                        " не поддерживается Google Translate."));

        mockMvc.perform(MockMvcRequestBuilders.get("/translate")
                        .param("words", text)
                        .param("sourceLanguage", sourceLanguage)
                        .param("targetLanguage", targetLanguage)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Язык источника " + sourceLanguage +
                        " не поддерживается Google Translate."));
    }

    @Test
    public void testTranslateWordsWithInvalidTargetLanguage() throws Exception {
        String text = "Hello, world!";
        String sourceLanguage = "en";
        String targetLanguage = "yy";

        when(translationService.giveTranslation(anyString(), eq(text), eq(sourceLanguage), eq(targetLanguage))).
                thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Язык перевода " + targetLanguage +
                        " не поддерживается Google Translate."));

        mockMvc.perform(MockMvcRequestBuilders.get("/translate")
                        .param("words", text)
                        .param("sourceLanguage", sourceLanguage)
                        .param("targetLanguage", targetLanguage)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Язык перевода " + targetLanguage +
                        " не поддерживается Google Translate."));
    }
}
