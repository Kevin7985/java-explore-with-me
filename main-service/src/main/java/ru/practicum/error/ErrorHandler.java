package ru.practicum.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.category.exceptions.CategoryNotFound;
import ru.practicum.compilation.exceptions.CompilationNotFound;
import ru.practicum.compilation.exceptions.CompilationValidation;
import ru.practicum.error.model.ApiError;
import ru.practicum.event.exceptions.DateValidation;
import ru.practicum.event.exceptions.EventConditions;
import ru.practicum.event.exceptions.EventNotFound;
import ru.practicum.event.exceptions.EventValidation;
import ru.practicum.request.exceptions.RequestConflict;
import ru.practicum.request.exceptions.RequestNotFound;
import ru.practicum.user.exceptions.SubscriptionAlreadyExists;
import ru.practicum.user.exceptions.SubscriptionConflict;
import ru.practicum.user.exceptions.SubscriptionNotFound;
import ru.practicum.user.exceptions.UserNotFound;

import javax.validation.ConstraintViolationException;
import java.net.ConnectException;
import java.sql.SQLException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            EventValidation.class,
            InvalidFormatException.class,
            DateValidation.class,
            CompilationValidation.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError invalidRequestHandler(final Exception e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                e.getMessage()
        );
    }

    @ExceptionHandler({SQLException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError sqlConflictHandler(final SQLException e) {
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }

    @ExceptionHandler({
            UserNotFound.class,
            CategoryNotFound.class,
            EventNotFound.class,
            RequestNotFound.class,
            CompilationNotFound.class,
            SubscriptionNotFound.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError entityNotFoundHandler(final RuntimeException e) {
        return new ApiError(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                e.getMessage()
        );
    }

    @ExceptionHandler({
            ConnectException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError externalServicesConnectHandler(final ConnectException e) {
        return new ApiError(
                "INTERNAL_SERVER_ERROR",
                "Could not connect to external service",
                e.getMessage()
        );
    }

    @ExceptionHandler({
            RequestConflict.class,
            EventConditions.class,
            SubscriptionAlreadyExists.class,
            SubscriptionConflict.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError requestCreateHandler(final Exception e) {
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "For the requested operation the conditions are not met.",
                e.getMessage()
        );
    }
}
