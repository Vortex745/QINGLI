package com.jin.xianqu_backend.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

@Data
public class LostFoundAddDTO implements Serializable {
    private Long id; // Optional: for update. If null, it's a new entry.
    /**
     * 0-失物, 1-招领
     */
    private Integer type;
    private String itemName;
    private String category;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date time;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String features;
    private String imageUrls;
    private String contactInfo;
    private Integer isPublic;
    private String university;
    private Integer status; // 0-Publishing, 1-Found/Returned, 2-Off-shelf
}
