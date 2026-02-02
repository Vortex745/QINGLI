package com.jin.xianqu_admin.model.dto;

import lombok.Data;

@Data
public class AdminUserPageDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String nickname;
    private String phone; // Optional search by phone
    private Integer status; // Status filter: 1=active, 0=banned
    private String role; // Filter by role (e.g. "user")
}
