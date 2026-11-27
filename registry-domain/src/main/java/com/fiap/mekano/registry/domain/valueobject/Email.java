package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value Object de email. Normaliza para lowercase para evitar duplicidade
 * causada apenas por caixa.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new AppException(400, Messages.get("email.invalid.format", value == null ? "null" : value.strip()));
        }
        String trimmed = value.strip();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new AppException(400, Messages.get("email.invalid.format", trimmed));
        }
        this.value = trimmed.toLowerCase(Locale.ROOT);
    }
}
