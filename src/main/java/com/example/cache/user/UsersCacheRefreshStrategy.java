package com.example.cache.user;

import com.example.cache.CacheRefreshStrategy;
import com.example.dto.UserDTO;
import com.example.services.IUserService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.utils.AppConstants.USER;

@Component
@Qualifier(USER)
public class UsersCacheRefreshStrategy extends CacheRefreshStrategy<String, UserDTO> {

    @Autowired
    IUserService userService;

    @Override
    public String cacheIdentifierField() {
        return "id";
    }

    @Override
    public UserDTO getExistingObjectByIdentifier(Object id) {
        return userService.findById((Long) id);
    }

    @Override
    public Map<String, Object> convertObjectToMap(UserDTO userDTO) {
        Map<String, Object> userMap = Maps.newHashMap();
        userMap.put("id", userDTO.getId());
        userMap.put("name", userDTO.getName());
        userMap.put("email", userDTO.getEmail());
        userMap.put("location", userDTO.getLocation());
        userMap.put("department", userDTO.getDepartment());
        userMap.put("band", userDTO.getBand());
        return userMap;
    }

    @Override
    public UserDTO convertMapToObject(Map<String, Object> map) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(Long.parseLong(String.valueOf(map.get("id"))));
        userDTO.setName((String) map.get("name"));
        userDTO.setEmail((String) map.get("email"));
        userDTO.setLocation((String) map.get("location"));
        userDTO.setDepartment((String) map.get("department"));
        userDTO.setBand((String) map.get("band"));
        return userDTO;
    }

}
