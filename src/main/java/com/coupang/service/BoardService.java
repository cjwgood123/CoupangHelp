package com.coupang.service;

import com.coupang.dto.ProductListDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class BoardService {

    private final JdbcTemplate jdbcTemplate;

    /** 조회 대상 월: 2025-11, 2025-12, 2026년 전체 */
    private static final String[] BOARD_MONTHS = {
        "2025-11", "2025-12",
        "2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06",
        "2026-07", "2026-08", "2026-09", "2026-10", "2026-11", "2026-12"
    };

    public BoardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 월과 구매수에 해당하는 모든 테이블을 조회하여 데이터를 반환
     * 예: 2025-11월, 100명 이상 구매 → coupang_products_100_20251117, coupang_products_100_20251118 등
     * 2025-11, 2025-12, 2026년 전체 포함
     * 캐시 2분: 첫 진입·다음 10페이지 반복 조회 시 DB 생략
     */
    /** yymm(연월)별 테이블 상한 */
    private static final int BOARD_TABLES_LIMIT = 90;

    /**
     * 구매했어요: month = yymm "202602" → 해당 연월 테이블만 조회 (최신→과거 순, 테이블 경계 페이징)
     */
    @org.springframework.cache.annotation.Cacheable(value = "boardProducts", key = "#month + '-' + #count + '-' + #offset + '-' + #limit + '-' + (#searchKeyword != null ? #searchKeyword : '')")
    public List<ProductListDto> getProducts(String month, int count, int offset, int limit, String searchKeyword) {
        String yymm = month != null && month.length() >= 6 ? month.replace("-", "").substring(0, 6) : null;
        List<String> tables = findOrderedTables(count, yymm);
        if (tables.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);
        String searchClause = buildSearchClause(searchKeyword);
        int cumulative = 0;
        for (int i = 0; i < tables.size(); i++) {
            String tableName = tables.get(i);
            int tableCount = getTableCount(tableName, categoryExclusionClause, searchClause);
            if (offset < cumulative + tableCount) {
                int localOffset = offset - cumulative;
                List<ProductListDto> chunk = getProductsFromSingleTable(tableName, localOffset, limit,
                    categoryExclusionClause, searchClause, offset + 1);
                int nextRowNum = offset + 1 + chunk.size();
                for (int j = i + 1; j < tables.size() && chunk.size() < limit; j++) {
                    int need = limit - chunk.size();
                    List<ProductListDto> fromNext = getProductsFromSingleTable(tables.get(j), 0, need,
                        categoryExclusionClause, searchClause, nextRowNum);
                    chunk.addAll(fromNext);
                    nextRowNum += fromNext.size();
                }
                return chunk;
            }
            cumulative += tableCount;
        }
        return new ArrayList<>();
    }

    /**
     * 전체 개수: 해당 yymm 테이블들만 합산
     */
    public int getTotalCount(String month, int count, String searchKeyword) {
        String yymm = month != null && month.length() >= 6 ? month.replace("-", "").substring(0, 6) : null;
        List<String> tables = findOrderedTables(count, yymm);
        if (tables.isEmpty()) return 0;
        String categoryExclusionClause = buildCategoryExclusionClause(getExcludedCategories());
        String searchClause = buildSearchClause(searchKeyword);
        int total = 0;
        for (String tableName : tables) {
            total += getTableCount(tableName, categoryExclusionClause, searchClause);
        }
        return total;
    }

    /**
     * DB에 실제 존재하는 연도별 월 목록 (테이블명에서 추출). 2025 → [11,12], 2026 → [1,2,...]
     */
    public List<Integer> getAvailableMonthsForYear(int year) {
        String sql = """
            SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME LIKE 'coupang_products_%'
              AND TABLE_NAME NOT LIKE '%_latest'
              AND TABLE_NAME REGEXP '^coupang_products_[0-9]+_[0-9]{8}$'
              AND TABLE_NAME NOT LIKE 'coupang_products_star'
            """;
        List<String> names = jdbcTemplate.queryForList(sql, String.class);
        Set<Integer> months = new TreeSet<>();
        for (String name : names) {
            if (name == null || name.length() < 8) continue;
            String suffix = name.substring(name.length() - 8);
            if (!suffix.matches("\\d{8}")) continue;
            String yymm = suffix.substring(0, 6);
            if (!yymm.startsWith(String.valueOf(year))) continue;
            int m = Integer.parseInt(yymm.substring(4, 6));
            months.add(m);
        }
        return new ArrayList<>(months);
    }

    /**
     * DB에 실제 있는 yymm 중 가장 마지막(최신) 값. 예: 3월까지 있으면 "202603"
     * 기본 진입 시 이 값을 쓰면 "가진 데이터의 마지막 달"이 활성화됨.
     */
    public String getLatestYymm() {
        String sql = """
            SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME LIKE 'coupang_products_%'
              AND TABLE_NAME NOT LIKE '%_latest'
              AND TABLE_NAME REGEXP '^coupang_products_[0-9]+_[0-9]{8}$'
              AND TABLE_NAME NOT LIKE 'coupang_products_star'
            """;
        List<String> names = jdbcTemplate.queryForList(sql, String.class);
        String maxYymm = null;
        for (String name : names) {
            if (name == null || name.length() < 8) continue;
            String suffix = name.substring(name.length() - 8);
            if (!suffix.matches("\\d{8}")) continue;
            String yymm = suffix.substring(0, 6);
            if (maxYymm == null || yymm.compareTo(maxYymm) > 0) maxYymm = yymm;
        }
        return maxYymm;
    }

    /**
     * DB에 yymm이 없을 때만 사용. 현재 연월 (한국 시간).
     */
    public static String currentYymm() {
        return YearMonth.now(java.time.ZoneId.of("Asia/Seoul"))
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
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

    /**
     * 해당 인원수·연월(yymm) 테이블 목록. yymm null이면 현재 연월. 최신 날짜 순, _latest 제외.
     */
    private List<String> findOrderedTables(int count, String yymm) {
        String prefix = (yymm != null && !yymm.isEmpty())
            ? "coupang_products_" + count + "_" + yymm + "%"
            : "coupang_products_" + count + "_" + currentYymm() + "%";
        String sql = """
            SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE ? AND TABLE_NAME NOT LIKE '%_latest'
            ORDER BY TABLE_NAME DESC LIMIT ?
            """;
        return jdbcTemplate.queryForList(sql, String.class, prefix, BOARD_TABLES_LIMIT);
    }

    private int getTableCount(String tableName, String categoryExclusionClause, String searchClause) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE 1=1 " + categoryExclusionClause + searchClause;
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class);
        return n != null ? n : 0;
    }

    private String buildSearchClause(String searchKeyword) {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) return "";
        String escapedKeyword = searchKeyword.trim().replace("'", "''").replace("%", "\\%").replace("_", "\\_");
        return " AND (title LIKE '%" + escapedKeyword + "%' OR category LIKE '%" + escapedKeyword + "%')";
    }

    /**
     * 단일 테이블 조회 (review_increase NULL). startRowNum으로 전역 순번 부여.
     */
    private List<ProductListDto> getProductsFromSingleTable(String tableName, int offset, int limit,
            String categoryExclusionClause, String searchClause, int startRowNum) {
        String sql = "SELECT seq, title, productID, regidate, url, category, review " +
            "FROM " + tableName + " WHERE 1=1 " + categoryExclusionClause + searchClause +
            " ORDER BY regidate DESC, seq ASC LIMIT ? OFFSET ?";
        final int baseRowNum = startRowNum;
        return jdbcTemplate.query(sql,
            (rs, index) -> new ProductListDto(
                rs.getLong("seq"),
                baseRowNum + index,
                rs.getString("title"),
                rs.getString("productID"),
                rs.getDate("regidate") != null ? rs.getDate("regidate").toLocalDate() : null,
                rs.getString("url"),
                rs.getString("category"),
                rs.getString("review"),
                null),
            limit, offset);
    }

    /**
     * BOARD_MONTHS 기준 테이블 LIKE 조건 (예: TABLE_NAME LIKE 'coupang_products_%_202511%' OR ...)
     */
    private String buildTableMonthLikeClause() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BOARD_MONTHS.length; i++) {
            if (i > 0) sb.append(" OR ");
            String prefix = BOARD_MONTHS[i].replace("-", "");
            sb.append("TABLE_NAME LIKE 'coupang_products_%_").append(prefix).append("%'");
        }
        return sb.toString();
    }

    /**
     * 테이블명에서 count 값 추출
     * 예: coupang_products_100_20251120 → 100
     */
    private int extractCountFromTableName(String tableName) {
        try {
            // 테이블명 형식: coupang_products_{count}_{YYYYMMDD}
            String[] parts = tableName.split("_");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2]);
            }
        } catch (Exception e) {
            // 파싱 실패 시 기본값
        }
        return 0;
    }

    /**
     * 제외 카테고리 목록 조회
     */
    private List<String> getExcludedCategories() {
        try {
            String sql = "SELECT exceptCategory FROM excluded_category";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            // 테이블이 없거나 오류 발생 시 빈 리스트 반환
            return new ArrayList<>();
        }
    }

    /**
     * SQL WHERE 절에 카테고리 제외 조건 추가
     * 제외 카테고리가 없으면 빈 문자열 반환
     * NULL 카테고리는 포함되도록 함 (category IS NULL OR category NOT IN (...))
     */
    private String buildCategoryExclusionClause(List<String> excludedCategories) {
        if (excludedCategories == null || excludedCategories.isEmpty()) {
            return "";
        }
        
        // SQL Injection 방지를 위해 각 카테고리를 따옴표로 감싸고 이스케이프
        // NULL 카테고리도 포함되도록: (category IS NULL OR category NOT IN (...))
        StringBuilder clause = new StringBuilder(" AND (category IS NULL OR category NOT IN (");
        for (int i = 0; i < excludedCategories.size(); i++) {
            if (i > 0) {
                clause.append(", ");
            }
            // 카테고리명에서 작은따옴표를 이스케이프
            String category = excludedCategories.get(i).replace("'", "''");
            clause.append("'").append(category).append("'");
        }
        clause.append("))");
        return clause.toString();
    }


    /**
     * coupang_products_star 테이블에서 star 값으로 필터링된 상품 조회 (로켓 상품 제외)
     * 캐시 2분: 첫 진입·다음 10페이지 반복 조회 시 DB 생략
     */
    @org.springframework.cache.annotation.Cacheable(value = "starProducts", key = "(#star != null ? #star : 'all') + '-' + #offset + '-' + #limit")
    public List<ProductListDto> getStarProducts(Integer star, int offset, int limit) {
        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);
        
        String sql;
        Object[] params;
        
        if (star != null) {
            // 특정 star 값으로 필터링 (로켓 상품 제외)
            sql = """
                SELECT 
                    seq,
                    title,
                    productID,
                    regidate,
                    url,
                    category,
                    review,
                    NULL as review_increase
                FROM coupang_products_star
                WHERE star = ?
                  AND NOT MATCH(html) AGAINST('로켓' IN NATURAL LANGUAGE MODE)
                  """ + categoryExclusionClause + """
                ORDER BY regidate DESC, seq ASC
                LIMIT ? OFFSET ?
                """;
            params = new Object[]{star, limit, offset};
        } else {
            // 전체 상품 조회 (로켓 상품 제외)
            sql = """
                SELECT 
                    seq,
                    title,
                    productID,
                    regidate,
                    url,
                    category,
                    review,
                    NULL as review_increase
                FROM coupang_products_star
                WHERE NOT MATCH(html) AGAINST('로켓' IN NATURAL LANGUAGE MODE)
                  """ + categoryExclusionClause + """
                ORDER BY regidate DESC, seq ASC
                LIMIT ? OFFSET ?
                """;
            params = new Object[]{limit, offset};
        }
        
        final int startRowNum = offset + 1;
        
        return jdbcTemplate.query(sql, 
            (rs, index) -> {
                Long seq = rs.getLong("seq");
                int rowNum = startRowNum + index;
                String title = rs.getString("title");
                String productID = rs.getString("productID");
                LocalDate regidate = rs.getDate("regidate") != null 
                    ? rs.getDate("regidate").toLocalDate() 
                    : null;
                String url = rs.getString("url");
                String category = rs.getString("category");
                String review = rs.getString("review");
                return new ProductListDto(seq, rowNum, title, productID, regidate, url, category, review, null);
            },
            params);
    }

    /**
     * coupang_products_star 테이블에서 star 값으로 필터링된 총 개수 조회 (로켓 상품 제외)
     */
    public int getTotalStarCount(Integer star) {
        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);
        
        if (star != null) {
            String sql = "SELECT COUNT(*) FROM coupang_products_star WHERE star = ? AND NOT MATCH(html) AGAINST('로켓' IN NATURAL LANGUAGE MODE)" + categoryExclusionClause;
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class, star);
            return result != null ? result : 0;
        } else {
            String sql = "SELECT COUNT(*) FROM coupang_products_star WHERE NOT MATCH(html) AGAINST('로켓' IN NATURAL LANGUAGE MODE)" + categoryExclusionClause;
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
            return result != null ? result : 0;
        }
    }

    /**
     * coupang_products_star 테이블에 존재하는 모든 star 값 목록 조회 (정렬된 순서, 로켓 상품 제외)
     */
    public List<Integer> getAvailableStarValues() {
        String sql = """
            SELECT DISTINCT star 
            FROM coupang_products_star 
            WHERE star IS NOT NULL
              AND NOT MATCH(html) AGAINST('로켓' IN NATURAL LANGUAGE MODE)
            ORDER BY star ASC
            """;
        
        return jdbcTemplate.queryForList(sql, Integer.class);
    }

    /**
     * coupang_satisfied 테이블에서 특정 count에 해당하는 상품 조회
     * 예: coupang_satisfied_100_20251120, coupang_satisfied_200_20251120 등
     */
    public List<ProductListDto> getSatisfiedProducts(int count, int offset, int limit) {
        // 존재하는 테이블 목록 조회
        List<String> tableNames = findExistingSatisfiedTables(count);
        
        if (tableNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);

        // UNION ALL 쿼리 생성
        StringBuilder unionSql = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                unionSql.append(" UNION ALL ");
            }
            unionSql.append("SELECT seq, title, productID, regidate, url, category, review FROM ").append(tableNames.get(i));
            if (!categoryExclusionClause.isEmpty()) {
                unionSql.append(" WHERE 1=1").append(categoryExclusionClause);
            }
        }

        // 최신 productID만 선택
        String sql = """
            WITH all_data AS (
                """ + unionSql.toString() + """
            ),
            latest_data AS (
                SELECT *,
                       ROW_NUMBER() OVER (PARTITION BY productID ORDER BY regidate DESC, seq DESC) as rn
                FROM all_data
            )
            SELECT 
                l.seq,
                l.title,
                l.productID,
                l.regidate,
                l.url,
                l.category,
                l.review,
                NULL as review_increase
            FROM latest_data l
            WHERE l.rn = 1
            ORDER BY l.regidate DESC, l.seq ASC
            LIMIT ? OFFSET ?
            """;

        final int startRowNum = offset + 1;
        
        return jdbcTemplate.query(sql, 
            (rs, index) -> {
                Long seq = rs.getLong("seq");
                int rowNum = startRowNum + index;
                String title = rs.getString("title");
                String productID = rs.getString("productID");
                LocalDate regidate = rs.getDate("regidate") != null 
                    ? rs.getDate("regidate").toLocalDate() 
                    : null;
                String url = rs.getString("url");
                String category = rs.getString("category");
                String review = rs.getString("review");
                return new ProductListDto(seq, rowNum, title, productID, regidate, url, category, review, null);
            },
            limit, offset);
    }

    /**
     * coupang_satisfied 테이블에서 특정 count에 해당하는 총 개수 조회
     */
    public int getTotalSatisfiedCount(int count) {
        List<String> tableNames = findExistingSatisfiedTables(count);
        
        if (tableNames.isEmpty()) {
            return 0;
        }

        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);

        // UNION ALL 쿼리 생성
        StringBuilder unionSql = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                unionSql.append(" UNION ALL ");
            }
            unionSql.append("SELECT productID FROM ").append(tableNames.get(i));
            if (!categoryExclusionClause.isEmpty()) {
                unionSql.append(" WHERE 1=1").append(categoryExclusionClause);
            }
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
     * coupang_satisfied 테이블 중 특정 count에 해당하는 존재하는 테이블 목록 조회
     * 예: 100 → coupang_satisfied_100_20251120, coupang_satisfied_100_20251121 등
     */
    private List<String> findExistingSatisfiedTables(int count) {
        // 테이블명 패턴: coupang_satisfied_{count}_{YYYYMMDD}
        String tablePattern = "coupang_satisfied_" + count + "_%";
        
        String sql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME LIKE ?
            ORDER BY TABLE_NAME DESC
            """;
        
        return jdbcTemplate.queryForList(sql, String.class, tablePattern);
    }

    /**
     * 오늘 날짜에 업데이트된 구매했어요 상품 개수 조회
     * 오늘 날짜의 coupang_products_* 테이블에서 중복 제거된 productID 개수
     * 캐시 제거: DB 업데이트 시마다 실시간으로 반영되도록 함
     */
    public int getTodayPurchasedCount() {
        // 오늘 날짜 형식: YYYYMMDD
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 오늘 날짜의 모든 coupang_products 테이블 찾기
        String tablePattern = "coupang_products_%_" + today;
        
        String findTablesSql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME LIKE ?
            """;
        
        List<String> tableNames = jdbcTemplate.queryForList(findTablesSql, String.class, tablePattern);
        
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
     * 오늘 날짜에 업데이트된 만족했어요 상품 개수 조회
     * 오늘 날짜의 coupang_satisfied_* 테이블에서 중복 제거된 productID 개수
     * 캐시 제거: DB 업데이트 시마다 실시간으로 반영되도록 함
     */
    public int getTodaySatisfiedCount() {
        // 오늘 날짜 형식: YYYYMMDD
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 오늘 날짜의 모든 coupang_satisfied 테이블 찾기
        String tablePattern = "coupang_satisfied_%_" + today;
        
        String findTablesSql = """
            SELECT TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME LIKE ?
            """;
        
        List<String> tableNames = jdbcTemplate.queryForList(findTablesSql, String.class, tablePattern);
        
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
     * 단기간 인기 상품 개수 조회
     * 같은 상품이 더 낮은 구매자 수 테이블에서 더 높은 구매자 수 테이블로 이동한 경우를 찾음
     * 예: 100명 이상 테이블에 있던 상품이 200명 이상 테이블에 나타나는 경우
     * 성능 최적화: 캐싱 사용 (10분)
     */
    @org.springframework.cache.annotation.Cacheable(value = "shortTermPopular", unless = "#result == null")
    public int getShortTermPopularCount() {
        int[] counts = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000};
        
        java.util.Set<String> popularProducts = new java.util.HashSet<>();
        
        for (String month : BOARD_MONTHS) {
            for (int i = 0; i < counts.length - 1; i++) {
                int lowerCount = counts[i];
                int higherCount = counts[i + 1];
                
                // 낮은 구매자 수 테이블의 모든 테이블
                List<String> lowerTableNames = findExistingTables(month, lowerCount);
                if (lowerTableNames.isEmpty()) {
                    continue;
                }
                
                // 높은 구매자 수 테이블의 모든 테이블
                List<String> higherTableNames = findExistingTables(month, higherCount);
                if (higherTableNames.isEmpty()) {
                    continue;
                }
                
                // 각 테이블 그룹에서 최신 날짜의 상품들 조회
                String lowerProductsSql = buildLatestProductsQuery(lowerTableNames);
                String higherProductsSql = buildLatestProductsQuery(higherTableNames);
                
                // 낮은 구매자 수 테이블에 있던 상품이 높은 구매자 수 테이블에 나타나는지 확인
                String sql = """
                    SELECT COUNT(DISTINCT lower_products.productID)
                    FROM (
                        """ + lowerProductsSql + """
                    ) as lower_products
                    INNER JOIN (
                        """ + higherProductsSql + """
                    ) as higher_products
                    ON lower_products.productID = higher_products.productID
                    WHERE higher_products.max_regidate > lower_products.max_regidate
                    """;
                
                try {
                    Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
                    if (count != null && count > 0) {
                        // 중복 제거를 위해 개별 상품 ID 조회
                        String detailSql = """
                            SELECT DISTINCT lower_products.productID
                            FROM (
                                """ + lowerProductsSql + """
                            ) as lower_products
                            INNER JOIN (
                                """ + higherProductsSql + """
                            ) as higher_products
                            ON lower_products.productID = higher_products.productID
                            WHERE higher_products.max_regidate > lower_products.max_regidate
                            """;
                        List<String> productIds = jdbcTemplate.queryForList(detailSql, String.class);
                        popularProducts.addAll(productIds);
                    }
                } catch (Exception e) {
                    // 테이블이 없거나 쿼리 오류 시 무시하고 계속
                    continue;
                }
            }
        }
        
        // 중복 제거된 상품 개수 반환
        return popularProducts.size();
    }

    /**
     * 최신 날짜의 상품들을 조회하는 쿼리 생성
     */
    private String buildLatestProductsQuery(List<String> tableNames) {
        if (tableNames.isEmpty()) {
            return "SELECT productID, '1900-01-01' as max_regidate FROM (SELECT 1) as dummy WHERE 1=0";
        }
        
        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);
        
        // UNION ALL 쿼리 생성
        StringBuilder unionSql = new StringBuilder();
        for (int i = 0; i < tableNames.size(); i++) {
            if (i > 0) {
                unionSql.append(" UNION ALL ");
            }
            unionSql.append("SELECT productID, regidate FROM ").append(tableNames.get(i));
            if (!categoryExclusionClause.isEmpty()) {
                unionSql.append(" WHERE 1=1").append(categoryExclusionClause);
            }
        }
        
        // 각 productID별 최신 날짜만 선택
        return """
            SELECT productID, MAX(regidate) as max_regidate
            FROM (
                """ + unionSql.toString() + """
            ) as all_data
            GROUP BY productID
            """;
    }

    /**
     * 단기간 인기 상품 목록 조회 (상승폭 포함) - 최적화된 한 방 SQL
     * 모든 coupang_products_ 테이블을 UNION ALL로 합쳐서 CTE로 처리
     * 캐시 2분: 첫 진입·다음 10페이지 반복 조회 시 DB 생략
     */
    @org.springframework.cache.annotation.Cacheable(value = "shortTermProducts", key = "#offset + '-' + #limit")
    public List<ProductListDto> getShortTermPopularProducts(int offset, int limit) {
        // 1) BOARD_MONTHS(2025-11~2026-12) 해당 모든 coupang_products_* 테이블 조회
        String findTablesSql = "SELECT TABLE_NAME " +
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND (" + buildTableMonthLikeClause() + ") " +
            "ORDER BY TABLE_NAME";
        
        List<String> allTableNames = jdbcTemplate.queryForList(findTablesSql, String.class);
        
        if (allTableNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);

        // 2) all_data CTE용 UNION ALL SQL 생성 (10000 미만만 포함)
        StringBuilder allDataSql = new StringBuilder();
        boolean first = true;
        for (String tableName : allTableNames) {
            int countBand = extractCountFromTableName(tableName); // ex) coupang_products_100_... -> 100
            // 10000 이상은 제외
            if (countBand >= 10000) {
                continue;
            }
            if (!first) {
                allDataSql.append(" UNION ALL ");
            }
            first = false;
            allDataSql.append("SELECT seq, title, productID, regidate, url, category, review, ")
                      .append(countBand).append(" AS count_band ")
                      .append("FROM ").append(tableName);
            if (!categoryExclusionClause.isEmpty()) {
                allDataSql.append(" WHERE 1=1").append(categoryExclusionClause);
            }
        }
        
        // 10000 미만 테이블이 없으면 빈 리스트 반환
        if (first) {
            return new ArrayList<>();
        }
        
        String sql = """
            WITH all_data AS (
                """ + allDataSql.toString() + """
            ),
            -- 각 상품 / 각 밴드(count_band)별 최신 날짜
            latest_per_band AS (
                SELECT 
                    productID,
                    count_band,
                    MAX(regidate) AS max_regidate
                FROM all_data
                GROUP BY productID, count_band
            ),
            -- 낮은 밴드에서 높은 밴드로, 날짜도 뒤로 이동한 경우만 후보 (동일 날짜 제외)
            movement_candidates AS (
                SELECT
                    l.productID,
                    l.count_band AS from_count,
                    h.count_band AS to_count,
                    l.max_regidate AS from_date,
                    h.max_regidate AS to_date,
                    (h.count_band - l.count_band) AS rise_count,
                    DATEDIFF(h.max_regidate, l.max_regidate) AS days_diff
                FROM latest_per_band l
                JOIN latest_per_band h
                  ON h.productID = l.productID
                 AND h.count_band > l.count_band
                 AND h.max_regidate > l.max_regidate
                 AND DATEDIFF(h.max_regidate, l.max_regidate) > 0
            ),
            -- 한 상품에 여러 이동이 있으면, 상승폭 큰 것 우선, 소요일자는 짧을수록 우선
            best_movement AS (
                SELECT
                    *,
                    ROW_NUMBER() OVER (
                        PARTITION BY productID
                        ORDER BY rise_count DESC, days_diff ASC
                    ) AS rn
                FROM movement_candidates
            ),
            -- 최종 이동 정보를 상품 최신 상세정보와 합치기
            latest_detail AS (
                SELECT
                    ad.seq,
                    ad.title,
                    ad.productID,
                    ad.regidate,
                    ad.url,
                    ad.category,
                    ad.review,
                    bm.from_count,
                    bm.to_count,
                    bm.rise_count,
                    bm.from_date,
                    bm.to_date,
                    bm.days_diff,
                    ROW_NUMBER() OVER (
                        PARTITION BY ad.productID
                        ORDER BY ad.regidate DESC, ad.seq DESC
                    ) AS rn
                FROM all_data ad
                JOIN best_movement bm
                  ON ad.productID = bm.productID
                 AND bm.rn = 1
            )
            SELECT
                seq,
                title,
                productID,
                regidate,
                url,
                category,
                review,
                rise_count,
                from_count,
                to_count,
                from_date,
                to_date,
                days_diff
            FROM latest_detail
            WHERE rn = 1
            ORDER BY regidate DESC, seq ASC, rise_count DESC, days_diff ASC
            LIMIT ? OFFSET ?
            """;
        
        try {
            final int startRowNum = offset + 1;
            return jdbcTemplate.query(sql,
                (rs, index) -> {
                    Long seq = rs.getLong("seq");
                    String title = rs.getString("title");
                    String productID = rs.getString("productID");
                    LocalDate regidate = rs.getDate("regidate") != null
                            ? rs.getDate("regidate").toLocalDate()
                            : null;
                    String url = rs.getString("url");
                    String category = rs.getString("category");
                    String review = rs.getString("review");
                    Integer riseCount = rs.getObject("rise_count") != null
                            ? rs.getInt("rise_count")
                            : null;
                    Integer fromCount = rs.getObject("from_count") != null
                            ? rs.getInt("from_count")
                            : null;
                    Integer toCount = rs.getObject("to_count") != null
                            ? rs.getInt("to_count")
                            : null;
                    LocalDate fromDate = rs.getDate("from_date") != null
                            ? rs.getDate("from_date").toLocalDate()
                            : null;
                    LocalDate toDate = rs.getDate("to_date") != null
                            ? rs.getDate("to_date").toLocalDate()
                            : null;
                    Integer daysDiff = rs.getObject("days_diff") != null
                            ? rs.getInt("days_diff")
                            : null;
                    int rowNum = startRowNum + index;
                    return new ProductListDto(
                            seq, rowNum, title, productID, regidate,
                            url, category, review, riseCount,
                            fromCount, toCount, null, null, fromDate, toDate, daysDiff
                    );
                },
                limit, offset);
        } catch (Exception e) {
            // 쿼리 오류 시 빈 리스트 반환
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 단기간 인기 상품 총 개수 조회 - 최적화된 한 방 SQL
     */
    public int getShortTermPopularTotalCount() {
        // BOARD_MONTHS(2025-11~2026-12) 해당 모든 coupang_products_ 테이블 찾기
        String findTablesSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_SCHEMA = DATABASE() AND (" + buildTableMonthLikeClause() + ") ORDER BY TABLE_NAME";
        
        List<String> allTableNames = jdbcTemplate.queryForList(findTablesSql, String.class);
        
        if (allTableNames.isEmpty()) {
            return 0;
        }
        
        // 제외 카테고리 목록 조회
        List<String> excludedCategories = getExcludedCategories();
        String categoryExclusionClause = buildCategoryExclusionClause(excludedCategories);

        // all_data CTE 생성 (10000 미만만 포함)
        StringBuilder allDataSql = new StringBuilder();
        boolean first = true;
        for (String tableName : allTableNames) {
            // 테이블명에서 count 추출
            int count = extractCountFromTableName(tableName);
            // 10000 이상은 제외
            if (count >= 10000) {
                continue;
            }
            if (!first) {
                allDataSql.append(" UNION ALL ");
            }
            first = false;
            allDataSql.append("SELECT productID, regidate, ")
                      .append(count).append(" as count_band ")
                      .append("FROM ").append(tableName);
            if (!categoryExclusionClause.isEmpty()) {
                allDataSql.append(" WHERE 1=1").append(categoryExclusionClause);
            }
        }
        
        // 10000 미만 테이블이 없으면 0 반환
        if (first) {
            return 0;
        }
        
        // 카운트만 조회하는 SQL
        String sql = """
            WITH all_data AS (
                """ + allDataSql.toString() + """
            ),
            latest_per_band AS (
                SELECT 
                    productID,
                    count_band,
                    MAX(regidate) as max_regidate
                FROM all_data
                GROUP BY productID, count_band
            ),
            movement AS (
                SELECT DISTINCT
                    l.productID
                FROM latest_per_band l
                INNER JOIN latest_per_band h
                    ON l.productID = h.productID
                    AND h.count_band > l.count_band
                    AND h.max_regidate > l.max_regidate
                    AND DATEDIFF(h.max_regidate, l.max_regidate) > 0
            )
            SELECT COUNT(DISTINCT productID)
            FROM movement
            """;
        
        try {
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
            return result != null ? result : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 상위 카테고리 10개 조회
     * 100~9000까지 모든 구매했어요 테이블에서 카테고리별 집계
     */
    public List<CategoryCount> getTopCategories() {
        String month = "2025-11";
        String monthPrefix = month.replace("-", "");
        
        // 100~9000까지의 모든 coupang_products_ 테이블 찾기
        String findTablesSql = "SELECT TABLE_NAME " +
            "FROM INFORMATION_SCHEMA.TABLES " +
            "WHERE TABLE_SCHEMA = DATABASE() " +
            "AND TABLE_NAME LIKE 'coupang_products_%_" + monthPrefix + "%' " +
            "ORDER BY TABLE_NAME";
        
        List<String> allTableNames = jdbcTemplate.queryForList(findTablesSql, String.class);
        
        if (allTableNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        // all_data CTE 생성: 100~9000까지의 테이블만 포함, 카테고리가 "-"가 아닌 것만
        StringBuilder allDataSql = new StringBuilder();
        boolean first = true;
        for (String tableName : allTableNames) {
            int countBand = extractCountFromTableName(tableName);
            // 100~9000만 포함
            if (countBand < 100 || countBand >= 10000) {
                continue;
            }
            if (!first) {
                allDataSql.append(" UNION ALL ");
            }
            first = false;
            allDataSql.append("SELECT category FROM ").append(tableName)
                      .append(" WHERE category IS NOT NULL AND category != '' AND category != '-'");
        }
        
        if (first) {
            return new ArrayList<>();
        }
        
        // 카테고리별 집계 및 상위 10개 조회
        String sql = """
            WITH all_data AS (
                """ + allDataSql.toString() + """
            )
            SELECT 
                category,
                COUNT(*) as cnt
            FROM all_data
            GROUP BY category
            ORDER BY cnt DESC
            LIMIT 10
            """;
        
        try {
            return jdbcTemplate.query(sql,
                (rs, index) -> {
                    String category = rs.getString("category");
                    int count = rs.getInt("cnt");
                    return new CategoryCount(category, count);
                });
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 카테고리별 개수 DTO
     */
    public static class CategoryCount {
        private String category;
        private int count;

        public CategoryCount(String category, int count) {
            this.category = category;
            this.count = count;
        }

        public String getCategory() {
            return category;
        }

        public int getCount() {
            return count;
        }
    }

}

