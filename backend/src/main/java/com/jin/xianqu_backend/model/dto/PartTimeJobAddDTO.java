package com.jin.xianqu_backend.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class PartTimeJobAddDTO implements Serializable {
    private Long id; // Optional: for update. If null, it's a new job.
    private String title;
    private String content;
    private String workTime;
    private String workLocation;
    private String salary;
    private String settlementMethod;
    private Integer recruitCount;
    private String studentRequirement;
    private String tags;
    private String contactInfo;
    private String location;
    private Integer status; // Shelf status: 0-draft, 1-recruiting, 2-filled, 3-off-shelf
}
