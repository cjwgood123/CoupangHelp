package com.coupang.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarginTrackerDataDto {
    private Long dataSeq;
    private String userId;
    private String productNumber;
    private String productName;
    private String optionId;
    private String optionAlias;
    private String saleDate;
    private BigDecimal sellingPrice;
    private BigDecimal discountCoupon;
    private BigDecimal finalSellingPrice;
    private BigDecimal priceFluctuation;
    private BigDecimal salesQuantity;
    private BigDecimal actualSalesRevenue;
    private BigDecimal marginPerUnit;
    private BigDecimal totalMargin;
    private BigDecimal advertisingCost;
    private BigDecimal advertisingCostAdjusted;
    private BigDecimal netProfit;
    private BigDecimal marginRate;
    private BigDecimal adSales;
    private BigDecimal organicSales;
    private BigDecimal organicSalesRatio;
    private BigDecimal roas;
    private LocalDateTime regDate;

    public Long getDataSeq() { return dataSeq; }
    public void setDataSeq(Long dataSeq) { this.dataSeq = dataSeq; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getOptionId() { return optionId; }
    public void setOptionId(String optionId) { this.optionId = optionId; }
    public String getOptionAlias() { return optionAlias; }
    public void setOptionAlias(String optionAlias) { this.optionAlias = optionAlias; }
    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public BigDecimal getDiscountCoupon() { return discountCoupon; }
    public void setDiscountCoupon(BigDecimal discountCoupon) { this.discountCoupon = discountCoupon; }
    public BigDecimal getFinalSellingPrice() { return finalSellingPrice; }
    public void setFinalSellingPrice(BigDecimal finalSellingPrice) { this.finalSellingPrice = finalSellingPrice; }
    public BigDecimal getPriceFluctuation() { return priceFluctuation; }
    public void setPriceFluctuation(BigDecimal priceFluctuation) { this.priceFluctuation = priceFluctuation; }
    public BigDecimal getSalesQuantity() { return salesQuantity; }
    public void setSalesQuantity(BigDecimal salesQuantity) { this.salesQuantity = salesQuantity; }
    public BigDecimal getActualSalesRevenue() { return actualSalesRevenue; }
    public void setActualSalesRevenue(BigDecimal actualSalesRevenue) { this.actualSalesRevenue = actualSalesRevenue; }
    public BigDecimal getMarginPerUnit() { return marginPerUnit; }
    public void setMarginPerUnit(BigDecimal marginPerUnit) { this.marginPerUnit = marginPerUnit; }
    public BigDecimal getTotalMargin() { return totalMargin; }
    public void setTotalMargin(BigDecimal totalMargin) { this.totalMargin = totalMargin; }
    public BigDecimal getAdvertisingCost() { return advertisingCost; }
    public void setAdvertisingCost(BigDecimal advertisingCost) { this.advertisingCost = advertisingCost; }
    public BigDecimal getAdvertisingCostAdjusted() { return advertisingCostAdjusted; }
    public void setAdvertisingCostAdjusted(BigDecimal advertisingCostAdjusted) { this.advertisingCostAdjusted = advertisingCostAdjusted; }
    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
    public BigDecimal getMarginRate() { return marginRate; }
    public void setMarginRate(BigDecimal marginRate) { this.marginRate = marginRate; }
    public BigDecimal getAdSales() { return adSales; }
    public void setAdSales(BigDecimal adSales) { this.adSales = adSales; }
    public BigDecimal getOrganicSales() { return organicSales; }
    public void setOrganicSales(BigDecimal organicSales) { this.organicSales = organicSales; }
    public BigDecimal getOrganicSalesRatio() { return organicSalesRatio; }
    public void setOrganicSalesRatio(BigDecimal organicSalesRatio) { this.organicSalesRatio = organicSalesRatio; }
    public BigDecimal getRoas() { return roas; }
    public void setRoas(BigDecimal roas) { this.roas = roas; }
    public LocalDateTime getRegDate() { return regDate; }
    public void setRegDate(LocalDateTime regDate) { this.regDate = regDate; }
}
