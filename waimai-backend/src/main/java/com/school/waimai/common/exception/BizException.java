package com.school.waimai.common.exception;

import com.school.waimai.common.api.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;
    private final String message;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String message) {
        super(message);
        this.code = ErrorCode.Common.ERR_BAD_REQUEST.getCode();
        this.message = message;
    }
}
