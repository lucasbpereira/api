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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController()
@RequestMapping("api/auth")
public class TokenController {

    private final TokenService service;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final MessageService messageService;

    public TokenController(TokenService service, UserRepository userRepository, TokenService tokenService, MessageService messageService) {

        this.service = service;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.messageService = messageService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticateUser(@RequestBody LoginRequest loginRequest) {

        LoginResponse response = service.login(loginRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> createUser(@RequestBody RegisterRequest request) {

        LoginResponse response = service.createUser(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers(Authentication authentication) {
        var users = userRepository.findAll();

        if(tokenService.isAdmin(authentication)) {
            return ResponseEntity.ok(users);
        } else {
            throw new UnauthorizedException(messageService.getMessage("error.unauthorized.function"));
        }
    }
}
