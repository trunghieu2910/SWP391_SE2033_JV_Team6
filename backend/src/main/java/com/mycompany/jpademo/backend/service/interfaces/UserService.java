package com.mycompany.jpademo.backend.service.interfaces;

import com.mycompany.jpademo.backend.entity.User;

import java.util.List;

public interface UserService {

    User getUserByEmail(String email);

    User getUserById(Integer userID);

    List<User> getAllUsers();

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}