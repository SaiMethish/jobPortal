package com.example.jobPortal.mapper;

import com.example.jobPortal.dto.request.RecruiterProfileRequestDTO;
import com.example.jobPortal.dto.response.RecruiterProfileResponseDTO;
import com.example.jobPortal.entity.RecruiterProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterProfileMapper {

    // entity → ResponseDTO
    @Mapping(source = "user.userId", target = "userId")
    RecruiterProfileResponseDTO toResponseDTO(RecruiterProfile profile);

    // RequestDTO → entity (set .user() manually in the service after this)
    RecruiterProfile toEntity(RecruiterProfileRequestDTO dto);
}
