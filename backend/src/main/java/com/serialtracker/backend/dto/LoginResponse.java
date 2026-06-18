package com.serialtracker.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}