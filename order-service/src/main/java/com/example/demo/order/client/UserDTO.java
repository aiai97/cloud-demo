package com.example.demo.order.client;

public record UserDTO(Long id, String name, boolean vip, String servedBy, String tenantId) {
}
