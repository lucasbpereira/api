package br.app.moments.api.messages;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final HttpServletRequest request;

    public MessageService(MessageSource messageSource, LocaleResolver localeResolver, HttpServletRequest request) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
        this.request = request;
    }

    public String getMessage(String key, Object... args) {
        Locale locale = localeResolver.resolveLocale(request); // Obtém o Locale do usuário
        return messageSource.getMessage(key, args, locale);
    }
}
