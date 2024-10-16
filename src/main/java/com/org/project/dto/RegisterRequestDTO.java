package com.org.project.dto;

import com.org.project.model.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterRequestDTO {
    @NotBlank(message = "Name is required")
    private final String name;

    @NotBlank(message = "Email is required")
    @Email(message="Invalid email format")
    private final String email;

    private final String password;

    @NotNull(message = "Provider is required")
    private final User.Provider provider;

    public RegisterRequestDTO(String name, String email, String password, User.Provider provider) {
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
    public boolean isPasswordValidForProvider() {
        return (provider == User.Provider.LOCAL) == (password != null);
    }
}
