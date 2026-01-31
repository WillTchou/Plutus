package com.project.plutus.user.mapper;

import com.project.plutus.user.model.User;
import com.project.plutus.user.model.UserDTO;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toUserDTO(User user);

    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "password", expression = "java(passwordEncoder.encode(updatedUser.getPassword()))")
    void mapUpdateUser(User updatedUser, @MappingTarget User userToUpdate, @Context PasswordEncoder passwordEncoder);
}
