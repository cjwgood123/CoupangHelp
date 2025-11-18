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
        return "index";
    }
}

