package com.coupang.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesChartDataDto {
    private Long dataSeq;
    private String userId;
    private String extractDate;
    private String dayOfWeek;
    private String productNumber;
    private String time;
    private BigDecimal salesEstimate;
    private Integer revenueEstimate;
    private LocalDateTime regDate;

    public SalesChartDataDto() {
    }

    public SalesChartDataDto(String userId, String extractDate, String productNumber, String time, BigDecimal salesEstimate, Integer revenueEstimate) {
        this.userId = userId;
        this.extractDate = extractDate;
        this.productNumber = productNumber;
        this.time = time;
        this.salesEstimate = salesEstimate;
        this.revenueEstimate = revenueEstimate;
    }

    public Long getDataSeq() {
        return dataSeq;
    }

    public void setDataSeq(Long dataSeq) {
        this.dataSeq = dataSeq;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(String extractDate) {
        this.extractDate = extractDate;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BigDecimal getSalesEstimate() {
        return salesEstimate;
    }

    public void setSalesEstimate(BigDecimal salesEstimate) {
        this.salesEstimate = salesEstimate;
    }

    public Integer getRevenueEstimate() {
        return revenueEstimate;
    }

    public void setRevenueEstimate(Integer revenueEstimate) {
        this.revenueEstimate = revenueEstimate;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}

