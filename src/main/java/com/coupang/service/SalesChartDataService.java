package com.coupang.service;

import com.coupang.dto.SalesChartDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesChartDataService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SalesChartDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SalesChartDataDto> dataRowMapper = (rs, rowNum) -> {
        SalesChartDataDto data = new SalesChartDataDto();
        data.setDataSeq(rs.getLong("data_seq"));
        data.setUserId(rs.getString("user_id"));
        data.setExtractDate(rs.getString("extract_date"));
        data.setDayOfWeek(rs.getString("day_of_week"));
        data.setProductNumber(rs.getString("product_number"));
        data.setTime(rs.getString("time"));
        data.setSalesEstimate(rs.getBigDecimal("sales_estimate"));
        data.setRevenueEstimate(rs.getInt("revenue_estimate"));
        if (rs.getTimestamp("reg_date") != null) {
            data.setRegDate(rs.getTimestamp("reg_date").toLocalDateTime());
        }
        return data;
    };

    /**
     * 판매 데이터 저장 (배치)
     */
    public void saveSalesData(List<SalesChartDataDto> dataList) {
        String sql = """
            INSERT INTO sales_chart_data (user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            """;
        
        jdbcTemplate.batchUpdate(sql, dataList, dataList.size(),
            (ps, data) -> {
                ps.setString(1, data.getUserId());
                ps.setString(2, data.getExtractDate());
                ps.setString(3, data.getDayOfWeek());
                ps.setString(4, data.getProductNumber());
                ps.setString(5, data.getTime());
                ps.setBigDecimal(6, data.getSalesEstimate());
                ps.setInt(7, data.getRevenueEstimate());
            });
    }

    /**
     * 사용자별 데이터 조회 (날짜별)
     */
    public List<SalesChartDataDto> getDataByUserAndDate(String userId, String extractDate) {
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ? AND extract_date = ?
            ORDER BY time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId, extractDate);
    }

    /**
     * 사용자별 데이터 조회 (기간별)
     */
    public List<SalesChartDataDto> getDataByUserAndDateRange(String userId, String startDate, String endDate) {
        // 날짜 문자열을 숫자로 변환하여 비교 (예: "1월19일" -> 119)
        // SUBSTRING_INDEX를 사용하여 월과 일 추출
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ?
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) >= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) <= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            ORDER BY extract_date ASC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId, startDate, startDate, endDate, endDate);
    }
    
    /**
     * 사용자별 데이터 조회 (기간별 + 요일 필터)
     * dayFilter: 쉼표로 구분된 여러 요일 (예: "월,화,수" 또는 "월")
     */
    public List<SalesChartDataDto> getDataByUserAndDateRangeWithDayFilter(String userId, String startDate, String endDate, String dayFilter) {
        String dayCondition = "";
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(startDate);
        params.add(startDate);
        params.add(endDate);
        params.add(endDate);
        
        if (dayFilter != null && !dayFilter.trim().isEmpty()) {
            // 쉼표로 구분된 요일들을 파싱
            String[] days = dayFilter.split(",");
            if (days.length > 0) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < days.length; i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                    params.add(days[i].trim());
                }
                dayCondition = "AND day_of_week IN (" + placeholders.toString() + ")";
            }
        }
        
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ?
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) >= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) <= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            """ + dayCondition + """
            ORDER BY extract_date ASC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, params.toArray());
    }

    /**
     * 사용자별 상품번호별 데이터 조회
     */
    public List<SalesChartDataDto> getDataByUserAndProduct(String userId, String productNumber) {
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ? AND product_number = ?
            ORDER BY extract_date ASC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId, productNumber);
    }

    /**
     * 사용자별 모든 데이터 조회
     */
    public List<SalesChartDataDto> getAllDataByUser(String userId) {
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ?
            ORDER BY extract_date DESC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId);
    }

    /**
     * 사용자별 고유한 추출일자 목록 조회
     */
    public List<String> getDistinctExtractDates(String userId) {
        String sql = """
            SELECT DISTINCT extract_date
            FROM sales_chart_data
            WHERE user_id = ?
            ORDER BY extract_date DESC
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    /**
     * 사용자별 고유한 상품번호 목록 조회
     */
    public List<String> getDistinctProductNumbers(String userId) {
        String sql = """
            SELECT DISTINCT product_number
            FROM sales_chart_data
            WHERE user_id = ?
            ORDER BY product_number ASC
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, userId);
    }

    /**
     * 사용자별 데이터 조회 (날짜 + 상품번호)
     */
    public List<SalesChartDataDto> getDataByUserDateAndProduct(String userId, String extractDate, String productNumber) {
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ? AND extract_date = ? AND product_number = ?
            ORDER BY time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId, extractDate, productNumber);
    }

    /**
     * 사용자별 데이터 조회 (기간 + 상품번호)
     */
    public List<SalesChartDataDto> getDataByUserDateRangeAndProduct(String userId, String startDate, String endDate, String productNumber) {
        // 날짜 문자열을 숫자로 변환하여 비교
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ?
            AND product_number = ?
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) >= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) <= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            ORDER BY extract_date ASC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, userId, productNumber, startDate, startDate, endDate, endDate);
    }
    
    /**
     * 사용자별 데이터 조회 (기간 + 상품번호 + 요일 필터)
     */
    public List<SalesChartDataDto> getDataByUserDateRangeAndProductWithDayFilter(String userId, String startDate, String endDate, String productNumber, String dayFilter) {
        // dayFilter: 쉼표로 구분된 여러 요일 (예: "월,화,수" 또는 "월")
        String dayCondition = "";
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(productNumber);
        params.add(startDate);
        params.add(startDate);
        params.add(endDate);
        params.add(endDate);
        
        if (dayFilter != null && !dayFilter.trim().isEmpty()) {
            // 쉼표로 구분된 요일들을 파싱
            String[] days = dayFilter.split(",");
            if (days.length > 0) {
                StringBuilder placeholders = new StringBuilder();
                for (int i = 0; i < days.length; i++) {
                    if (i > 0) placeholders.append(",");
                    placeholders.append("?");
                    params.add(days[i].trim());
                }
                dayCondition = "AND day_of_week IN (" + placeholders.toString() + ")";
            }
        }
        
        String sql = """
            SELECT data_seq, user_id, extract_date, day_of_week, product_number, time, sales_estimate, revenue_estimate, reg_date
            FROM sales_chart_data
            WHERE user_id = ?
            AND product_number = ?
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) >= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            AND (
                CAST(SUBSTRING_INDEX(extract_date, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(extract_date, '월', -1), '일', 1) AS UNSIGNED)
            ) <= (
                CAST(SUBSTRING_INDEX(?, '월', 1) AS UNSIGNED) * 100 
                + CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(?, '월', -1), '일', 1) AS UNSIGNED)
            )
            """ + dayCondition + """
            ORDER BY extract_date ASC, time ASC
            """;
        
        return jdbcTemplate.query(sql, dataRowMapper, params.toArray());
    }
}

