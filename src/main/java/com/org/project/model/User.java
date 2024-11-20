package com.org.project.model;

import jakarta.persistence.*;

import com.org.project.util.PasswordUtil;
import com.org.project.util.AuthUtil;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Entity
@Table(name = "users")
public class User {

	@Id
	@UuidGenerator
	@Column(name = "id", length = 36, updatable = false, nullable = false)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "auth_version")
	private Integer authVersion;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Date createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Date updatedAt;

	@PrePersist
	protected void onCreate() {
		authVersion = AuthUtil.generateRandomAuthVersion();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Integer getAuthVersion() {
		return authVersion;
	}

	public void setAuthVersion(Integer authVersion) {
		this.authVersion = authVersion;
	}

	public void setPasswordHash(String password) {
		this.passwordHash = PasswordUtil.hashPassword(password);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public enum Provider {
		GOOGLE,
		GITHUB,
		LOCAL
	}
}
