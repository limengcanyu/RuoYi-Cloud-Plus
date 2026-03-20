package org.dromara.common.http.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.base.BaseException;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.http.annotation.RemoteServiceController;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 仅作用于内部远程 HTTP 接口的异常处理器.
 *
 * 远程接口与普通对外 API 分开处理:
 * 1. provider 直接返回非 2xx HTTP 状态，consumer 只按状态码判错
 * 2. 响应体仍保留 R.code / R.msg，方便把业务码继续透传回消费方
 */
@Slf4j
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RemoteServiceController.class)
public class RemoteHttpExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
        HttpServletRequest request) {
        log.error("请求地址'{}',不支持'{}'请求", request.getRequestURI(), e.getMethod());
        return buildResponse(HttpStatus.BAD_METHOD, e.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<R<Void>> handleServiceException(ServiceException e) {
        log.error(e.getMessage());
        int code = resolveBusinessCode(e.getCode(), HttpStatus.ERROR);
        return buildResponse(code, e.getMessage());
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<R<Void>> handleServletException(ServletException e, HttpServletRequest request) {
        log.error("请求地址'{}',发生未知异常.", request.getRequestURI(), e);
        return buildResponse(HttpStatus.ERROR, e.getMessage());
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<R<Void>> handleBaseException(BaseException e) {
        log.error(e.getMessage());
        return buildResponse(HttpStatus.ERROR, e.getMessage());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<R<Void>> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
        HttpServletRequest request) {
        log.error("请求参数类型不匹配'{}',发生系统异常.", request.getRequestURI());
        String message = String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'",
            e.getName(), e.getRequiredType().getName(), e.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<Void>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("请求地址'{}'不存在.", request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBindException(BindException e) {
        log.error(e.getMessage());
        String message = StreamUtils.join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> constraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage());
        String message = StreamUtils.join(e.getConstraintViolations(), ConstraintViolation::getMessage, ", ");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = StreamUtils.join(e.getBindingResult().getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<R<Void>> handlerMethodValidationException(HandlerMethodValidationException e) {
        log.error(e.getMessage());
        String message = StreamUtils.join(e.getAllErrors(), MessageSourceResolvable::getDefaultMessage, ", ");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
        HttpServletRequest request) {
        log.error("请求地址'{}', 参数解析失败: {}", request.getRequestURI(), e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "请求参数格式错误：" + e.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<Void>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("请求地址'{}',发生未知异常.", request.getRequestURI(), e);
        return buildResponse(HttpStatus.ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("请求地址'{}',发生系统异常.", request.getRequestURI(), e);
        return buildResponse(HttpStatus.ERROR, e.getMessage());
    }

    private ResponseEntity<R<Void>> buildResponse(int code, String message) {
        return ResponseEntity.status(resolveHttpStatus(code))
            .body(R.fail(code, message));
    }

    private HttpStatusCode resolveHttpStatus(int code) {
        if (code >= 100 && code <= 599) {
            return HttpStatusCode.valueOf(code);
        }
        return HttpStatusCode.valueOf(HttpStatus.ERROR);
    }

    private int resolveBusinessCode(Integer code, int defaultCode) {
        return code == null ? defaultCode : code;
    }
}
