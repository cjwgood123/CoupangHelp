package com.coupang.dto;

import java.time.LocalDateTime;

public class SalesChartUserDto {
    private Long userSeq;
    private String userId;
    private String userPw;
    private String sellerType; // ROCKET_GROSS, OTHER
    private String sellerOther; // 기타 셀러명
    private LocalDateTime regDate;

    public SalesChartUserDto() {
    }

    public SalesChartUserDto(String userId, String userPw, String sellerType, String sellerOther) {
        this.userId = userId;
        this.userPw = userPw;
        this.sellerType = sellerType;
        this.sellerOther = sellerOther;
    }

    public Long getUserSeq() {
        return userSeq;
    }

    public void setUserSeq(Long userSeq) {
        this.userSeq = userSeq;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getSellerType() {
        return sellerType;
    }

    public void setSellerType(String sellerType) {
        this.sellerType = sellerType;
    }

    public String getSellerOther() {
        return sellerOther;
    }

    public void setSellerOther(String sellerOther) {
        this.sellerOther = sellerOther;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }
}

