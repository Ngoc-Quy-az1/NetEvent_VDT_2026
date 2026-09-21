package com.example.notification.common.exception;

public enum ErrorCode {
    INTERNAL_SERVER_ERROR(500, "ERR_500", "An unexpected internal error occurred"),
    INVALID_REQUEST(400, "ERR_400", "Invalid request parameter"),
    NOT_FOUND(404, "ERR_404", "Requested resource not found"),
    UNAUTHORIZED(401, "ERR_401", "Unauthorized access"),
    RULE_EVALUATION_FAILED(422, "ERR_422", "Business rule evaluation failed"),
    APPROVAL_TIMEOUT(408, "ERR_408", "Approval process timed out"),
    DELIVERY_FAILED(502, "ERR_502", "Channel delivery failed");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
