package ru.practicum.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.error.model.ApiError;
import ru.practicum.stats.exceptions.ValidationError;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({
            ValidationError.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError requestCreateHandler(final Exception e) {
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "For the requested operation the conditions are not met.",
                e.getMessage()
        );
    }
}
