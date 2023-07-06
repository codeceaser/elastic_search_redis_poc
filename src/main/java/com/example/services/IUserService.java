package com.example.services;

import com.example.dto.UserDTO;
import com.example.entities.User;

import java.util.Collection;

public interface IUserService {
    Collection<UserDTO> findAll();

    Collection<UserDTO> findByLocation(String location);

    Collection<UserDTO> findByDepartment(String department);
    Collection<UserDTO> findByLocationAndDepartment(String location, String department);

    UserDTO findById(Long id);

    UserDTO save(User user);

    UserDTO create(User user);

    void deleteById(Long id);
}
