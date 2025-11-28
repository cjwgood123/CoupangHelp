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

