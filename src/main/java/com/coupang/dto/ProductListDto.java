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
    private Integer fromCount; // 시작 count
    private Integer toCount; // 끝 count
    private String fromTable; // 시작 테이블명
    private String toTable; // 끝 테이블명
    private LocalDate fromDate; // 시작 날짜
    private LocalDate toDate; // 끝 날짜
    private Integer daysDiff; // 일수 차이

    public ProductListDto(Long seq, int rowNum, String title, String productID, LocalDate regidate, String url, String category, String review, Integer reviewIncrease) {
        this(seq, rowNum, title, productID, regidate, url, category, review, reviewIncrease, null, null, null, null, null, null, null);
    }

    public ProductListDto(Long seq, int rowNum, String title, String productID, LocalDate regidate, String url, String category, String review, Integer reviewIncrease, Integer fromCount, Integer toCount, String fromTable, String toTable, LocalDate fromDate, LocalDate toDate, Integer daysDiff) {
        this.seq = seq;
        this.rowNum = rowNum;
        this.title = title;
        this.productID = productID;
        this.regidate = regidate;
        this.url = url;
        this.category = category;
        this.review = review;
        this.reviewIncrease = reviewIncrease;
        this.fromCount = fromCount;
        this.toCount = toCount;
        this.fromTable = fromTable;
        this.toTable = toTable;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.daysDiff = daysDiff;
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

    public Integer getFromCount() {
        return fromCount;
    }

    public void setFromCount(Integer fromCount) {
        this.fromCount = fromCount;
    }

    public Integer getToCount() {
        return toCount;
    }

    public void setToCount(Integer toCount) {
        this.toCount = toCount;
    }

    public String getFromTable() {
        return fromTable;
    }

    public void setFromTable(String fromTable) {
        this.fromTable = fromTable;
    }

    public String getToTable() {
        return toTable;
    }

    public void setToTable(String toTable) {
        this.toTable = toTable;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public Integer getDaysDiff() {
        return daysDiff;
    }

    public void setDaysDiff(Integer daysDiff) {
        this.daysDiff = daysDiff;
    }
}

