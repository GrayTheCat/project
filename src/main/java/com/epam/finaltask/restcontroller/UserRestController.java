package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String username, @Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(username, userDTO));
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> changeAccountStatus(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.changeAccountStatus(userDTO));
    }
}
