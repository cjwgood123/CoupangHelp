package com.coupang.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 상품 등록 및 관리 (margin_tracker_product, margin_tracker_product_option).
 * 등록된 상품은 판매 데이터 입력 시 상품번호/상품명 자동 셀렉트에 사용.
 */
@Service
public class MarginTrackerProductService {

    private final JdbcTemplate jdbcTemplate;

    public MarginTrackerProductService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 상품 + 옵션 저장. productSeq가 있으면 수정, 없으면 신규.
     */
    public long saveProduct(String userId, String productNumber, String productName,
                           String shippingCostRange, String commissionRate,
                           List<Map<String, Object>> options, Long productSeq) {
        if (productSeq != null && productSeq > 0) {
            String upd = "UPDATE margin_tracker_product SET product_name=?, shipping_cost_range=?, commission_rate=? WHERE product_seq=? AND user_id=?";
            jdbcTemplate.update(upd, productName, shippingCostRange, commissionRate, productSeq, userId);
            jdbcTemplate.update("DELETE FROM margin_tracker_product_option WHERE product_seq=?", productSeq);
        } else {
            String ins = "INSERT INTO margin_tracker_product (user_id, product_number, product_name, shipping_cost_range, commission_rate) VALUES (?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, userId);
                ps.setString(2, productNumber);
                ps.setString(3, productName);
                ps.setString(4, shippingCostRange);
                ps.setString(5, commissionRate);
                return ps;
            }, kh);
            Number key = kh.getKey();
            productSeq = key != null ? key.longValue() : 0L;
        }
        if (productSeq > 0 && options != null && !options.isEmpty()) {
            String optSql = "INSERT INTO margin_tracker_product_option (product_seq, option_id, option_alias, selling_price, cost_price, margin_per_unit) VALUES (?,?,?,?,?,?)";
            for (Map<String, Object> o : options) {
                BigDecimal selling = toBigDecimal(o.get("sellingPrice"));
                BigDecimal cost = toBigDecimal(o.get("costPrice"));
                // 프론트엔드에서 계산된 marginPerUnit 사용 (판매가 - 입출고/배송비 - 수수료 - 원가)
                BigDecimal margin = toBigDecimal(o.get("marginPerUnit"));
                // marginPerUnit이 없으면 백엔드에서 계산 (하지만 올바른 계산을 위해 프론트엔드에서 전달받는 것이 좋음)
                if (margin == null && selling != null && cost != null) {
                    margin = selling.subtract(cost);
                }
                jdbcTemplate.update(optSql,
                    productSeq,
                    (String) o.get("optionId"),
                    (String) o.get("optionAlias"),
                    selling,
                    cost,
                    margin
                );
            }
        }
        return productSeq;
    }

    /** 회원별 등록 상품 목록 (노출 순서 적용, 판매 입력/매출 일지 탭 순서) */
    public List<Map<String, Object>> findProductsByUserId(String userId) {
        String sql = "SELECT product_seq AS productSeq, product_number AS productNumber, product_name AS productName, shipping_cost_range AS shippingCostRange, commission_rate AS commissionRate, display_order AS displayOrder FROM margin_tracker_product WHERE user_id=? ORDER BY COALESCE(display_order, 999999) ASC, reg_date DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productSeq", rs.getLong("productSeq"));
            m.put("productNumber", rs.getString("productNumber"));
            m.put("productName", rs.getString("productName"));
            m.put("shippingCostRange", rs.getString("shippingCostRange"));
            m.put("commissionRate", rs.getString("commissionRate"));
            int doVal = rs.getInt("displayOrder");
            m.put("displayOrder", rs.wasNull() ? null : doVal);
            return m;
        }, userId);
    }

    /** 상품 노출 순서 저장 (productSeqs 순서대로 1,2,3,... 부여) */
    public void updateDisplayOrder(String userId, List<Long> productSeqs) {
        if (userId == null || productSeqs == null || productSeqs.isEmpty()) return;
        for (int i = 0; i < productSeqs.size(); i++) {
            int order = i + 1;
            Long seq = productSeqs.get(i);
            jdbcTemplate.update("UPDATE margin_tracker_product SET display_order=? WHERE product_seq=? AND user_id=?", order, seq, userId);
        }
    }

    /** 단건 조회 (수정 폼용) */
    public Map<String, Object> findProductBySeqAndUserId(long productSeq, String userId) {
        String sql = "SELECT product_seq AS productSeq, product_number AS productNumber, product_name AS productName, shipping_cost_range AS shippingCostRange, commission_rate AS commissionRate FROM margin_tracker_product WHERE product_seq=? AND user_id=?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("productSeq", rs.getLong("productSeq"));
                m.put("productNumber", rs.getString("productNumber"));
                m.put("productName", rs.getString("productName"));
                m.put("shippingCostRange", rs.getString("shippingCostRange"));
                m.put("commissionRate", rs.getString("commissionRate"));
                return m;
            }, productSeq, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Map<String, Object>> findOptionsByProductSeq(long productSeq) {
        String sql = "SELECT option_seq AS optionSeq, option_id AS optionId, option_alias AS optionAlias, selling_price AS sellingPrice, cost_price AS costPrice, margin_per_unit AS marginPerUnit FROM margin_tracker_product_option WHERE product_seq=? ORDER BY option_seq";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("optionSeq", rs.getLong("optionSeq"));
            m.put("optionId", rs.getString("optionId"));
            m.put("optionAlias", rs.getString("optionAlias"));
            BigDecimal sellingPrice = rs.getBigDecimal("sellingPrice");
            BigDecimal costPrice = rs.getBigDecimal("costPrice");
            BigDecimal marginPerUnit = rs.getBigDecimal("marginPerUnit");
            // BigDecimal을 문자열로 변환하여 JavaScript에서 정확하게 파싱되도록 함
            m.put("sellingPrice", sellingPrice != null ? sellingPrice.toString() : null);
            m.put("costPrice", costPrice != null ? costPrice.toString() : null);
            m.put("marginPerUnit", marginPerUnit != null ? marginPerUnit.toString() : null);
            return m;
        }, productSeq);
    }

    /** 상품 삭제 (해당 회원·상품번호). margin_tracker_product_option은 FK ON DELETE CASCADE로 자동 삭제됨 */
    public boolean deleteByUserIdAndProductNumber(String userId, String productNumber) {
        if (userId == null || productNumber == null || productNumber.isBlank()) return false;
        String sql = "DELETE FROM margin_tracker_product WHERE user_id = ? AND product_number = ?";
        int n = jdbcTemplate.update(sql, userId, productNumber);
        return n > 0;
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try {
            return new BigDecimal(v.toString().replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
}
