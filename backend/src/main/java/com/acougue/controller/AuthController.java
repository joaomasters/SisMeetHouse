package com.acougue.controller;

import com.acougue.config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if ("teste".equals(username) && "teste".equals(password)) {
            return ResponseEntity.ok(Map.of(
                "token",    jwtUtil.generateToken(username),
                "username", username
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Credenciais invalidas"));
    }
}
