package com.org.project.model.auth;

import com.org.project.model.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message="Invalid email format")
    private String email;

    private String password;

    @NotNull(message = "Provider is required")
    private User.Provider provider;

    public RegisterRequest(String name, String email, String password, User.Provider provider) {
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

    public User.Provider getProvider() {
        return provider;
    }

    @AssertTrue(message = "Password is required for LOCAL Provider")
    private boolean isPasswordValidForProvider(){
        return ((provider == User.Provider.LOCAL) && (password != null)) || ((provider != User.Provider.LOCAL) && (password == null));
    }
}
