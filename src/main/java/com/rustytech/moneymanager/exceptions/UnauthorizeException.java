package com.rustytech.moneymanager.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class UnauthorizeException extends RuntimeException {
    private final String message;
}
