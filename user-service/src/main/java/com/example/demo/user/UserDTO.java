package com.example.demo.user;

public record UserDTO(Long id, String name, boolean vip, String servedBy, String tenantId) {
}
