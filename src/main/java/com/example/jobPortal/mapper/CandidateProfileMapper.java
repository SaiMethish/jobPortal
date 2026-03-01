package com.example.jobPortal.mapper;

import com.example.jobPortal.dto.request.CandidateProfileRequestDTO;
import com.example.jobPortal.dto.response.CandidateProfileResponseDTO;
import com.example.jobPortal.entity.CandidateProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateProfileMapper {

    // entity → ResponseDTO
    CandidateProfileResponseDTO toResponseDTO(CandidateProfile profile);

    // RequestDTO → entity (set .user() manually in the service after this)
    CandidateProfile toEntity(CandidateProfileRequestDTO dto);

}
