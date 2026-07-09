package org.horndevelopmentteam.bankingtransactionriskfraudmonitoring.common;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
