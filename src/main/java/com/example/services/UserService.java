package com.example.services;

import com.example.annotations.RefreshCache;
import com.example.dto.UserDTO;
import com.example.entities.User;
import com.example.repositories.UserJpaRepository;
import com.example.repositories.elastic.UserElasticRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.utils.AppConstants.USER;

@Service
public class UserService implements IUserService{

    public static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    UserElasticRepository userElasticRepository;

    @Value("${indicator}")
    private String indicator;

    public static String prop1;

    @Value("${property1}")
    public void setProp1(String p1){
        prop1 = p1;
    }

    public String getProp1(){
        return prop1;
    }

    public static final Function<Collection<User>, Map<Long, UserDTO>> CACHE_MAP_CONVERTER = (users) -> {
        Map<Long, UserDTO> userMap = users.stream().map(UserDTO::new).collect(Collectors.toConcurrentMap(UserDTO::getId, Function.identity(), (existing, newer) -> newer));
        return userMap;
    };

    @Override
    public Collection<UserDTO> findAll() {
        LOGGER.info("Indicator Value is : {} and prop1 is: {}", indicator, prop1);
        List<UserDTO> userDTOS = userJpaRepository.findAll().stream().map(UserDTO::new).collect(Collectors.toList());
        Iterable<UserDTO> userDTOS1 = userElasticRepository.saveAll(userDTOS);
        return userDTOS;
    }

    @Override
//    @Cacheable(value = USERS_BY_LOCATION, key="#location")
    public Collection<UserDTO> findByLocation(String location) {
        if(location == null){
            throw new IllegalArgumentException("Location cannot be null");
        }
        Collection<UserDTO> userDTOS  = userElasticRepository.findByLocation(location);
        if (CollectionUtils.isEmpty(userDTOS)) {
            LOGGER.info("Calling findByLocation on UserJpaRepository");
            Collection<User> users = userJpaRepository.findByLocation(location);
            userDTOS = users.stream().map(UserDTO::new).collect(Collectors.toList());
        } else {
            LOGGER.info("Got Users by Location Using UserElasticRepository {}", userDTOS);
        }
        return userDTOS;
    }

    @Override
//    @Cacheable(value = USERS_BY_DEPARTMENT, key="#department")
    public Collection<UserDTO> findByDepartment(String department) {
        Collection<UserDTO> userDTOS = null;
        if(department == null){
            throw new IllegalArgumentException("Department cannot be null");
        } else {
            userDTOS = userElasticRepository.findByDepartment(department);
            if(!CollectionUtils.isEmpty(userDTOS)){
                LOGGER.info("Got Users by Department Using UserElasticRepository {}", userDTOS);
            } else {
                LOGGER.info("Calling findByLocation on UserJpaRepository");
                Collection<User> users = userJpaRepository.findByDepartment(department);
                userDTOS = users.stream().map(UserDTO::new).collect(Collectors.toList());
            }
        }
        return userDTOS;
    }

    @Override
    public Collection<UserDTO> findByLocationAndDepartment(String location, String department) {
        Collection<UserDTO> userDTOS = null;
        if(location == null || department == null){
            throw new IllegalArgumentException("Location and Department cannot be null");
        } else {
            userDTOS = userElasticRepository.findByLocationAndDepartment(location, department);
            if(!CollectionUtils.isEmpty(userDTOS)){
                LOGGER.info("Got Users by Location and Department Using UserElasticRepository {}", userDTOS);
            } else {
                LOGGER.info("Calling findByLocationAndDepartment on UserJpaRepository");
                Collection<User> users = userJpaRepository.findByLocationAndDepartment(location, department);
                userDTOS = users.stream().map(UserDTO::new).collect(Collectors.toList());
            }
        }
        return userDTOS;
    }

    @Override
    public UserDTO findById(Long id) {
        return userJpaRepository.findById(id).map(UserDTO::new).orElseGet(() -> null);
    }

    @Override
    @RefreshCache(cacheNames = {USER})
    public UserDTO save(User user) {
        UserDTO saved = new UserDTO(userJpaRepository.save(user));
        return saved;
    }

    @Override
    @RefreshCache(cacheNames = {USER})
    public UserDTO create(User user) {
        UserDTO saved = new UserDTO(userJpaRepository.save(user));
        return saved;
    }

    @Override
    @RefreshCache(cacheNames = {USER}, isDelete = "Y")
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}
