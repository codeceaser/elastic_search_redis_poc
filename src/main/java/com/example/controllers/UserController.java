package com.example.controllers;

import com.example.dto.UserDTO;
import com.example.entities.User;
import com.example.services.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @GetMapping
    public Collection<UserDTO> findAll() {
        LOGGER.info("Starting findAll");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id) {
        LOGGER.info("Starting findById");
        return userService.findById(id);
    }

    @GetMapping("/location/{location}")
    public Collection<UserDTO> findByLocation(@PathVariable String location) {
        LOGGER.info("Starting findByLocation {} ", location);
        return userService.findByLocation(location);
    }

    @GetMapping("/department/{department}")
    public Collection<UserDTO> findByDepartment(@PathVariable String department) {
        LOGGER.info("Starting find By Department {}",department);
        return userService.findByDepartment(department);
    }

    @GetMapping("/{location}/{department}")
    public Collection<UserDTO> findByLocationAndDepartment(@PathVariable String location, @PathVariable String department) {
        LOGGER.info("Starting find By Location {} And Department {} ",location, department);
        return userService.findByLocationAndDepartment(location, department);
    }

    @PostMapping
    public UserDTO create(@RequestBody User user) {
//        userCount++;
//        user.setId(userCount);
        LOGGER.info("Saving a new user {}", user);
        return userService.create(user);
    }

    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @RequestBody User user) {
        LOGGER.info("Updating an existing user {} with an id {}", user, id);
        user.setId(id);
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
