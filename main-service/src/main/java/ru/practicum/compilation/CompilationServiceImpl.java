package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.ViewStats;
import ru.practicum.error.exceptions.ServiceConnection;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.request.RequestRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.service.MapperService;
import ru.practicum.service.ValidationService;
import ru.practicum.utils.Pagination;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final MapperService mapperService;
    private final ValidationService validationService;
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    private final StatsClient statsClient;

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        if (compilationDto.getPinned() == null) {
            compilationDto.setPinned(false);
        }

        List<Event> events = eventRepository.findByIdIn(compilationDto.getEvents() == null ? new ArrayList<>() : compilationDto.getEvents());

        Compilation comp = mapperService.toCompilation(compilationDto, events);

        log.info("Создана новая подборка: " + comp);
        return toCompilationDto(compilationRepository.save(comp));
    }

    @Override
    public void deleteCompilation(Long compId) {
        validationService.validateCompilation(compId);

        log.info("Удалена подборка id = " + compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationRequest) {
        Compilation comp = validationService.validateCompilation(compId);

        comp.setTitle(compilationRequest.getTitle() == null ? comp.getTitle() : compilationRequest.getTitle());
        comp.setPinned(compilationRequest.getPinned() == null ? comp.getPinned() : compilationRequest.getPinned());

        if (compilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findByIdIn(compilationRequest.getEvents());
            comp.setEvents(events);
        }

        log.info("Обновлена подборка с id = " + compId);
        return toCompilationDto(compilationRepository.save(comp));
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation comp = validationService.validateCompilation(compId);

        log.info("Получена информации о подборке с id = " + compId);
        return toCompilationDto(comp);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<CompilationDto> compilations = new ArrayList<>();

        Pageable pageable;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Page<Compilation> page;
        Pagination pager = new Pagination(from, size);

        if (size == null) {
            pageable = PageRequest.of(pager.getPageStart(), pager.getPageSize(), sort);
            page = pinned != null ? compilationRepository.findByPinned(pinned, pageable) : compilationRepository.findAll(pageable);

            while (page.hasContent()) {
                compilations.addAll(page.stream()
                        .map(this::toCompilationDto)
                        .collect(Collectors.toList()));
                pageable = pageable.next();
                page = pinned != null ? compilationRepository.findByPinned(pinned, pageable) : compilationRepository.findAll(pageable);
            }
        } else {
            for (int i = pager.getPageStart(); i < pager.getPagesAmount(); i++) {
                pageable = PageRequest.of(i, pager.getPageSize(), sort);
                page = pinned != null ? compilationRepository.findByPinned(pinned, pageable) : compilationRepository.findAll(pageable);
                compilations.addAll(page.stream()
                        .map(this::toCompilationDto)
                        .collect(Collectors.toList()));
            }

            compilations = compilations.stream().limit(size).collect(Collectors.toList());
        }

        log.info("Получение информации о подборках");
        return compilations;
    }

    private CompilationDto toCompilationDto(Compilation compilation) {
        List<Long> eventIds = compilation.getEvents().stream().map(Event::getId).collect(Collectors.toList());

        List<Request> requests = requestRepository.findByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        HashMap<Long, List<Request>> req = new HashMap<>();

        requests.forEach(item -> {
            if (!req.containsKey(item.getId())) {
                req.put(item.getId(), new ArrayList<>());
            }

            req.get(item.getId()).add(item);
        });

        List<String> uris = eventIds.stream().map(item -> "/events/" + item).collect(Collectors.toList());
        List<ViewStats> statsReq = statsClient.getStats(LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.now(), uris, true);
        if (statsReq == null) {
            throw new ServiceConnection("Возвращён пустой ответ сервера статистики");
        }

        HashMap<Long, ViewStats> stats = new HashMap<>();
        statsReq.forEach(item -> {
            stats.put(Long.valueOf(item.getUri().split("/events/")[1]), item);
        });

        List<EventShortDto> events = compilation.getEvents().stream()
                .map(item -> mapperService.toEventShortDto(
                        item,
                        req.containsKey(item.getId()) ? req.get(item.getId()).size() : 0,
                        stats.containsKey(item.getId()) ? stats.get(item.getId()).getHits() : 0))
                .collect(Collectors.toList());

        return mapperService.toCompilationDto(compilation, events);
    }
}
