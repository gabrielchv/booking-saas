package dev.gazerah.booking.user;

import dev.gazerah.booking.common.ConflictException;
import dev.gazerah.booking.common.NotFoundException;
import dev.gazerah.booking.common.TenantContext;
import dev.gazerah.booking.user.dto.CreateUserRequest;
import dev.gazerah.booking.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAllByTenantId(TenantContext.require())
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (request.role() == Role.OWNER) {
            throw new ConflictException("A tenant has exactly one owner; create an admin or staff instead");
        }
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(
                TenantContext.require(),
                request.fullName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role());
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        if (role == Role.OWNER) {
            throw new ConflictException("Owner role cannot be assigned");
        }
        User user = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == Role.OWNER && isLastOwner()) {
            throw new ConflictException("Cannot demote the last owner");
        }
        user.setRole(role);
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = repository.findByIdAndTenantId(id, TenantContext.require())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == Role.OWNER) {
            throw new ConflictException("Owner account cannot be deleted");
        }
        repository.delete(user);
    }

    private boolean isLastOwner() {
        return repository.findAllByTenantIdAndRole(TenantContext.require(), Role.OWNER).size() <= 1;
    }
}
