package com.coupang.controller;

import com.coupang.service.BoardService;
import com.coupang.service.ShareService;
import com.coupang.service.ShareService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class IndexController {

    private final BoardService boardService;
    private final ShareService shareService;

    public IndexController(BoardService boardService, ShareService shareService) {
        this.boardService = boardService;
        this.shareService = shareService;
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

    @GetMapping("/wing-express-guide")
    public String wingExpressGuide() {
        return "wing-express-guide";
    }

    @GetMapping("/item-winner-guide")
    public String itemWinnerGuide() {
        return "item-winner-guide";
    }

    @GetMapping("/beginner-seller-strategy")
    public String beginnerSellerStrategy() {
        return "beginner-seller-strategy";
    }

    @GetMapping("/product-registration-guide")
    public String productRegistrationGuide() {
        return "product-registration-guide";
    }

    @GetMapping("/margin-template-guide")
    public String marginTemplateGuide() {
        return "margin-template-guide";
    }

    @GetMapping("/product-detail-writing-guide")
    public String productDetailWritingGuide() {
        return "product-detail-writing-guide";
    }

    @GetMapping("/item-winner-safety-checklist")
    public String itemWinnerSafetyChecklist() {
        return "item-winner-safety-checklist";
    }

    @GetMapping("/blog-structure-guide")
    public String blogStructureGuide() {
        return "blog-structure-guide";
    }

    @GetMapping("/coupang-entry-guide")
    public String coupangEntryGuide() {
        return "coupang-entry-guide";
    }

    @GetMapping("/dropshipping-guide")
    public String dropshippingGuide() {
        return "dropshipping-guide";
    }

    @GetMapping("/margin-tracker")
    public String marginTracker() {
        return "margin-tracker";
    }

    @GetMapping("/margin-tracker/input")
    public String marginTrackerInput(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua != null && isMobileUserAgent(ua)) {
            String qs = request.getQueryString();
            return "redirect:/margin-tracker/input/mobile" + (qs != null && !qs.isEmpty() ? "?" + qs : "");
        }
        return "margin-tracker-input";
    }

    @GetMapping("/margin-tracker/input/mobile")
    public String marginTrackerInputMobile() {
        return "margin-tracker-input-mobile";
    }

    @GetMapping("/margin-tracker/product-register")
    public String marginTrackerProductRegister() {
        return "margin-tracker-product-register";
    }

    private static boolean isMobileUserAgent(String ua) {
        if (ua == null) return false;
        String lower = ua.toLowerCase();
        return lower.contains("android") || lower.contains("iphone") || lower.contains("ipod")
                || lower.contains("mobile") || lower.contains("webos") || lower.contains("blackberry");
    }

    @GetMapping("/event")
    public String event() {
        return "redirect:/";
    }

    @GetMapping("/event/generate-code")
    @ResponseBody
    public Map<String, String> generateShareCode(HttpSession session) {
        // 세션에 이미 share_code가 있으면 기존 것 반환
        String shareCode = (String) session.getAttribute("shareCode");
        if (shareCode == null) {
            shareCode = shareService.getNextShareCode();
            session.setAttribute("shareCode", shareCode);
        }
        Map<String, String> response = new HashMap<>();
        response.put("shareCode", shareCode);
        response.put("shareUrl", "https://helpcoupang.com/" + shareCode);
        return response;
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

