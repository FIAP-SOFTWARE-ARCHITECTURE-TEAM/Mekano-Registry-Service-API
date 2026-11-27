package com.fiap.mekano.registry.domain.valueobject;

import com.fiap.mekano.registry.domain.exception.AppException;
import com.fiap.mekano.registry.domain.exception.Messages;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object que encapsula e valida um CPF brasileiro.
 *
 * <ul>
 *   <li>Normaliza para apenas digitos.</li>
 *   <li>Exige 11 digitos e rejeita sequencias repetidas.</li>
 *   <li>Valida os digitos verificadores (modulo 11).</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode
@ToString
public final class Cpf {

    private final String value;

    public Cpf(String value) {
        if (value == null || value.isBlank()) {
            throw new AppException(400, Messages.get("cpf.invalid", value == null ? "null" : value.strip()));
        }
        String digits = value.strip().replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new AppException(400, Messages.get("cpf.invalid", value.strip()));
        }
        if (digits.chars().distinct().count() == 1) {
            throw new AppException(400, Messages.get("cpf.invalid", value.strip()));
        }
        if (!isValidCheckDigits(digits)) {
            throw new AppException(400, Messages.get("cpf.invalid", value.strip()));
        }
        this.value = digits;
    }

    private static boolean isValidCheckDigits(String digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digits.charAt(i) - '0') * (10 - i);
        }
        int first = 11 - (sum % 11);
        if (first >= 10) {
            first = 0;
        }
        if (first != (digits.charAt(9) - '0')) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (digits.charAt(i) - '0') * (11 - i);
        }
        int second = 11 - (sum % 11);
        if (second >= 10) {
            second = 0;
        }
        return second == (digits.charAt(10) - '0');
    }
}
