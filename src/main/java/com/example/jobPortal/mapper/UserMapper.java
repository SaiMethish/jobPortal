package com.example.jobPortal.mapper;


import com.example.jobPortal.dto.request.UserRequestDTO;
import com.example.jobPortal.dto.response.UserResponseDTO;
import com.example.jobPortal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    //DTO to entity
    @Mapping(target="provider", expression = "java(com.jobportal.entity.AuthProvider.LOCAL)")
    User toEntity(UserRequestDTO userRequestDTO);

    //entity to DTO
    UserResponseDTO toResponseDTO(User user);
}
