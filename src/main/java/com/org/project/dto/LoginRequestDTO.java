package com.org.project.dto;

import com.org.project.model.User;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @NotNull(message = "Provider is required")
    private User.Provider provider;

    private String oauthToken;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.Provider getProvider() {
        return provider;
    }

    public void setProvider(User.Provider provider) {
        this.provider = provider;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken){
        this.oauthToken = oauthToken;
    }

    @AssertTrue(message = "Password is required for LOCAL Provider")
    public boolean isPasswordValidForProvider() {
        return (provider == User.Provider.LOCAL) == (password != null);
    }

    @AssertTrue(message = "Token is required for OAuth providers")
    public boolean isOAuthTokenPresent() {
        return (provider == User.Provider.LOCAL) == (oauthToken == null);
    }
}
