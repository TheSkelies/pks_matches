package ru.mirea.matchaggregator.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.dto.admin.UserCreateRequest;
import ru.mirea.matchaggregator.dto.admin.UserDto;
import ru.mirea.matchaggregator.dto.admin.UserUpdateRequest;
import ru.mirea.matchaggregator.dto.common.PageResponse;
import ru.mirea.matchaggregator.repository.UserAccountRepository;
import ru.mirea.matchaggregator.service.UserAdminService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserAdminService service;
    private final UserAccountRepository userRepository;

    public AdminUserController(UserAdminService service, UserAccountRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PageResponse<UserDto> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<UserAccount> users = userRepository.findAll(pageable);
        return PageResponse.from(users.map(UserDto::from));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.from(service.create(req)));
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest req) {
        return UserDto.from(service.update(id, req));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable UUID id) {
        String tempPassword = service.resetPassword(id);
        return ResponseEntity.ok("Новый временный пароль: " + tempPassword);
    }
}
