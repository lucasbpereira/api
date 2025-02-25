package br.app.moments.api.event;


import br.app.moments.api.token.TokenService;
import br.app.moments.api.user.User;
import br.app.moments.api.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public EventController(EventRepository eventRepository, UserRepository userRepository, TokenService tokenService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest,
                                             JwtAuthenticationToken token) {
        User user = userRepository.findByUserId(UUID.fromString(token.getName()));

        Event event = new Event();
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setEventDate(eventRequest.getEventDate());
        event.setLocation(eventRequest.getLocation());
        event.setEventTime(eventRequest.getEventTime());
        event.setCreator(user);

        // Adiciona o criador como participante com confirmação
        event.setParticipants(new HashSet<>());
        event.getParticipants().add(user); // Adiciona o criador como participante
        event.getParticipantResponses().put(user, true); // Presença confirmada

        System.out.println("USUÁRIO:  "+user);

        Event createdEvent = eventRepository.save(event);
        EventResponse response = new EventResponse();

        response.setTitle(createdEvent.getTitle());
        response.setDescription(createdEvent.getDescription());
        response.setEventDate(createdEvent.getEventDate());
        response.setLocation(createdEvent.getLocation());
        response.setEventTime(createdEvent.getEventTime());
        response.setEventId(createdEvent.getEventId());




        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String eventId,
                                             @RequestBody EventRequest updatedEvent,
                                             JwtAuthenticationToken token) {
        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event event = eventRepository.findByEventId(eventIdUUID);


        String userId = token.getName();
        System.out.println("evento" + event.getCreator().getUserId());
        System.out.println("userId" + UUID.fromString(userId));

        // Verifica se o usuário autenticado é o criador do evento
        if (!event.getCreator().getUserId().equals(UUID.fromString(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Atualiza os campos permitidos
        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setLocation(updatedEvent.getLocation());
        event.setEventDate(updatedEvent.getEventDate());
        event.setEventTime(updatedEvent.getEventTime());

        Event createdEvent = eventRepository.save(event);
        EventResponse response = new EventResponse();

        response.setTitle(createdEvent.getTitle());
        response.setDescription(createdEvent.getDescription());
        response.setEventDate(createdEvent.getEventDate());
        response.setLocation(createdEvent.getLocation());
        response.setEventTime(createdEvent.getEventTime());
        response.setEventId(createdEvent.getEventId());

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId,
                                            @AuthenticationPrincipal User authenticatedUser) {
        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event event = eventRepository.findByEventId(eventIdUUID);

        // Verifica se o usuário autenticado é o criador do evento
        if (!event.getCreator().getUserId().equals(authenticatedUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        eventRepository.delete(event);

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<List<Event>> listAllEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable String eventId) {
        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event eventOpt = eventRepository.findByEventId(eventIdUUID);

        return ResponseEntity.ok(eventOpt);
    }


    @PutMapping("/{eventId}/confirmPresence")
    public ResponseEntity<Void> confirmPresence(@PathVariable String eventId,
                                                @RequestParam boolean confirmed,
                                                @AuthenticationPrincipal User authenticatedUser) {
        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event event = eventRepository.findByEventId(eventIdUUID);

        // Verifica se o usuário está participando do evento
        if (!event.getParticipants().contains(authenticatedUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Atualiza a confirmação de presença
        event.getParticipantResponses().put(authenticatedUser, confirmed);
        eventRepository.save(event);

        return ResponseEntity.ok().build();
    }

}
