package com.jin.xianqu_backend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private String code;
    private String phoneCode;
}
