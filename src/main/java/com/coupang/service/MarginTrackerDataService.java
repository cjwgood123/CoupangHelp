package com.coupang.service;

import com.coupang.dto.MarginTrackerDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarginTrackerDataService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MarginTrackerDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<MarginTrackerDataDto> ROW_MAPPER = (rs, rowNum) -> {
        MarginTrackerDataDto dto = new MarginTrackerDataDto();
        dto.setDataSeq(rs.getLong("data_seq"));
        dto.setUserId(rs.getString("user_id"));
        dto.setProductNumber(rs.getString("product_number"));
        try { dto.setProductName(rs.getString("product_name")); } catch (Exception ignored) { /* column may not exist */ }
        try { dto.setOptionId(rs.getString("option_id")); } catch (Exception ignored) { /* column may not exist */ }
        try { dto.setOptionAlias(rs.getString("option_alias")); } catch (Exception ignored) { /* column may not exist */ }
        dto.setSaleDate(rs.getString("sale_date"));
        dto.setSellingPrice(toBigDecimal(rs, "selling_price"));
        dto.setDiscountCoupon(toBigDecimal(rs, "discount_coupon"));
        dto.setFinalSellingPrice(toBigDecimal(rs, "final_selling_price"));
        dto.setPriceFluctuation(toBigDecimal(rs, "price_fluctuation"));
        dto.setSalesQuantity(toBigDecimal(rs, "sales_quantity"));
        dto.setActualSalesRevenue(toBigDecimal(rs, "actual_sales_revenue"));
        dto.setMarginPerUnit(toBigDecimal(rs, "margin_per_unit"));
        dto.setTotalMargin(toBigDecimal(rs, "total_margin"));
        dto.setAdvertisingCost(toBigDecimal(rs, "advertising_cost"));
        dto.setAdvertisingCostAdjusted(toBigDecimal(rs, "advertising_cost_adjusted"));
        dto.setNetProfit(toBigDecimal(rs, "net_profit"));
        dto.setMarginRate(toBigDecimal(rs, "margin_rate"));
        dto.setAdSales(toBigDecimal(rs, "ad_sales"));
        dto.setOrganicSales(toBigDecimal(rs, "organic_sales"));
        dto.setOrganicSalesRatio(toBigDecimal(rs, "organic_sales_ratio"));
        dto.setRoas(toBigDecimal(rs, "roas"));
        if (rs.getTimestamp("reg_date") != null) {
            dto.setRegDate(rs.getTimestamp("reg_date").toLocalDateTime());
        }
        return dto;
    };

    private static BigDecimal toBigDecimal(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        BigDecimal v = rs.getBigDecimal(column);
        return v != null ? v : null;
    }

    public void save(MarginTrackerDataDto dto) {
        String sql = """
            INSERT INTO margin_tracker_data (
                user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbcTemplate.update(sql,
            dto.getUserId(),
            dto.getProductNumber(),
            dto.getProductName(),
            dto.getOptionId(),
            dto.getOptionAlias(),
            dto.getSaleDate(),
            dto.getSellingPrice(),
            dto.getDiscountCoupon(),
            dto.getFinalSellingPrice(),
            dto.getPriceFluctuation(),
            dto.getSalesQuantity(),
            dto.getActualSalesRevenue(),
            dto.getMarginPerUnit(),
            dto.getTotalMargin(),
            dto.getAdvertisingCost(),
            dto.getAdvertisingCostAdjusted(),
            dto.getNetProfit(),
            dto.getMarginRate(),
            dto.getAdSales(),
            dto.getOrganicSales(),
            dto.getOrganicSalesRatio(),
            dto.getRoas()
        );
    }

    public List<MarginTrackerDataDto> findByUserId(String userId) {
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE user_id = ?
            ORDER BY sale_date DESC, reg_date DESC
            """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    public List<MarginTrackerDataDto> findByUserId(String userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE user_id = ?
            ORDER BY sale_date DESC, reg_date DESC
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, size, offset);
    }

    public int countByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM margin_tracker_data WHERE user_id = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return n != null ? n : 0;
    }

    public java.util.Optional<MarginTrackerDataDto> findByIdAndUserId(Long dataSeq, String userId) {
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE data_seq = ? AND user_id = ?
            """;
        List<MarginTrackerDataDto> list = jdbcTemplate.query(sql, ROW_MAPPER, dataSeq, userId);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    public void update(MarginTrackerDataDto dto) {
        String sql = """
            UPDATE margin_tracker_data SET
                product_number = ?, product_name = ?, option_id = ?, option_alias = ?, sale_date = ?, selling_price = ?, discount_coupon = ?,
                final_selling_price = ?, price_fluctuation = ?, sales_quantity = ?, actual_sales_revenue = ?,
                margin_per_unit = ?, total_margin = ?, advertising_cost = ?, advertising_cost_adjusted = ?,
                net_profit = ?, margin_rate = ?, ad_sales = ?, organic_sales = ?, organic_sales_ratio = ?, roas = ?
            WHERE data_seq = ? AND user_id = ?
            """;
        jdbcTemplate.update(sql,
            dto.getProductNumber(),
            dto.getProductName(),
            dto.getOptionId(),
            dto.getOptionAlias(),
            dto.getSaleDate(),
            dto.getSellingPrice(),
            dto.getDiscountCoupon(),
            dto.getFinalSellingPrice(),
            dto.getPriceFluctuation(),
            dto.getSalesQuantity(),
            dto.getActualSalesRevenue(),
            dto.getMarginPerUnit(),
            dto.getTotalMargin(),
            dto.getAdvertisingCost(),
            dto.getAdvertisingCostAdjusted(),
            dto.getNetProfit(),
            dto.getMarginRate(),
            dto.getAdSales(),
            dto.getOrganicSales(),
            dto.getOrganicSalesRatio(),
            dto.getRoas(),
            dto.getDataSeq(),
            dto.getUserId()
        );
    }

    public boolean deleteByIdAndUserId(Long dataSeq, String userId) {
        String sql = "DELETE FROM margin_tracker_data WHERE data_seq = ? AND user_id = ?";
        int n = jdbcTemplate.update(sql, dataSeq, userId);
        return n > 0;
    }

    /** 해당 회원·상품번호의 모든 매출 데이터 삭제 (상품 삭제 시 연관 데이터 정리) */
    public int deleteByUserIdAndProductNumber(String userId, String productNumber) {
        if (userId == null || productNumber == null || productNumber.isBlank()) return 0;
        String sql = "DELETE FROM margin_tracker_data WHERE user_id = ? AND product_number = ?";
        return jdbcTemplate.update(sql, userId, productNumber);
    }

    /** 같은 날짜, 같은 상품의 모든 기록 조회 (수정 시 모든 옵션 표시용) */
    public List<MarginTrackerDataDto> findByUserIdProductAndDate(String userId, String productNumber, String saleDate) {
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE user_id = ? AND product_number = ? AND sale_date = ?
            ORDER BY reg_date ASC
            """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, productNumber, saleDate);
    }

    /** 해당 상품의 가장 최근 입력 데이터 1건 조회 (판매가 변동 등 이전 값 자동 설정용) */
    public java.util.Optional<MarginTrackerDataDto> findLatestByUserIdAndProductNumber(String userId, String productNumber) {
        if (userId == null || productNumber == null || productNumber.isBlank()) {
            return java.util.Optional.empty();
        }
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE user_id = ? AND product_number = ?
            ORDER BY reg_date DESC
            LIMIT 1
            """;
        List<MarginTrackerDataDto> list = jdbcTemplate.query(sql, ROW_MAPPER, userId, productNumber);
        return list.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(list.get(0));
    }

    /** 해당 상품의 옵션별 가장 최근 판매가 변동 조회 (reg_date 기준, 옵션마다 최신 1건) */
    public List<MarginTrackerDataDto> findLatestRecordsByUserIdAndProductNumber(String userId, String productNumber) {
        if (userId == null || productNumber == null || productNumber.isBlank()) {
            return List.of();
        }
        String sql = """
            SELECT data_seq, user_id, product_number, product_name, option_id, option_alias, sale_date, selling_price, discount_coupon,
                   final_selling_price, price_fluctuation, sales_quantity, actual_sales_revenue,
                   margin_per_unit, total_margin, advertising_cost, advertising_cost_adjusted,
                   net_profit, margin_rate, ad_sales, organic_sales, organic_sales_ratio, roas, reg_date
            FROM margin_tracker_data
            WHERE user_id = ? AND product_number = ?
            ORDER BY reg_date DESC
            LIMIT 500
            """;
        return jdbcTemplate.query(sql, ROW_MAPPER, userId, productNumber);
    }

    /** 상품번호/상품명 목록 (기존 입력 데이터에서 추출) */
    public List<Map<String, String>> findDistinctProductsByUserId(String userId) {
        String sql = """
            SELECT product_number AS productNumber, MAX(product_name) AS productName
            FROM margin_tracker_data
            WHERE user_id = ? AND product_number IS NOT NULL AND product_number != ''
            GROUP BY product_number
            ORDER BY MAX(reg_date) DESC
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("productNumber", rs.getString("productNumber"));
            m.put("productName", rs.getString("productName"));
            return m;
        }, userId);
    }
}
