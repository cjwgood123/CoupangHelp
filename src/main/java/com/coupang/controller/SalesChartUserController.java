package com.coupang.controller;

import com.coupang.dto.SalesChartDataDto;
import com.coupang.dto.SalesChartUserDto;
import com.coupang.service.SalesChartDataService;
import com.coupang.service.SalesChartUserService;
import com.coupang.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales-chart")
public class SalesChartUserController {

    private final SalesChartUserService userService;
    private final SalesChartDataService dataService;

    @Autowired
    public SalesChartUserController(SalesChartUserService userService, SalesChartDataService dataService) {
        this.userService = userService;
        this.dataService = dataService;
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String userId = request.get("userId");
        String userPw = request.get("userPw");
        String userPwConfirm = request.get("userPwConfirm");
        String sellerType = request.get("sellerType");
        String sellerOther = request.get("sellerOther");

        // 유효성 검사
        if (userId == null || userId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "아이디를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userPw == null || userPw.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!userPw.equals(userPwConfirm)) {
            response.put("success", false);
            response.put("message", "비밀번호가 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userPw.length() < 4) {
            response.put("success", false);
            response.put("message", "비밀번호는 최소 4자 이상이어야 합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (sellerType == null || (!sellerType.equals("ROCKET_GROSS") && !sellerType.equals("OTHER"))) {
            response.put("success", false);
            response.put("message", "셀러 타입을 선택해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (sellerType.equals("OTHER") && (sellerOther == null || sellerOther.trim().isEmpty())) {
            response.put("success", false);
            response.put("message", "기타 셀러명을 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        // 아이디 중복 확인
        if (userService.existsUserId(userId)) {
            response.put("success", false);
            response.put("message", "이미 존재하는 아이디입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 회원가입 처리
        SalesChartUserDto user = new SalesChartUserDto();
        user.setUserId(userId.trim());
        user.setUserPw(userPw); // 실제로는 암호화해야 하지만, 간단한 구현을 위해 평문 저장
        user.setSellerType(sellerType);
        user.setSellerOther(sellerType.equals("OTHER") ? sellerOther.trim() : null);

        boolean success = userService.signup(user);
        
        if (success) {
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("userId", user.getUserId());
        } else {
            response.put("success", false);
            response.put("message", "회원가입에 실패했습니다. 다시 시도해주세요.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String userId = request.get("userId");
        String userPw = request.get("userPw");

        if (userId == null || userId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "아이디를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        userId = userId.trim();
        
        // SQL 인젝션 및 XSS 검사
        if (InputSanitizer.containsSqlInjection(userId) || InputSanitizer.containsXss(userId)) {
            response.put("success", false);
            response.put("message", "입력값에 허용되지 않은 문자가 포함되어 있습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userPw == null || userPw.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        userPw = userPw.trim();
        
        // SQL 인젝션 및 XSS 검사
        if (InputSanitizer.containsSqlInjection(userPw) || InputSanitizer.containsXss(userPw)) {
            response.put("success", false);
            response.put("message", "입력값에 허용되지 않은 문자가 포함되어 있습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<SalesChartUserDto> user = userService.login(userId, userPw);
        
        if (user.isPresent()) {
            response.put("success", true);
            response.put("message", "로그인되었습니다.");
            response.put("userId", user.get().getUserId());
            response.put("sellerType", user.get().getSellerType());
            response.put("sellerOther", user.get().getSellerOther());
        } else {
            response.put("success", false);
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 판매 데이터 저장
     */
    @PostMapping("/data/save")
    public ResponseEntity<Map<String, Object>> saveData(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        String userId = (String) request.get("userId");
        String extractDate = (String) request.get("extractDate");
        String productNumber = (String) request.get("productNumber");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) request.get("dataList");

        if (userId == null || userId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "사용자 정보가 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        if (extractDate == null || extractDate.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "데이터 추출일자를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (productNumber == null || productNumber.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "상품번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        if (dataList == null || dataList.isEmpty()) {
            response.put("success", false);
            response.put("message", "판매 데이터를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 입력값 검증 및 정제
            userId = userId.trim();
            
            // SQL 인젝션 및 XSS 검사
            if (InputSanitizer.containsSqlInjection(userId) || InputSanitizer.containsXss(userId)) {
                response.put("success", false);
                response.put("message", "입력값에 허용되지 않은 문자가 포함되어 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            extractDate = extractDate.trim();
            
            // 날짜 형식 검증
            if (!InputSanitizer.isValidExtractDate(extractDate)) {
                response.put("success", false);
                response.put("message", "날짜 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            productNumber = productNumber.trim();
            
            // 상품번호 검증
            if (!InputSanitizer.isValidProductNumber(productNumber)) {
                response.put("success", false);
                response.put("message", "상품번호는 숫자만 입력 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // extractDate에서 요일 추출 및 날짜 정리
            String cleanExtractDate = extractDate;
            String dayOfWeek = extractDayOfWeek(extractDate);
            
            // 요일 부분 제거 (예: "1월26일 (월)" -> "1월26일")
            if (cleanExtractDate.contains("(")) {
                cleanExtractDate = cleanExtractDate.substring(0, cleanExtractDate.indexOf("(")).trim();
            }
            
            // 요일 검증
            if (dayOfWeek != null && !InputSanitizer.isValidDayOfWeek(dayOfWeek)) {
                response.put("success", false);
                response.put("message", "요일 형식이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<SalesChartDataDto> salesDataList = new ArrayList<>();
            for (Map<String, Object> data : dataList) {
                String time = (String) data.get("time");
                String salesStr = data.get("sales").toString();
                String revenueStr = data.get("revenue").toString();
                
                // 시간 검증
                if (time == null || !InputSanitizer.isValidTime(time)) {
                    response.put("success", false);
                    response.put("message", "시간 형식이 올바르지 않습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // 숫자 검증
                if (!InputSanitizer.isValidNumber(salesStr) || !InputSanitizer.isValidNumber(revenueStr)) {
                    response.put("success", false);
                    response.put("message", "판매량 또는 매출 값이 올바르지 않습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                
                SalesChartDataDto salesData = new SalesChartDataDto();
                salesData.setUserId(userId);
                salesData.setExtractDate(cleanExtractDate);
                salesData.setDayOfWeek(dayOfWeek);
                salesData.setProductNumber(productNumber);
                salesData.setTime(time);
                salesData.setSalesEstimate(new BigDecimal(salesStr));
                salesData.setRevenueEstimate(Integer.parseInt(revenueStr));
                salesDataList.add(salesData);
            }

            dataService.saveSalesData(salesDataList);
            
            response.put("success", true);
            response.put("message", "데이터가 저장되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "데이터 저장 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 판매 데이터 조회
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getData(
            @RequestParam String userId,
            @RequestParam(required = false) String extractDate,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String productNumber,
            @RequestParam(required = false) String dayFilter) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 입력값 검증
            if (userId == null || userId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자 ID가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            userId = userId.trim();
            
            // SQL 인젝션 및 XSS 검사
            if (InputSanitizer.containsSqlInjection(userId) || InputSanitizer.containsXss(userId)) {
                response.put("success", false);
                response.put("message", "입력값에 허용되지 않은 문자가 포함되어 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 날짜 파라미터 검증
            if (extractDate != null && !extractDate.trim().isEmpty()) {
                extractDate = extractDate.trim();
                if (InputSanitizer.containsSqlInjection(extractDate) || InputSanitizer.containsXss(extractDate)) {
                    response.put("success", false);
                    response.put("message", "날짜에 허용되지 않은 문자가 포함되어 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                startDate = startDate.trim();
                if (InputSanitizer.containsSqlInjection(startDate) || InputSanitizer.containsXss(startDate)) {
                    response.put("success", false);
                    response.put("message", "시작일에 허용되지 않은 문자가 포함되어 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                endDate = endDate.trim();
                if (InputSanitizer.containsSqlInjection(endDate) || InputSanitizer.containsXss(endDate)) {
                    response.put("success", false);
                    response.put("message", "종료일에 허용되지 않은 문자가 포함되어 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 상품번호 검증
            if (productNumber != null && !productNumber.trim().isEmpty() && !productNumber.equals("전체")) {
                productNumber = productNumber.trim();
                if (InputSanitizer.containsSqlInjection(productNumber) || InputSanitizer.containsXss(productNumber)) {
                    response.put("success", false);
                    response.put("message", "상품번호에 허용되지 않은 문자가 포함되어 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                if (!InputSanitizer.isValidProductNumber(productNumber)) {
                    response.put("success", false);
                    response.put("message", "상품번호 형식이 올바르지 않습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // 요일 필터 검증
            if (dayFilter != null && !dayFilter.trim().isEmpty()) {
                dayFilter = dayFilter.trim();
                if (InputSanitizer.containsSqlInjection(dayFilter) || InputSanitizer.containsXss(dayFilter)) {
                    response.put("success", false);
                    response.put("message", "요일 필터에 허용되지 않은 문자가 포함되어 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }
                // 쉼표로 구분된 요일들 검증
                String[] days = dayFilter.split(",");
                for (String day : days) {
                    day = day.trim();
                    if (!day.isEmpty() && !InputSanitizer.isValidDayOfWeek(day)) {
                        response.put("success", false);
                        response.put("message", "요일 형식이 올바르지 않습니다.");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }
            
            List<SalesChartDataDto> dataList;
            
            // 날짜 형식 변환 (요일 부분 제거)
            String convertedExtractDate = cleanExtractDate(extractDate);
            String convertedStartDate = cleanExtractDate(startDate);
            String convertedEndDate = cleanExtractDate(endDate);
            
            // 날짜와 상품번호를 동시에 필터링할 수 있도록 수정
            if (convertedExtractDate != null && !convertedExtractDate.trim().isEmpty()) {
                // 특정 날짜 조회
                if (productNumber != null && !productNumber.trim().isEmpty()) {
                    // 날짜 + 상품번호
                    dataList = dataService.getDataByUserDateAndProduct(userId, convertedExtractDate, productNumber);
                } else {
                    // 날짜만
                    dataList = dataService.getDataByUserAndDate(userId, convertedExtractDate);
                }
            } else if (convertedStartDate != null && convertedEndDate != null 
                    && !convertedStartDate.trim().isEmpty() && !convertedEndDate.trim().isEmpty()) {
                // 기간별 조회
                if (productNumber != null && !productNumber.trim().isEmpty() && !productNumber.equals("전체")) {
                    // 기간 + 상품번호 + 요일 필터
                    if (dayFilter != null && !dayFilter.trim().isEmpty()) {
                        dataList = dataService.getDataByUserDateRangeAndProductWithDayFilter(userId, convertedStartDate, convertedEndDate, productNumber, dayFilter);
                    } else {
                        dataList = dataService.getDataByUserDateRangeAndProduct(userId, convertedStartDate, convertedEndDate, productNumber);
                    }
                } else {
                    // 기간 + 요일 필터
                    if (dayFilter != null && !dayFilter.trim().isEmpty()) {
                        dataList = dataService.getDataByUserAndDateRangeWithDayFilter(userId, convertedStartDate, convertedEndDate, dayFilter);
                    } else {
                        dataList = dataService.getDataByUserAndDateRange(userId, convertedStartDate, convertedEndDate);
                    }
                }
            } else if (productNumber != null && !productNumber.trim().isEmpty() && !productNumber.equals("전체")) {
                // 상품번호만
                dataList = dataService.getDataByUserAndProduct(userId, productNumber);
            } else {
                // 전체 조회
                dataList = dataService.getAllDataByUser(userId);
            }
            
            response.put("success", true);
            response.put("data", dataList);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "데이터 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 추출일자 목록 조회
     */
    @GetMapping("/data/dates")
    public ResponseEntity<Map<String, Object>> getExtractDates(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> dates = dataService.getDistinctExtractDates(userId);
            response.put("success", true);
            response.put("dates", dates);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "데이터 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 상품번호 목록 조회
     */
    @GetMapping("/data/products")
    public ResponseEntity<Map<String, Object>> getProductNumbers(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> products = dataService.getDistinctProductNumbers(userId);
            response.put("success", true);
            response.put("products", products);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "데이터 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 날짜에서 요일 추출
     * "1월26일 (월)" -> "월"
     */
    private String extractDayOfWeek(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = date.trim();
        
        // "(월)" 형식에서 요일 추출
        if (trimmed.contains("(") && trimmed.contains(")")) {
            int start = trimmed.indexOf("(") + 1;
            int end = trimmed.indexOf(")");
            if (start < end) {
                return trimmed.substring(start, end).trim();
            }
        }
        
        return null;
    }
    
    /**
     * 날짜에서 요일 부분 제거
     * "1월26일 (월)" -> "1월26일"
     * "2026년 01월 26일" -> "1월26일"
     */
    private String cleanExtractDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return date;
        }
        
        String trimmed = date.trim();
        
        // "(요일)" 부분 제거
        if (trimmed.contains("(")) {
            trimmed = trimmed.substring(0, trimmed.indexOf("(")).trim();
        }
        
        // "2026년 01월 26일" 형식인 경우 "1월26일"로 변환
        if (trimmed.contains("년") && trimmed.contains("월") && trimmed.contains("일")) {
            try {
                // "2026년 01월 26일" -> "1월26일"
                String monthPart = trimmed.substring(trimmed.indexOf("년") + 1, trimmed.indexOf("월")).trim();
                String dayPart = trimmed.substring(trimmed.indexOf("월") + 1, trimmed.indexOf("일")).trim();
                
                // 앞의 0 제거
                int month = Integer.parseInt(monthPart);
                int day = Integer.parseInt(dayPart);
                
                return month + "월" + day + "일";
            } catch (Exception e) {
                // 변환 실패 시 원본 반환
                e.printStackTrace();
            }
        }
        
        // 이미 "1월26일" 형식이면 그대로 반환
        if (trimmed.contains("월") && trimmed.contains("일")) {
            return trimmed;
        }
        
        return trimmed;
    }
}

