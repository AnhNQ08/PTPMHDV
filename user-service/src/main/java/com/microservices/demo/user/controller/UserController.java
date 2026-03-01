package com.microservices.demo.user.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Value("${server.port}")
    private String serverPort;

    @GetMapping
    public Map<String, Object> getUsers() {
        List<Map<String, String>> users = List.of(
                Map.of("id", "1", "name", "Nguyen Van A", "email", "nguyenvana@example.com"),
                Map.of("id", "2", "name", "Tran Thi B", "email", "tranthib@example.com"),
                Map.of("id", "3", "name", "Le Van C", "email", "levanc@example.com")
        );

        return Map.of(
                "message", "User list fetched successfully from port " + serverPort,
                "serverPort", serverPort,
                "users", users
        );
    }
}
