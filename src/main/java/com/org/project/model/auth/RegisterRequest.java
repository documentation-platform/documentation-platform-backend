package com.org.project.model.auth;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String provider;

    public RegisterRequest(String name, String email, String password, String provider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProvider() {
        return provider;
    }
}
