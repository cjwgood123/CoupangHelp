package com.coupang.controller;

import com.coupang.dto.MarginTrackerDataDto;
import com.coupang.service.MarginTrackerDataService;
import com.coupang.service.MarginTrackerProductService;
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
    private final MarginTrackerProductService marginTrackerProductService;
    private final SalesChartUserService userService;

    public MarginTrackerController(MarginTrackerDataService marginTrackerDataService,
                                    MarginTrackerProductService marginTrackerProductService,
                                    SalesChartUserService userService) {
        this.marginTrackerDataService = marginTrackerDataService;
        this.marginTrackerProductService = marginTrackerProductService;
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
        dto.setOptionId(getString(body, "optionId"));
        dto.setOptionAlias(getString(body, "optionAlias"));
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
        
        // 마진율 계산 (없거나 0이면 계산)
        java.math.BigDecimal marginRate = toBigDecimal(body, "marginRate");
        if (marginRate == null || marginRate.compareTo(java.math.BigDecimal.ZERO) == 0) {
            java.math.BigDecimal finalPrice = dto.getFinalSellingPrice();
            java.math.BigDecimal marginPerUnit = dto.getMarginPerUnit();
            java.math.BigDecimal totalMargin = dto.getTotalMargin();
            java.math.BigDecimal actualRevenue = dto.getActualSalesRevenue();
            
            // 개당 마진과 최종 판매가로 계산
            if (finalPrice != null && marginPerUnit != null && 
                finalPrice.compareTo(java.math.BigDecimal.ZERO) > 0) {
                marginRate = marginPerUnit.divide(finalPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new java.math.BigDecimal("100"));
            }
            // 또는 총 마진과 실제 매출로 계산
            else if (actualRevenue != null && totalMargin != null && 
                     actualRevenue.compareTo(java.math.BigDecimal.ZERO) > 0) {
                marginRate = totalMargin.divide(actualRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new java.math.BigDecimal("100"));
            }
        }
        dto.setMarginRate(marginRate);
        
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
     * 단건 조회 (수정용) - 같은 날짜/상품의 모든 옵션 데이터도 함께 반환
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
                        // 같은 날짜, 같은 상품의 모든 옵션 데이터 조회
                        if (dto.getProductNumber() != null && dto.getSaleDate() != null) {
                            List<MarginTrackerDataDto> sameDateRecords = marginTrackerDataService.findByUserIdProductAndDate(
                                userId.trim(), dto.getProductNumber(), dto.getSaleDate()
                            );
                            List<Map<String, Object>> allRecords = sameDateRecords.stream()
                                .map(MarginTrackerController::toRecordMap)
                                .collect(Collectors.toList());
                            res.put("allRecordsForDate", allRecords);
                        }
                        return ResponseEntity.ok(res);
                    })
                    .orElse(ResponseEntity.ok(Map.of("success", false)));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 기존 상품 목록 (상품번호/상품명 선택용) - 기존 입력 데이터 + 등록 상품 합쳐서 반환 (등록 상품 우선)
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
            List<Map<String, Object>> registered = marginTrackerProductService.findProductsByUserId(userId.trim());
            List<Map<String, String>> fromData = marginTrackerDataService.findDistinctProductsByUserId(userId.trim());
            List<Map<String, String>> products = new java.util.ArrayList<>();
            if (registered != null && !registered.isEmpty()) {
                for (Map<String, Object> r : registered) {
                    Map<String, String> p = new HashMap<>();
                    p.put("productNumber", (String) r.get("productNumber"));
                    p.put("productName", (String) r.get("productName"));
                    p.put("fromRegistered", "true");
                    products.add(p);
                }
            }
            for (Map<String, String> p : fromData) {
                String num = p.get("productNumber");
                if (products.stream().noneMatch(m -> num != null && num.equals(m.get("productNumber")))) {
                    products.add(new HashMap<>(p));
                }
            }
            res.put("success", true);
            res.put("products", products);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("products", List.of());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 등록 상품만 조회 (판매 입력 페이지 셀렉트/달력 연동 자동 매칭용)
     */
    @GetMapping("/registered-products")
    public ResponseEntity<Map<String, Object>> registeredProducts(@RequestParam String userId) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("products", List.of());
            return ResponseEntity.badRequest().body(res);
        }
        try {
            List<Map<String, Object>> products = marginTrackerProductService.findProductsByUserId(userId.trim());
            res.put("success", true);
            res.put("products", products != null ? products : List.of());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("products", List.of());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 해당 상품의 가장 최근 입력 데이터 1건 조회 (동일 상품 입력 시 판매가 변동 등 이전 값 자동 설정용)
     */
    @GetMapping("/latest-record")
    public ResponseEntity<Map<String, Object>> getLatestRecord(
            @RequestParam String userId,
            @RequestParam String productNumber) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty() || productNumber == null || productNumber.trim().isEmpty()) {
            res.put("success", false);
            return ResponseEntity.badRequest().body(res);
        }
        try {
            var dtoOpt = marginTrackerDataService.findLatestByUserIdAndProductNumber(userId.trim(), productNumber.trim());
            if (dtoOpt.isEmpty()) {
                res.put("success", true);
                res.put("priceFluctuation", null);
                return ResponseEntity.ok(res);
            }
            MarginTrackerDataDto dto = dtoOpt.get();
            res.put("success", true);
            res.put("priceFluctuation", dto.getPriceFluctuation());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 상품 옵션 조회 (productNumber로)
     */
    @GetMapping("/product-options")
    public ResponseEntity<Map<String, Object>> getProductOptions(@RequestParam String userId, @RequestParam String productNumber) {
        Map<String, Object> res = new HashMap<>();
        if (userId == null || userId.trim().isEmpty() || productNumber == null || productNumber.trim().isEmpty()) {
            res.put("success", false);
            res.put("options", List.of());
            return ResponseEntity.badRequest().body(res);
        }
        try {
            List<Map<String, Object>> products = marginTrackerProductService.findProductsByUserId(userId.trim());
            Long productSeq = null;
            for (Map<String, Object> p : products) {
                if (productNumber.equals(p.get("productNumber"))) {
                    productSeq = ((Number) p.get("productSeq")).longValue();
                    break;
                }
            }
            if (productSeq == null) {
                res.put("success", false);
                res.put("options", List.of());
                res.put("message", "등록된 상품을 찾을 수 없습니다.");
                return ResponseEntity.ok(res);
            }
            List<Map<String, Object>> options = marginTrackerProductService.findOptionsByProductSeq(productSeq);
            res.put("success", true);
            res.put("options", options != null ? options : List.of());
            res.put("productSeq", productSeq);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("options", List.of());
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 상품 등록 및 관리 - 저장 (상품 + 옵션)
     */
    @PostMapping("/product")
    public ResponseEntity<Map<String, Object>> saveProduct(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        String userId = getString(body, "userId");
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(res);
        }
        userId = userId.trim();
        if (!userService.findByUserId(userId).isPresent()) {
            res.put("success", false);
            res.put("message", "존재하지 않는 회원입니다.");
            return ResponseEntity.badRequest().body(res);
        }
        String productNumber = getString(body, "productNumber");
        String productName = getString(body, "productName");
        if (productNumber == null || productName == null || productName.isEmpty()) {
            res.put("success", false);
            res.put("message", "상품 ID와 상품 별칭을 입력해주세요.");
            return ResponseEntity.badRequest().body(res);
        }
        String shippingCostRange = getString(body, "shippingCostRange");
        String commissionRate = getString(body, "commissionRate");
        Long productSeq = body.get("productSeq") != null ? Long.valueOf(body.get("productSeq").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> options = (List<Map<String, Object>>) body.get("options");
        if (options == null) options = List.of();
        try {
            long seq = marginTrackerProductService.saveProduct(userId, productNumber, productName, shippingCostRange, commissionRate, options, productSeq);
            res.put("success", true);
            res.put("message", productSeq != null ? "수정되었습니다." : "저장되었습니다.");
            res.put("productSeq", seq);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(res);
        }
    }

    /**
     * 상품 노출 순서 저장 (상품 노출 순서 변경 탭에서 호출)
     */
    @PostMapping("/product-order")
    public ResponseEntity<Map<String, Object>> saveProductOrder(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        String userId = getString(body, "userId");
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(res);
        }
        userId = userId.trim();
        if (!userService.findByUserId(userId).isPresent()) {
            res.put("success", false);
            res.put("message", "존재하지 않는 회원입니다.");
            return ResponseEntity.badRequest().body(res);
        }
        @SuppressWarnings("unchecked")
        List<Number> list = (List<Number>) body.get("productSeqs");
        if (list == null || list.isEmpty()) {
            res.put("success", true);
            res.put("message", "변경할 순서가 없습니다.");
            return ResponseEntity.ok(res);
        }
        List<Long> productSeqs = list.stream().map(Number::longValue).collect(Collectors.toList());
        try {
            marginTrackerProductService.updateDisplayOrder(userId, productSeqs);
            res.put("success", true);
            res.put("message", "순서가 저장되었습니다.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "저장 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(res);
        }
    }

    /** 상품 삭제: 해당 상품의 매출 데이터(margin_tracker_data) 전부 삭제 후 상품(margin_tracker_product) 삭제. 옵션은 FK CASCADE로 자동 삭제. */
    @DeleteMapping("/product")
    public ResponseEntity<Map<String, Object>> deleteProduct(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        String userId = getString(body, "userId");
        if (userId == null || userId.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "로그인이 필요합니다.");
            return ResponseEntity.badRequest().body(res);
        }
        userId = userId.trim();
        String productNumber = getString(body, "productNumber");
        if (productNumber == null || productNumber.isEmpty()) {
            res.put("success", false);
            res.put("message", "삭제할 상품을 선택하세요.");
            return ResponseEntity.badRequest().body(res);
        }
        if (!userService.findByUserId(userId).isPresent()) {
            res.put("success", false);
            res.put("message", "존재하지 않는 회원입니다.");
            return ResponseEntity.badRequest().body(res);
        }
        try {
            marginTrackerDataService.deleteByUserIdAndProductNumber(userId, productNumber);
            boolean deleted = marginTrackerProductService.deleteByUserIdAndProductNumber(userId, productNumber);
            res.put("success", deleted);
            res.put("message", deleted ? "상품과 관련 매출 데이터가 모두 삭제되었습니다." : "삭제할 상품을 찾을 수 없습니다.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("success", false);
            res.put("message", "삭제 중 오류가 발생했습니다.");
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
        m.put("optionId", dto.getOptionId());
        m.put("optionAlias", dto.getOptionAlias());
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
