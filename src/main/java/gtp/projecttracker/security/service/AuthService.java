package gtp.projecttracker.security.service;

import gtp.projecttracker.dto.request.user.LoginRequest;
import gtp.projecttracker.dto.request.user.RegisterRequest;
import gtp.projecttracker.dto.response.JwtResponse;
import gtp.projecttracker.model.jpa.User;
import gtp.projecttracker.model.jpa.User.Role;
import gtp.projecttracker.repository.jpa.UserRepository;
import gtp.projecttracker.security.jwt.JwtProvider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Validator validator;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            Validator validator
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.validator = validator;
    }

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        RegisterRequest sanitised = request.sanitized();
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request, RegisterRequest.NonOAuthValidation.class);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (userRepository.existsByEmail(sanitised.email())) {
            log.warn("Registration attempt with existing email: {}", sanitised.email());
            throw new IllegalArgumentException("Email already exists: " + sanitised.email());
        }

        User user = new User();
        user.setEmail(sanitised.email());
        user.setPassword(passwordEncoder.encode(sanitised.password()));
        user.setName(sanitised.name());
        user.setRole(determineDefaultRole());

        User savedUser = userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(savedUser);
        return generateTokenResponse(userDetails);
    }

    public JwtResponse login(LoginRequest request) {
        try {
            LoginRequest sanitised = request.sanitized();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            sanitised.email(),
                            sanitised.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return generateTokenResponse(userDetails);
        } catch (BadCredentialsException ex) {
            log.warn("Failed login attempt for email: {}", request.email());
            throw ex;
        }
    }

    private Role determineDefaultRole() {
        return userRepository.count() == 0 ? Role.ROLE_ADMIN : Role.ROLE_DEVELOPER;
    }

    private JwtResponse generateTokenResponse(UserDetailsImpl user) {
        String jwt = jwtProvider.generateToken(user);
        return new JwtResponse(
                jwt,
                user.getUser().getId(),
                user.getUser().getEmail(),
                user.getUser().getRole().name(),
                jwtProvider.getExpirationDuration()
        );
    }
}