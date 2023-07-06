package com.example.repositories.elastic;

import com.example.dto.UserDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Collection;

public interface UserElasticRepository extends ElasticsearchRepository<UserDTO, String> {

    Collection<UserDTO> findByBand(String band);
    Collection<UserDTO> findByLocation(String location);
    Collection<UserDTO> findByDepartment(String department);
    Collection<UserDTO> findByLocationAndDepartment(String location, String department);
    Collection<UserDTO> findByLocationAndBand(String location, String band);
    Collection<UserDTO> findByDepartmentAndBand(String department, String band);
}
