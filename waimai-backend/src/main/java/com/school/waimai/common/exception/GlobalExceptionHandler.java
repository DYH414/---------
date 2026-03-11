package com.school.waimai.common.exception;

import com.school.waimai.common.api.ApiResponse;
import com.school.waimai.common.api.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<?> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ApiResponse<?> handleValidationException(Exception e) {
        log.warn("参数校验异常: {}", e.getMessage());
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                message = ex.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        return ApiResponse.error(ErrorCode.Common.ERR_PARAM_INVALID.getCode(), message);
    }

    /**
     * 请求体缺失或格式错误（如未传 body、非 JSON）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体不可读: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.Common.ERR_PARAM_INVALID.getCode(),
                "请使用 JSON 格式请求体，并设置 Content-Type: application/json");
    }

    /**
     * Content-Type 不支持（如传了 text/plain 而非 application/json）
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResponse<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的 Content-Type: {}", e.getMessage());
        return ApiResponse.error(ErrorCode.Common.ERR_PARAM_INVALID.getCode(),
                "请设置请求头 Content-Type: application/json");
    }

    /**
     * 其他异常：开发环境在 message 中返回异常信息便于排查
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        boolean isDev = environment.getActiveProfiles().length == 0
                || java.util.Arrays.asList(environment.getActiveProfiles()).contains("dev");
        String message = isDev && e.getMessage() != null
                ? (ErrorCode.Common.ERR_SYSTEM_BUSY.getMessage() + " [" + e.getMessage() + "]")
                : ErrorCode.Common.ERR_SYSTEM_BUSY.getMessage();
        return ApiResponse.error(ErrorCode.Common.ERR_SYSTEM_BUSY.getCode(), message);
    }
}
