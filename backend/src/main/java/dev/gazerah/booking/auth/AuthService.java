package dev.gazerah.booking.auth;

import dev.gazerah.booking.auth.dto.AuthResponse;
import dev.gazerah.booking.auth.dto.LoginRequest;
import dev.gazerah.booking.auth.dto.SignupRequest;
import dev.gazerah.booking.common.ConflictException;
import dev.gazerah.booking.tenant.Tenant;
import dev.gazerah.booking.tenant.TenantRepository;
import dev.gazerah.booking.user.Role;
import dev.gazerah.booking.user.User;
import dev.gazerah.booking.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        String slug = slugify(request.tenantName());
        if (tenantRepository.existsBySlug(slug)) {
            throw new ConflictException("Tenant name already taken");
        }

        Tenant tenant = tenantRepository.save(new Tenant(request.tenantName(), slug));

        User owner = new User(
                tenant.getId(),
                request.fullName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.OWNER);
        userRepository.save(owner);

        return toResponse(owner);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.issue(
                user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(
                token, user.getId(), user.getTenantId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private String slugify(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
