package com.coupang.controller;

import com.coupang.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}

