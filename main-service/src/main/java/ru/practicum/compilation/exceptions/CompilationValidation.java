package ru.practicum.compilation.exceptions;

public class CompilationValidation extends RuntimeException {
    public CompilationValidation(String message) {
        super(message);
    }
}
