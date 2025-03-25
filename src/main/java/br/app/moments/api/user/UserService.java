package br.app.moments.api.user;

import br.app.moments.api.event.Event;
import br.app.moments.api.event.EventRepository;
import br.app.moments.api.event.EventRequest;
import br.app.moments.api.event.EventResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getInfo(JwtAuthenticationToken token) {
        User user = userRepository.findByUserId(UUID.fromString(token.getName()));

        return mapperEntityToUserResponse(user);
    }


    private UserResponse mapperEntityToUserResponse(User entity) {
        UserResponse response = new UserResponse();
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setPhone(entity.getPhone());
        response.setPhoneShare(entity.getPhoneShare());
        response.setStreet(entity.getStreet());
        response.setHouseNumber(entity.getHouseNumber());
        response.setNeighborhood(entity.getNeighborhood());
        response.setCity(entity.getCity());
        response.setState(entity.getState());
        response.setCountry(entity.getCountry());
        response.setCep(entity.getCep());
        response.setGender(entity.getGender());
        response.setBirthDate(entity.getBirthDate());
        response.setRoles(entity.getRoles());
        response.setEvents(entity.getEvents());

        return response;
    }
}
