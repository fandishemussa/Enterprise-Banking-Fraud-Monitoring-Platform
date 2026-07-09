package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
