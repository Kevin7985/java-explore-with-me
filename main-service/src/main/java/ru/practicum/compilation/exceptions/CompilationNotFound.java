package ru.practicum.compilation.exceptions;

public class CompilationNotFound extends RuntimeException {
    public CompilationNotFound(String message) {
        super(message);
    }
}
