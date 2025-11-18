package com.coupang.dto;

import java.time.LocalDate;

public class ProductListDto {
    private Long seq; // DB 원본 seq (참고용)
    private int rowNum; // 조회일자 기준 순번 (1부터 시작)
    private String title;
    private String productID;
    private LocalDate regidate;
    private String url;
    private String category;
    private String review; // 상품평 개수
    private Integer reviewIncrease; // 상품평 증가량 (이전 대비)

    public ProductListDto(Long seq, int rowNum, String title, String productID, LocalDate regidate, String url, String category, String review, Integer reviewIncrease) {
        this.seq = seq;
        this.rowNum = rowNum;
        this.title = title;
        this.productID = productID;
        this.regidate = regidate;
        this.url = url;
        this.category = category;
        this.review = review;
        this.reviewIncrease = reviewIncrease;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public LocalDate getRegidate() {
        return regidate;
    }

    public void setRegidate(LocalDate regidate) {
        this.regidate = regidate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Integer getReviewIncrease() {
        return reviewIncrease;
    }

    public void setReviewIncrease(Integer reviewIncrease) {
        this.reviewIncrease = reviewIncrease;
    }
}

