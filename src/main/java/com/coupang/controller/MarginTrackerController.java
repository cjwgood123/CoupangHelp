package com.coupang.controller;

import com.coupang.dto.MarginTrackerDataDto;
import com.coupang.service.MarginTrackerDataService;
import com.coupang.service.SalesChartUserService;
import com.coupang.util.InputSanitizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/margin-tracker")
public class MarginTrackerController {

    private final MarginTrackerDataService marginTrackerDataService;
    private final SalesChartUserService userService;

    public MarginTrackerController(MarginTrackerDataService marginTrackerDataService,
                                    SalesChartUserService userService) {
        this.marginTrackerDataService = marginTrackerDataService;
        this.userService = userService;
    }

    /**
     * 마진 트래커 데이터 저장 (로그인 회원 = sales_chart_user 동일)
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> save(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) body.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(res);
        }
        userId = userId.trim();
        if (InputSanitizer.containsSqlInjection(userId) || InputSanitizer.containsXss(userId)) {
            res.put("success", false);
            res.put("message", "입력값에 허용되지 않은 문자가 포함되어 있습니다.");
            return ResponseEntity.badRequest().body(res);
        }
        if (!userService.findByUserId(userId).isPresent()) {
            res.put("success", false);
            res.put("message", "존재하지 않는 회원입니다.");
            return ResponseEntity.badRequest().body(res);
        }

        MarginTrackerDataDto dto = new MarginTrackerDataDto();
        dto.setUserId(userId);
        dto.setProductNumber(getString(body, "productNumber"));
        dto.setProductName(getString(body, "productName"));
        dto.setSaleDate(getString(body, "date"));
        dto.setSellingPrice(toBigDecimal(body, "sellingPrice"));
        dto.setDiscountCoupon(toBigDecimal(body, "discountCoupon"));
        dto.setFinalSellingPrice(toBigDecimal(body, "finalSellingPrice"));
        dto.setPriceFluctuation(toBigDecimal(body, "priceFluctuation"));
        dto.setSalesQuantity(toBigDecimal(body, "salesQuantity"));
        dto.setActualSalesRevenue(toBigDecimal(body, "actualSalesRevenue"));
        dto.setMarginPerUnit(toBigDecimal(body, "marginPerUnit"));
        dto.setTotalMargin(toBigDecimal(body, "totalMargin"));
        dto.setAdvertisingCost(toBigDecimal(body, "advertisingCost"));
        dto.setAdvertisingCostAdjusted(toBigDecimal(body, "advertisingCostAdjusted"));
        dto.setNetProfit(toBigDecimal(body, "netProfit"));
        dto.setMarginRate(toBigDecimal(body, "marginRate"));
        dto.setAdSales(toBigDecimal(body, "adSales"));
        dto.setOrganicSales(toBigDecimal(body, "organicSales"));
        dto.setOrganicSalesRatio(toBigDecimal(body, "organicSalesRatio"));
        dto.setRoas(toBigDecimal(body, "roas"));

        try {
            Object idObj = body.get("dataSeq");
            if (idObj != null && !idObj.toString().trim().isEmpty()) {
                long dataSeq = Long.parseLong(idObj.toString().trim());
                dto.setDataSeq(dataSeq);
                marginTrackerDataService.update(dto);
                res.put("success", true);
                res.put("message", "수정되었습니다.");
            } else {
                marginTrackerDataService.save(dto);
                res.put("success", true);
                res.put("message", "저장되었습니다.");
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 회원별 마진 트래커 기록 목록 (대시보드용, 페이징)
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> records(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9999") int size) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("records", List.of());
            return ResponseEntity.badRequest().body(res);
        }
        userId = userId.trim();
        try {
            int total = marginTrackerDataService.countByUserId(userId);
            List<MarginTrackerDataDto> list = marginTrackerDataService.findByUserId(userId, page, size);
            List<Map<String, Object>> records = list.stream().map(dto -> toRecordMap(dto)).collect(Collectors.toList());
            res.put("success", true);
            res.put("records", records);
            res.put("total", total);
            res.put("page", page);
            res.put("size", size);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("records", List.of());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 단건 조회 (수정용)
     */
    @GetMapping("/record/{id}")
    public ResponseEntity<Map<String, Object>> getRecord(@PathVariable Long id, @RequestParam String userId) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            return ResponseEntity.badRequest().body(res);
        }
        try {
            return marginTrackerDataService.findByIdAndUserId(id, userId.trim())
                    .map(dto -> {
                        res.put("success", true);
                        res.put("record", toRecordMap(dto));
                        return ResponseEntity.ok(res);
                    })
                    .orElse(ResponseEntity.ok(Map.of("success", false)));
        } catch (Exception e) {
            res.put("success", false);
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 기존 상품 목록 (상품번호/상품명 선택용)
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> products(@RequestParam String userId) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("products", List.of());
            return ResponseEntity.badRequest().body(res);
        }
        try {
            List<Map<String, String>> products = marginTrackerDataService.findDistinctProductsByUserId(userId.trim());
            res.put("success", true);
            res.put("products", products);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("products", List.of());
            return ResponseEntity.status(500).body(res);
        }
    }

    @DeleteMapping("/record/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id, @RequestParam String userId) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(res);
        }
        try {
            boolean deleted = marginTrackerDataService.deleteByIdAndUserId(id, userId.trim());
            res.put("success", deleted);
            res.put("message", deleted ? "삭제되었습니다." : "삭제할 수 없습니다.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(res);
        }
    }

    private static Map<String, Object> toRecordMap(MarginTrackerDataDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", dto.getDataSeq());
        m.put("productNumber", dto.getProductNumber());
        m.put("productName", dto.getProductName());
        m.put("date", dto.getSaleDate());
        m.put("timestamp", dto.getRegDate() != null ? dto.getRegDate().toString() : null);
        m.put("sellingPrice", dto.getSellingPrice());
        m.put("discountCoupon", dto.getDiscountCoupon());
        m.put("finalSellingPrice", dto.getFinalSellingPrice());
        m.put("priceFluctuation", dto.getPriceFluctuation());
        m.put("salesQuantity", dto.getSalesQuantity());
        m.put("actualSalesRevenue", dto.getActualSalesRevenue());
        m.put("marginPerUnit", dto.getMarginPerUnit());
        m.put("totalMargin", dto.getTotalMargin());
        m.put("advertisingCost", dto.getAdvertisingCost());
        m.put("netProfit", dto.getNetProfit());
        m.put("marginRate", dto.getMarginRate());
        m.put("adSales", dto.getAdSales());
        m.put("organicSales", dto.getOrganicSales());
        m.put("organicSalesRatio", dto.getOrganicSalesRatio());
        m.put("roas", dto.getRoas());
        return m;
    }

    private static String getString(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString().trim() : null;
    }

    private static BigDecimal toBigDecimal(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null || (v instanceof String && ((String) v).isEmpty())) return null;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try {
            return new BigDecimal(v.toString().replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
}
