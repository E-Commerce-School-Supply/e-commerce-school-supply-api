package com.e_commerce.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private String phoneNumber;
    private String avatarUrl;
    private String status;
    private String lastLoginDate;
    private LocalDateTime createdAt;
}
