package com.myproject.crispysystem.common.util;

import java.math.BigDecimal;

public class ValidationUtil {
    public static void validateNotNull(Object object, String message) {
        if (object == null) throw new IllegalArgumentException(message);
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}

