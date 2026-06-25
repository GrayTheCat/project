package com.epam.finaltask.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ModelAndView handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return handleException(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ModelAndView handleBusinessLogicException(BusinessLogicException ex) {
        return handleException(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("Suspicious activity detected.");
        return handleException(HttpStatus.FORBIDDEN, ex);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAll(Exception ex) {
        return handleException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex) {
        return handleException(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid UUID or argument passed: {}", ex.getMessage());
        return handleException(HttpStatus.BAD_REQUEST, ex); // Видасть 400 Bad Request
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        ModelAndView view = new ModelAndView("error");
        view.addObject("status", HttpStatus.FORBIDDEN.value());
        return view;
    }

    private ModelAndView handleException(HttpStatus status, Exception ex){
        log.error(ex.getMessage());
        ModelAndView view = new ModelAndView("error");
        view.addObject("requestUri", "/error");
        view.addObject("status", status.value());
        return view;
    }
}
