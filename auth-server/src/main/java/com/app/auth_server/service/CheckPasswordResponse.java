package com.app.auth_server.service;

public record CheckPasswordResponse(String passwordHash, String breachCount, boolean leaked) {}
