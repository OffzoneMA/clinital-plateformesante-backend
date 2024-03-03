package com.clinitalPlatform.exception;

import com.clinital.util.ApiError;
import com.clinital.util.ResponseEntityBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(x -> x.getDefaultMessage())
				.collect(Collectors.toList());

		ApiError err = new ApiError(HttpStatus.BAD_REQUEST, "Constraint Violations", errors);

		return ResponseEntityBuilder.build(err);

	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handle(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		if (ex instanceof NullPointerException) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (ex instanceof ConstraintViolationException) {

			List<String> details = new ArrayList<String>();
			details.add(ex.getMessage());
			ApiError err = new ApiError(HttpStatus.BAD_REQUEST, "Constraint Violations", details);

			return ResponseEntityBuilder.build(err);

		}
		if (ex instanceof TransactionSystemException) {
			List<String> details = new ArrayList<String>();
			details.add(ex.getMessage());
			ApiError err = new ApiError(HttpStatus.BAD_REQUEST, "Transaction Exception", details);

			return ResponseEntityBuilder.build(err);

		}

		List<String> details = new ArrayList<String>();
		details.add(ex.getMessage());
		ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "the Exception"+details, details);

		return ResponseEntityBuilder.build(err);
	}

}
