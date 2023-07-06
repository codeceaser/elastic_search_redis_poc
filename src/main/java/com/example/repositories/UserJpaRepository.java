package com.example.repositories;

import com.example.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Collection<User> findByBand(String band);
    Collection<User> findByLocation(String location);
    Collection<User> findByDepartment(String department);
    Collection<User> findByLocationAndDepartment(String location, String department);
    Collection<User> findByLocationAndBand(String location, String band);
    Collection<User> findByDepartmentAndBand(String department, String band);

}
