package com.fiap.mekano.registry.domain.exception;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Acesso as mensagens de dominio (ResourceBundle {@code messages.properties}).
 */
public final class Messages {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages");

    private Messages() {
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(BUNDLE.getString(key), args);
    }
}
