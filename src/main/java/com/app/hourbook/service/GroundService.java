package com.app.hourbook.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.hourbook.dto.GroundRequestDto;
import com.app.hourbook.dto.GroundResponseDto;
import com.app.hourbook.enums.GroundStatus;
public interface GroundService {

    GroundResponseDto createGround(
            GroundRequestDto dto,
            MultipartFile[] images) throws IOException;

    GroundResponseDto updateGround(
            Integer id,
            GroundRequestDto dto,
            MultipartFile[] images) throws IOException;

    GroundResponseDto getGroundById(Integer id);

    List<GroundResponseDto> getAllGrounds();

    List<GroundResponseDto> getGroundsByStatus(GroundStatus status);

    String disableGround(Integer id);

    String enableGround(Integer id);

    String approveGround(Integer id);

    String rejectGround(Integer id);

    String deleteGround(Integer id);

}