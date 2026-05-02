package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.requests.ChangePasswordRequest;
import com.codewithmosh.store.dtos.requests.UpdateUserRequest;
import com.codewithmosh.store.dtos.resources.UserResource;
import com.codewithmosh.store.mappers.UserMapper;
import com.codewithmosh.store.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping
    public org.springframework.data.domain.Page<UserResource> getAllUsers(
            @RequestParam(required = false, defaultValue = "name", name = "sort") String sort,
            @RequestParam(required = false, defaultValue = "0", name = "page") int page,
            @RequestParam(required = false, defaultValue = "10", name = "size") int size) {
        if (!Set.of("name", "email").contains(sort)) sort = "name";
        var pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sort));
        var usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResource> getUser(@PathVariable Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userMapper.toResource(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResource> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        userMapper.update(request, user);
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.toResource(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (!user.getPassword().equals(request.getOldPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}
