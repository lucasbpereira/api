package br.app.moments.api.event;


import br.app.moments.api.messages.MessageResponse;
import br.app.moments.api.token.TokenService;
import br.app.moments.api.user.ParticipantResponse;
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
import org.springframework.web.servlet.HandlerMapping;

import java.util.*;

@RestController
@RequestMapping("api/event")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest,
                                             JwtAuthenticationToken token) {

        EventResponse response = eventService.createEvent(eventRequest, token);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String eventId,
                                             @RequestBody EventRequest updatedEvent,
                                             JwtAuthenticationToken token) {
        EventResponse response = eventService.updateEvent(eventId, updatedEvent, token);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{eventId}")
    public ResponseEntity<MessageResponse> deleteEvent(@PathVariable String eventId,
                                                       JwtAuthenticationToken token) {
        eventService.deleteEvent(eventId, token);

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<List<EventResponse>> listAllEvents() {

        List<EventResponse> response = eventService.listAllEvents();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventAllInfoResponse> getEventById(@PathVariable String eventId) {
        EventAllInfoResponse response = eventService.getEventById(eventId);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{eventId}/confirmPresence")
    public ResponseEntity<MessageResponse> confirmPresence(@PathVariable String eventId,
                                                @RequestParam boolean confirmed,
                                                JwtAuthenticationToken token) {

        MessageResponse response = eventService.confirmPresence(eventId, confirmed, token);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{eventId}/invite")
    public ResponseEntity<EventAllInfoResponse> inviteUsers(@PathVariable String eventId,
                                                            @RequestParam(required = false) String email,
                                                            @RequestParam(required = false) String phone,
                                                           JwtAuthenticationToken token) {


        EventAllInfoResponse response = eventService.inviteUsers(eventId, email, phone, token);

        return ResponseEntity.ok(response);
    }
}
