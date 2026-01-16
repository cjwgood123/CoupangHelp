package com.coupang.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShareService {

    private final JdbcTemplate jdbcTemplate;

    public ShareService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 공유 코드로 방문 기록 (share_referrals 테이블에 기록 없으면 생성, 있으면 업데이트)
     * share_visits 테이블에 방문 기록 추가
     */
    public void recordVisit(String shareCode, String ipAddress, String userAgent) {
        try {
            // share_referrals 테이블에 코드가 없으면 생성
            String checkSql = "SELECT COUNT(*) FROM share_referrals WHERE share_code = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, shareCode);
            
            if (count == null || count == 0) {
                // 새로 생성
                String insertSql = "INSERT INTO share_referrals (share_code, total_visits, unique_visits) VALUES (?, 1, 1)";
                jdbcTemplate.update(insertSql, shareCode);
            } else {
                // 총 방문 횟수 증가
                String updateSql = "UPDATE share_referrals SET total_visits = total_visits + 1 WHERE share_code = ?";
                jdbcTemplate.update(updateSql, shareCode);
            }
            
            // share_visits 테이블에 방문 기록 추가
            String visitSql = "INSERT INTO share_visits (share_code, ip_address, user_agent) VALUES (?, ?, ?)";
            jdbcTemplate.update(visitSql, shareCode, ipAddress, userAgent);
            
        } catch (Exception e) {
            // 오류 발생 시 로그만 남기고 계속 진행 (리다이렉트는 정상 동작)
            System.err.println("Error recording visit for share_code: " + shareCode + ", error: " + e.getMessage());
        }
    }

    /**
     * 공유 코드의 통계 정보 조회
     */
    public ShareStats getShareStats(String shareCode) {
        try {
            String sql = "SELECT total_visits, unique_visits FROM share_referrals WHERE share_code = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                int totalVisits = rs.getInt("total_visits");
                int uniqueVisits = rs.getInt("unique_visits");
                return new ShareStats(shareCode, totalVisits, uniqueVisits);
            }, shareCode);
        } catch (Exception e) {
            return new ShareStats(shareCode, 0, 0);
        }
    }

    /**
     * 다음 공유 코드 생성 (DB에서 최대값 조회 후 +1)
     * 동시성 문제를 방지하기 위해 트랜잭션 처리 필요할 수 있음
     */
    public String getNextShareCode() {
        try {
            // 현재 최대 share_code 조회 (share_1, share_2 형식)
            String sql = "SELECT share_code FROM share_referrals WHERE share_code LIKE 'share_%' ORDER BY CAST(SUBSTRING(share_code, 7) AS UNSIGNED) DESC LIMIT 1";
            String maxCode = null;
            try {
                maxCode = jdbcTemplate.queryForObject(sql, String.class);
            } catch (Exception e) {
                // 테이블이 비어있거나 첫 레코드인 경우
            }

            int nextNumber;
            if (maxCode != null && maxCode.startsWith("share_")) {
                try {
                    String numberStr = maxCode.substring(6); // "share_" 이후 숫자
                    nextNumber = Integer.parseInt(numberStr) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1; // 파싱 실패 시 1부터 시작
                }
            } else {
                nextNumber = 1; // 첫 번째 코드
            }

            String newShareCode = "share_" + nextNumber;

            // 새 share_code를 DB에 생성 (초기값 0)
            String insertSql = "INSERT INTO share_referrals (share_code, total_visits, unique_visits) VALUES (?, 0, 0) ON DUPLICATE KEY UPDATE share_code = share_code";
            jdbcTemplate.update(insertSql, newShareCode);

            return newShareCode;
        } catch (Exception e) {
            // 오류 발생 시 기본값 반환
            System.err.println("Error generating next share code: " + e.getMessage());
            return "share_1";
        }
    }

    /**
     * 공유 통계 DTO
     */
    public static class ShareStats {
        private final String shareCode;
        private final int totalVisits;
        private final int uniqueVisits;

        public ShareStats(String shareCode, int totalVisits, int uniqueVisits) {
            this.shareCode = shareCode;
            this.totalVisits = totalVisits;
            this.uniqueVisits = uniqueVisits;
        }

        public String getShareCode() {
            return shareCode;
        }

        public int getTotalVisits() {
            return totalVisits;
        }

        public int getUniqueVisits() {
            return uniqueVisits;
        }
    }
}

