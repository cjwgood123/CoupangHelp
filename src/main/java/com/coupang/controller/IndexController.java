package com.coupang.controller;

import com.coupang.service.BoardService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Controller
public class IndexController {

    private final BoardService boardService;

    public IndexController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping(value = {"/", ""})
    public String index(Model model) {
        // 전체 상품 수 조회
        int totalProductCount = boardService.getTotalProductCount();
        // 천 단위 콤마 포맷팅
        String formattedCount = NumberFormat.getNumberInstance(Locale.US).format(totalProductCount);
        model.addAttribute("totalProductCount", totalProductCount);
        model.addAttribute("formattedProductCount", formattedCount);
        
        // 오늘 업데이트된 상품 개수 조회
        int todayPurchasedCount = boardService.getTodayPurchasedCount();
        int todaySatisfiedCount = boardService.getTodaySatisfiedCount();
        model.addAttribute("todayPurchasedCount", todayPurchasedCount);
        model.addAttribute("todaySatisfiedCount", todaySatisfiedCount);
        
        // 단기간 인기 상품 개수 조회
        try {
            int shortTermPopularCount = boardService.getShortTermPopularTotalCount();
            model.addAttribute("shortTermPopularCount", shortTermPopularCount);
        } catch (Exception e) {
            model.addAttribute("shortTermPopularCount", 0);
        }
        
        // 상위 카테고리 10개 조회
        try {
            var topCategories = boardService.getTopCategories();
            model.addAttribute("topCategories", topCategories);
        } catch (Exception e) {
            model.addAttribute("topCategories", new java.util.ArrayList<>());
        }
        
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/seller-guide")
    public String sellerGuide() {
        return "seller-guide";
    }

    @GetMapping("/product-finding-guide")
    public String productFindingGuide() {
        return "product-finding-guide";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/1688-sourcing-guide")
    public String sourcingGuide1688() {
        return "1688-sourcing-guide";
    }

    @GetMapping("/rocket-gross-guide")
    public String rocketGrossGuide() {
        return "rocket-gross-guide";
    }

    @GetMapping("/roi-system-guide")
    public String roiSystemGuide() {
        return "roi-system-guide";
    }

    @GetMapping("/rocket-gross-registration-guide")
    public String rocketGrossRegistrationGuide() {
        return "rocket-gross-registration-guide";
    }

    @GetMapping("/rocket-gross-warehouse-guide")
    public String rocketGrossWarehouseGuide() {
        return "rocket-gross-warehouse-guide";
    }

    @GetMapping("/guide/rocket-gross-warehouse-supplies")
    public String rocketGrossWarehouseSupplies() {
        return "guide/rocket-gross-warehouse-supplies";
    }

    @GetMapping("/guide/rocket-gross-warehouse-process")
    public String rocketGrossWarehouseProcess() {
        return "guide/rocket-gross-warehouse-process";
    }

    @GetMapping("/guide/rocket-gross-label-printer")
    public String rocketGrossLabelPrinter() {
        return "guide/rocket-gross-label-printer";
    }

    @GetMapping("/guide/rocket-gross-label-attachment")
    public String rocketGrossLabelAttachment() {
        return "guide/rocket-gross-label-attachment";
    }

    @GetMapping("/guide/rocket-gross-box-packing")
    public String rocketGrossBoxPacking() {
        return "guide/rocket-gross-box-packing";
    }

    @GetMapping("/guide/rocket-gross-pc-application")
    public String rocketGrossPcApplication() {
        return "guide/rocket-gross-pc-application";
    }

    @GetMapping("/guide/rocket-gross-warehouse-center")
    public String rocketGrossWarehouseCenter() {
        return "guide/rocket-gross-warehouse-center";
    }

    @GetMapping("/guide/rocket-gross-delivery-method")
    public String rocketGrossDeliveryMethod() {
        return "guide/rocket-gross-delivery-method";
    }

    @GetMapping("/guide/rocket-gross-return-prevention")
    public String rocketGrossReturnPrevention() {
        return "guide/rocket-gross-return-prevention";
    }

    // 쿠팡 판매 A to Z 가이드
    @GetMapping("/guide/seller-registration")
    public String sellerRegistration() {
        return "guide/seller-registration";
    }

    @GetMapping("/guide/coupang-wing-usage")
    public String coupangWingUsage() {
        return "guide/coupang-wing-usage";
    }

    @GetMapping("/guide/product-registration")
    public String productRegistration() {
        return "guide/product-registration";
    }

    @GetMapping("/guide/settlement-fees")
    public String settlementFees() {
        return "guide/settlement-fees";
    }

    @GetMapping("/guide/sales-strategy")
    public String salesStrategy() {
        return "guide/sales-strategy";
    }

    // 잘 팔리는 상품 찾는 법 가이드
    @GetMapping("/guide/popular-product-search")
    public String popularProductSearch() {
        return "guide/popular-product-search";
    }

    @GetMapping("/guide/business-insight")
    public String businessInsight() {
        return "guide/business-insight";
    }

    @GetMapping("/guide/competitor-analysis")
    public String competitorAnalysis() {
        return "guide/competitor-analysis";
    }

    @GetMapping("/guide/margin-calculation")
    public String marginCalculation() {
        return "guide/margin-calculation";
    }

    @GetMapping("/guide/category-trends")
    public String categoryTrends() {
        return "guide/category-trends";
    }

    // 1688 중국 공장 소싱 가이드
    @GetMapping("/guide/1688-registration")
    public String sourcing1688Registration() {
        return "guide/1688-registration";
    }

    @GetMapping("/guide/1688-factory-price")
    public String sourcing1688FactoryPrice() {
        return "guide/1688-factory-price";
    }

    @GetMapping("/guide/1688-cost-calculation")
    public String sourcing1688CostCalculation() {
        return "guide/1688-cost-calculation";
    }

    @GetMapping("/guide/1688-purchasing-agent")
    public String sourcing1688PurchasingAgent() {
        return "guide/1688-purchasing-agent";
    }

    @GetMapping("/guide/1688-customs-logistics")
    public String sourcing1688CustomsLogistics() {
        return "guide/1688-customs-logistics";
    }

    // 로켓그로스 초보 소싱 가이드
    @GetMapping("/guide/empty-land-finding")
    public String emptyLandFinding() {
        return "guide/empty-land-finding";
    }

    @GetMapping("/guide/green-belt-products")
    public String greenBeltProducts() {
        return "guide/green-belt-products";
    }

    @GetMapping("/guide/product-validation")
    public String productValidation() {
        return "guide/product-validation";
    }

    @GetMapping("/guide/test-order")
    public String testOrder() {
        return "guide/test-order";
    }

    @GetMapping("/guide/main-product-selection")
    public String mainProductSelection() {
        return "guide/main-product-selection";
    }

    // ROI 회전 시스템 가이드
    @GetMapping("/guide/roi-concept-calculation")
    public String roiConceptCalculation() {
        return "guide/roi-concept-calculation";
    }

    @GetMapping("/guide/capital-allocation")
    public String capitalAllocation() {
        return "guide/capital-allocation";
    }

    @GetMapping("/guide/inventory-rotation")
    public String inventoryRotation() {
        return "guide/inventory-rotation";
    }

    @GetMapping("/guide/season-prediction")
    public String seasonPrediction() {
        return "guide/season-prediction";
    }

    @GetMapping("/guide/automation-system")
    public String automationSystem() {
        return "guide/automation-system";
    }

    @GetMapping("/sitemap.xml")
    public ResponseEntity<String> sitemap() {
        try {
            Resource resource = new ClassPathResource("static/sitemap.xml");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/xml; charset=UTF-8"));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

