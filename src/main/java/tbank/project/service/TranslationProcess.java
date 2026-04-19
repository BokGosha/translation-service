package tbank.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class TranslationProcess {

    private static final String TRANSLATE_URL =
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl={sl}&tl={tl}&dt=t&q={q}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService translationExecutor;

    public String translate(String text, String sourceLanguage, String targetLanguage) {
        String[] words = text.split("\\s+");

        CompletableFuture<String>[] futures = new CompletableFuture[words.length];
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            futures[i] = CompletableFuture.supplyAsync(
                    () -> translateWord(word, sourceLanguage, targetLanguage),
                    translationExecutor);
        }

        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ResponseStatusException rse) {
                throw rse;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Ошибка при переводе", cause);
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < futures.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(futures[i].join());
        }
        return result.toString();
    }

    private String translateWord(String word, String sourceLanguage, String targetLanguage) {
        if (word.isEmpty()) {
            return "";
        }

        String response;
        try {
            response = restTemplate.getForObject(TRANSLATE_URL, String.class, sourceLanguage, targetLanguage, word);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Сервис перевода недоступен", e);
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode translated = rootNode.get(0).get(0).get(0);
            return translated.asText();
        } catch (JsonProcessingException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Некорректный ответ от сервиса перевода", e);
        }
    }
}
