package com.jin.xianqu_backend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String bio;
    private String bgImage;
}
