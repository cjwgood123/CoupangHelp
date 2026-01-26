package com.coupang.util;

import java.util.regex.Pattern;

/**
 * SQL 인젝션 및 XSS 공격 방어를 위한 입력값 검증 및 정제 유틸리티
 */
public class InputSanitizer {
    
    // SQL 인젝션 패턴
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE|UNION|SCRIPT|SCRIPT>|--|/\\*|\\*/|;|'|\"|`|\\*|%|_|\\||&|\\^|~|\\[|\\]|\\{|\\}|<|>)\\b)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    
    // XSS 패턴
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|</script>|<iframe|</iframe>|<object|</object>|<embed|</embed>|javascript:|onerror=|onclick=|onload=|onmouseover=|onfocus=|onblur=)",
        Pattern.CASE_INSENSITIVE
    );
    
    // 특수 문자 패턴
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
        "[<>\"'`;\\\\]"
    );
    
    /**
     * SQL 인젝션 패턴 검사
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }
    
    /**
     * XSS 패턴 검사
     */
    public static boolean containsXss(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }
    
    /**
     * 입력값 정제 (SQL 인젝션 및 XSS 제거)
     */
    public static String sanitize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        
        String sanitized = input.trim();
        
        // SQL 인젝션 패턴 제거
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        
        // XSS 패턴 제거
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");
        
        // 특수 문자 이스케이프
        sanitized = sanitized.replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#x27;")
                            .replace("`", "&#x60;")
                            .replace(";", "&#x3B;")
                            .replace("\\", "&#x5C;");
        
        return sanitized.trim();
    }
    
    /**
     * 사용자 ID 검증 (알파벳, 숫자, 언더스코어만 허용, 길이 제한 없음)
     */
    public static boolean isValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        // 알파벳, 숫자, 언더스코어만 허용 (길이 제한 없음)
        return userId.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * 상품번호 검증 (숫자만 허용)
     */
    public static boolean isValidProductNumber(String productNumber) {
        if (productNumber == null || productNumber.trim().isEmpty()) {
            return false;
        }
        // 숫자만 허용, 최대 20자
        return productNumber.matches("^[0-9]{1,20}$");
    }
    
    /**
     * 날짜 형식 검증 (예: "1월26일" 또는 "1월26일 (월)")
     */
    public static boolean isValidExtractDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        // "1월26일" 또는 "1월26일 (월)" 형식만 허용
        return date.matches("^\\d{1,2}월\\d{1,2}일(\\s*\\([월화수목금토일]\\))?$");
    }
    
    /**
     * 시간 형식 검증 (예: "00h", "02h", ... "22h")
     */
    public static boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }
        // "00h", "02h", ... "22h" 형식만 허용
        return time.matches("^(0[02468]|1[02468]|2[02])h$");
    }
    
    /**
     * 요일 검증 (월, 화, 수, 목, 금, 토, 일)
     */
    public static boolean isValidDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek.trim().isEmpty()) {
            return false;
        }
        return dayOfWeek.matches("^[월화수목금토일]$");
    }
    
    /**
     * 셀러 타입 검증
     */
    public static boolean isValidSellerType(String sellerType) {
        if (sellerType == null || sellerType.trim().isEmpty()) {
            return false;
        }
        return sellerType.equals("ROCKET_GROSS") || sellerType.equals("OTHER");
    }
    
    /**
     * 셀러 기타명 검증 (최대 100자, 특수 문자 제한)
     */
    public static boolean isValidSellerOther(String sellerOther) {
        if (sellerOther == null) {
            return true; // null 허용
        }
        if (sellerOther.trim().isEmpty()) {
            return true; // 빈 문자열 허용
        }
        // 최대 100자, 특수 문자 제한
        if (sellerOther.length() > 100) {
            return false;
        }
        return !containsSqlInjection(sellerOther) && !containsXss(sellerOther);
    }
    
    /**
     * 숫자 검증 (판매량, 매출)
     */
    public static boolean isValidNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 입력값 길이 검증
     */
    public static boolean isValidLength(String input, int maxLength) {
        if (input == null) {
            return true;
        }
        return input.length() <= maxLength;
    }
}

