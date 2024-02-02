package ru.practicum.compilation.dto;

import org.springframework.stereotype.Component;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.List;

@Component
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto compilationDto, List<Event> events) {
        return new Compilation(
                null,
                compilationDto.getTitle(),
                compilationDto.getPinned(),
                events
        );
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                events
        );
    }
}
