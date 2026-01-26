package com.coupang.service;

import com.coupang.dto.SalesChartUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SalesChartUserService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SalesChartUserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SalesChartUserDto> userRowMapper = (rs, rowNum) -> {
        SalesChartUserDto user = new SalesChartUserDto();
        user.setUserSeq(rs.getLong("user_seq"));
        user.setUserId(rs.getString("user_id"));
        user.setUserPw(rs.getString("user_pw"));
        user.setSellerType(rs.getString("seller_type"));
        user.setSellerOther(rs.getString("seller_other"));
        if (rs.getTimestamp("reg_date") != null) {
            user.setRegDate(rs.getTimestamp("reg_date").toLocalDateTime());
        }
        return user;
    };

    /**
     * 회원가입
     */
    public boolean signup(SalesChartUserDto user) {
        String sql = """
            INSERT INTO sales_chart_user (user_id, user_pw, seller_type, seller_other, reg_date)
            VALUES (?, ?, ?, ?, NOW())
            """;
        
        try {
            jdbcTemplate.update(sql,
                user.getUserId(),
                user.getUserPw(),
                user.getSellerType(),
                user.getSellerOther()
            );
            return true;
        } catch (DataIntegrityViolationException e) {
            // 중복 아이디 등 제약조건 위반
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 로그인 - 아이디와 비밀번호로 사용자 조회
     */
    public Optional<SalesChartUserDto> login(String userId, String userPw) {
        String sql = """
            SELECT user_seq, user_id, user_pw, seller_type, seller_other, reg_date
            FROM sales_chart_user
            WHERE user_id = ? AND user_pw = ?
            """;
        
        try {
            SalesChartUserDto user = jdbcTemplate.queryForObject(sql, userRowMapper, userId, userPw);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 아이디 중복 확인
     */
    public boolean existsUserId(String userId) {
        String sql = """
            SELECT COUNT(*) FROM sales_chart_user WHERE user_id = ?
            """;
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    /**
     * 사용자 정보 조회 (아이디로)
     */
    public Optional<SalesChartUserDto> findByUserId(String userId) {
        String sql = """
            SELECT user_seq, user_id, user_pw, seller_type, seller_other, reg_date
            FROM sales_chart_user
            WHERE user_id = ?
            """;
        
        try {
            SalesChartUserDto user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

