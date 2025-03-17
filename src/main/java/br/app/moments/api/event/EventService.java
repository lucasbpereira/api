package br.app.moments.api.event;

import br.app.moments.api.exceptions.NotFoundException;
import br.app.moments.api.exceptions.UnauthorizedException;
import br.app.moments.api.messages.MessageResponse;
import br.app.moments.api.messages.MessageService;
import br.app.moments.api.token.TokenService;
import br.app.moments.api.user.ParticipantResponse;
import br.app.moments.api.user.User;
import br.app.moments.api.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final MessageService messageService;

    public EventService(EventRepository eventRepository, UserRepository userRepository, TokenService tokenService, MessageService messageService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.messageService = messageService;
    }

    public EventResponse createEvent(EventRequest eventRequest, JwtAuthenticationToken token) {
        User user = userRepository.findByUserId(UUID.fromString(token.getName()));

        Event event = mapperEventRequestToEntity(eventRequest);
        event.setCreator(user);
        event.setParticipants(new HashSet<>());
        event.getParticipants().add(user);
        event.getParticipantResponses().put(user, true);

        Event createdEvent = eventRepository.save(event);

        return mapperEntityToEventResponse(createdEvent);
    }

    public EventResponse updateEvent(String eventId, EventRequest updatedEvent, JwtAuthenticationToken token) {
        UUID eventIdUUID = UUID.fromString(eventId);
        Event event = eventRepository.findByEventId(eventIdUUID);

        if(event == null) {
            throw new RuntimeException("Event not found");
        }

        String userId = token.getName();
        if (!event.getCreator().getUserId().equals(UUID.fromString(userId))  && !tokenService.isAdmin(token)) {
            throw new UnauthorizedException(messageService.getMessage("error.not.creator.event"));
        }

        Event mappedEvent = mapperEventRequestToEntity(updatedEvent);
        mappedEvent.setCreator(event.getCreator());
        mappedEvent.setEventId(event.getEventId());
        mappedEvent.setParticipants(event.getParticipants());
        mappedEvent.setParticipantResponses(event.getParticipantResponses());

        Event newEvent = eventRepository.save(mappedEvent);

        return mapperEntityToEventResponse(newEvent);
    }

    public void deleteEvent(String eventId, JwtAuthenticationToken token) {

        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event event = eventRepository.findByEventId(eventIdUUID);

        String userId = token.getName();
        if (!event.getCreator().getUserId().equals(UUID.fromString(userId)) && !tokenService.isAdmin(token)) {
            throw new UnauthorizedException(messageService.getMessage("error.not.creator.event"));
        }

        eventRepository.delete(event);
    }

    public List<EventResponse> listAllEvents() {

        List<Event> events = eventRepository.findAll();

        List<EventResponse> response = new ArrayList<EventResponse>();

        for (Event event : events) {
            response.add(mapperEntityToEventResponse(event));
        }

        return response;
    }

    public EventAllInfoResponse getEventById(String eventId) {
        UUID eventIdUUID = UUID.fromString(eventId); // Converte para UUID
        Event event = eventRepository.findByEventId(eventIdUUID);

        return new EventAllInfoResponse(
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDate(),
                event.getEventTime(),
                event.getMinParticipants(),
                event.getMaxParticipants(),
                mapperUserEntityToParticipantResponse(event.getParticipants(), event.getParticipantResponses())
        );
    }

    public MessageResponse confirmPresence(String eventId, boolean confirmed, JwtAuthenticationToken token) {

        UUID eventIdUUID = UUID.fromString(eventId);
        Event event = eventRepository.findByEventId(eventIdUUID);

        String userId = token.getName();
        User user = userRepository.findByUserId(UUID.fromString(userId));

        if (!event.getParticipants().contains(user)) {
            throw new UnauthorizedException(messageService.getMessage("error.not.participant.event"));
        }

        event.getParticipantResponses().put(user, confirmed);
        eventRepository.save(event);

        return new MessageResponse(HttpStatus.OK.value(), messageService.getMessage("success.confirmed.presence.event"));
    }

    public EventAllInfoResponse inviteUsers(String eventId, String email, String phone, JwtAuthenticationToken token) {
        UUID eventIdUUID = UUID.fromString(eventId);
        Event event = eventRepository.findByEventId(eventIdUUID);


        String userId = token.getName();
        if (!event.getCreator().getUserId().equals(UUID.fromString(userId)) && !tokenService.isAdmin(token)) {
            throw new UnauthorizedException(messageService.getMessage("error.not.creator.event"));
        }

        User user = null;

        if(email != null) {
            user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(messageService.getMessage("error.user.notfound")));
        }

        if(phone != null) {
            user = userRepository.findByPhone(phone).orElseThrow(() -> new NotFoundException(messageService.getMessage("error.user.notfound")));
        }

        Set<User> participants = event.getParticipants();
        participants.add(user);

        event.setParticipants(participants);

        eventRepository.save(event);

        return new EventAllInfoResponse(
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getEventDate(),
                event.getEventTime(),
                event.getMinParticipants(),
                event.getMaxParticipants(),
                mapperUserEntityToParticipantResponse(event.getParticipants(), event.getParticipantResponses())
        );
    }

    private EventResponse mapperEntityToEventResponse(Event dto) {
        EventResponse response = new EventResponse();
        response.setTitle(dto.getTitle());
        response.setDescription(dto.getDescription());
        response.setEventDate(dto.getEventDate());
        response.setLocation(dto.getLocation());
        response.setEventTime(dto.getEventTime());
        response.setMaxParticipants(dto.getMaxParticipants());
        response.setMinParticipants(dto.getMinParticipants());
        response.setEventId(dto.getEventId());

        return response;
    }


    private Event mapperEventRequestToEntity(EventRequest entity) {
        Event response = new Event();
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setEventDate(entity.getEventDate());
        response.setLocation(entity.getLocation());
        response.setEventTime(entity.getEventTime());
        response.setMaxParticipants(entity.getMaxParticipants());
        response.setMinParticipants(entity.getMinParticipants());

        return response;
    }

    private Set<ParticipantResponse> mapperUserEntityToParticipantResponse(Set<User> participants, Map<User, Boolean> participantResponses) {
        Set<ParticipantResponse> response = new HashSet<>();

        for (User user : participants) {
            ParticipantResponse participant = new ParticipantResponse();
            participant.setEmail(user.getEmail());
            participant.setFirstName(user.getFirstName());
            participant.setLastName(user.getLastName());

            boolean isConfirmed = participantResponses.containsKey(user) ? participantResponses.get(user) : false;
            participant.setConfirmed(isConfirmed);

            if (user.getPhoneShare()) {
                participant.setPhone(user.getPhone());
            }

            response.add(participant);
        }

        return response;
    }
}
