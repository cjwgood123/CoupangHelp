package com.coupang.service;

import com.coupang.dto.ProductListDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService {

    private final JdbcTemplate jdbcTemplate;

    public BoardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 월과 구매수에 해당하는 모든 테이블을 조회하여 데이터를 반환
     * 예: 2025-11월, 100명 이상 구매 → coupang_products_100_20251117, coupang_products_100_20251118 등
     */
    public List<ProductListDto> getProducts(String month, int count, int offset, int limit) {
        // 존재하는 테이블 목록 조회
        List<String> tableNames = findExistingTables(month, count);
        
        if (tableNames.isEmpty()) {
            return new ArrayList<>();
        }

        // UNION ALL 쿼리 생성
        StringBuilder unionSql = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                unionSql.append(" UNION ALL ");
            }
            unionSql.append("SELECT seq, title, productID, regidate, url, category, review FROM ").append(tableNames.get(i));
        }

        // 최신 productID만 선택하고, 이전 review와 비교하여 증가량 계산
        String sql = """
            WITH all_data AS (
                """ + unionSql.toString() + """
            ),
            latest_data AS (
                SELECT *,
                       ROW_NUMBER() OVER (PARTITION BY productID ORDER BY regidate DESC, seq DESC) as rn
                FROM all_data
            ),
            previous_review AS (
                SELECT 
                    l.productID,
                    l.regidate as current_regidate,
                    (
                        SELECT review 
                        FROM all_data 
                        WHERE productID = l.productID 
                          AND regidate < l.regidate
                          AND review IS NOT NULL 
                          AND review != ''
                        ORDER BY regidate DESC 
                        LIMIT 1
                    ) as prev_review
                FROM latest_data l
                WHERE l.rn = 1
            )
            SELECT 
                l.seq,
                l.title,
                l.productID,
                l.regidate,
                l.url,
                l.category,
                l.review,
                CASE 
                    WHEN l.review IS NOT NULL AND l.review != '' 
                         AND p.prev_review IS NOT NULL AND p.prev_review != ''
                         AND CAST(l.review AS UNSIGNED) > CAST(p.prev_review AS UNSIGNED)
                    THEN CAST(l.review AS UNSIGNED) - CAST(p.prev_review AS UNSIGNED)
                    ELSE NULL
                END as review_increase
            FROM latest_data l
            LEFT JOIN previous_review p ON l.productID = p.productID AND l.regidate = p.current_regidate
            WHERE l.rn = 1
            ORDER BY l.regidate DESC, l.seq ASC
            LIMIT ? OFFSET ?
            """;

        // 조회일자 기준으로 순번을 매기기 위해 offset을 고려
        final int startRowNum = offset + 1; // offset이 0이면 1번부터 시작
        
        return jdbcTemplate.query(sql, 
            (rs, index) -> {
                Long seq = rs.getLong("seq");
                int rowNum = startRowNum + index; // offset을 고려한 순번
                String title = rs.getString("title");
                String productID = rs.getString("productID");
                LocalDate regidate = rs.getDate("regidate") != null 
                    ? rs.getDate("regidate").toLocalDate() 
                    : null;
                String url = rs.getString("url");
                String category = rs.getString("category");
                String review = rs.getString("review");
                Integer reviewIncrease = rs.getObject("review_increase") != null 
                    ? rs.getInt("review_increase") 
                    : null;
                return new ProductListDto(seq, rowNum, title, productID, regidate, url, category, review, reviewIncrease);
            },
            limit, offset);
    }

    /**
     * 전체 개수 조회 (중복 제거된 productID 개수)
     */
    public int getTotalCount(String month, int count) {
        List<String> tableNames = findExistingTables(month, count);
        
        if (tableNames.isEmpty()) {
            return 0;
        }

        // UNION ALL 쿼리 생성
        StringBuilder unionSql = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                unionSql.append(" UNION ALL ");
            }
            unionSql.append("SELECT productID FROM ").append(tableNames.get(i));
        }

        // 중복 제거된 productID 개수
        String sql = """
            SELECT COUNT(DISTINCT productID) 
            FROM (
                """ + unionSql.toString() + """
            ) as combined
            """;

        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    /**
     * 전체 상품 수 조회 (모든 coupang_products 테이블의 총합)
     */
    public int getTotalProductCount() {
        // 모든 coupang_products 테이블을 찾아서 각각 COUNT 후 합산
        String findTablesSql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME LIKE 'coupang_products_%'
            """;
        
        List<String> tableNames = jdbcTemplate.queryForList(findTablesSql, String.class);
        
        if (tableNames.isEmpty()) {
            return 0;
        }
        
        StringBuilder countSql = new StringBuilder("SELECT SUM(cnt) FROM (");
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                countSql.append(" UNION ALL ");
            }
            countSql.append("SELECT COUNT(*) as cnt FROM ").append(tableNames.get(i));
        }
        countSql.append(") as total");
        
        Integer result = jdbcTemplate.queryForObject(countSql.toString(), Integer.class);
        return result != null ? result : 0;
    }

    /**
     * 특정 월과 구매수에 해당하는 존재하는 테이블 목록 조회
     * 예: 2025-11, 100 → coupang_products_100_20251117, coupang_products_100_20251118 등
     */
    private List<String> findExistingTables(String month, int count) {
        // month 형식: "2025-11" → "202511"로 변환
        String monthPrefix = month.replace("-", "");
        
        // 테이블명 패턴: coupang_products_{count}_{YYYYMMDD}
        String tablePattern = "coupang_products_" + count + "_" + monthPrefix + "%";
        
        String sql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME LIKE ?
            ORDER BY TABLE_NAME DESC
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, tablePattern);
    }
}

