package br.app.moments.api.token;

import br.app.moments.api.event.Event;
import br.app.moments.api.event.EventRepository;
import br.app.moments.api.exceptions.UnauthorizedException;
import br.app.moments.api.messages.MessageService;
import br.app.moments.api.user.Role;
import br.app.moments.api.user.RoleRepository;
import br.app.moments.api.user.User;
import br.app.moments.api.user.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EventRepository eventRepository;
    private final MessageService messageService;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenService(JwtEncoder jwtEncoder,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           EventRepository eventRepository,
                           MessageService messageService,
                           BCryptPasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.eventRepository = eventRepository;
        this.messageService = messageService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse createUser(RegisterRequest request) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());

        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UnauthorizedException(messageService.getMessage("error.conflict.email"));
        }
        if(userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new UnauthorizedException(messageService.getMessage("error.conflict.phone"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setRoles(Set.of(basicRole));
        user.setPhoneShare(request.getPhoneShare());
        user.setStreet(request.getStreet());
        user.setHouseNumber(request.getHouseNumber());
        user.setNeighborhood(request.getNeighborhood());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setCity(request.getCity());
        user.setCep(request.getCep());


        userRepository.save(user);

        // Verifica se o usuário tem um invitationCode e adiciona ao evento
        if (request.getInvitationCode() != null) {
            UUID eventId = UUID.fromString(request.getInvitationCode()); // Converte para UUID
            Event event = eventRepository.findByEventId(eventId);
                if(event != null) {
                    event.getParticipants().add(user);
                    eventRepository.save(event);
                }
        }
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(request.getEmail());
        loginRequest.setPassword(request.getPassword());

        return login(loginRequest);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());

        if(user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
            String message = messageService.getMessage("error.bad.credentials");

            throw new BadCredentialsException(message);
        }

        var now = Instant.now();
        var one_hour = 3600L;
        var expiresIn = one_hour * 24;

        var scopes = user.get().getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));


        var claims = JwtClaimsSet.builder()
                .issuer("moments_api")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        LoginResponse response = new LoginResponse();
        response.setAccessToken(jwtValue);
        response.setExpiresIn(expiresIn);

        return response;
    }

    public Boolean isAdmin(Authentication authentication) {

        // Recupera as autoridades do usuário autenticado
        var authorities = authentication.getAuthorities();

        // Verifica se o usuário tem a autoridade 'SCOPE_ADMIN'
        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("SCOPE_ADMIN"));

        return isAdmin;
    }
}
