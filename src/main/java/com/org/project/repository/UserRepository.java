package com.org.project.repository;

import com.org.project.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> { }
