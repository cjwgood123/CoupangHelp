package com.coupang.controller;

import com.coupang.dto.ProductListDto;
import com.coupang.service.BoardService;
import com.coupang.util.MobileDetector;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/2025-11/all/{count}")
    public String board(@PathVariable String count,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String search,
                        HttpServletRequest request,
                        Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            String redirectUrl = "/mobile/2025-11/all/" + count + "?page=" + page;
            if (search != null && !search.trim().isEmpty()) {
                redirectUrl += "&search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
            }
            return "redirect:" + redirectUrl;
        }
        
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getProducts("2025-11", countInt, offset, size, search);
        int totalCount = boardService.getTotalCount("2025-11", countInt, search);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("count", countInt);
        model.addAttribute("month", "2025-11");
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("search", search);

        return "board";
    }

    @GetMapping("/mobile/2025-11/all/{count}")
    public String boardMobile(@PathVariable String count,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size,
                              Model model) {
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getProducts("2025-11", countInt, offset, size, null);
        int totalCount = boardService.getTotalCount("2025-11", countInt, null);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("count", countInt);
        model.addAttribute("month", "2025-11");
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-mobile";
    }

    @GetMapping("/2025-01/all/{count}")
    public String boardJanuary(@PathVariable String count,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               HttpServletRequest request,
                               Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/2025-01/all/" + count + "?page=" + page;
        }
        
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getProducts("2025-01", countInt, offset, size, null);
        int totalCount = boardService.getTotalCount("2025-01", countInt, null);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("count", countInt);
        model.addAttribute("month", "2025-01");
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board";
    }

    @GetMapping("/mobile/2025-01/all/{count}")
    public String boardJanuaryMobile(@PathVariable String count,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     Model model) {
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getProducts("2025-01", countInt, offset, size, null);
        int totalCount = boardService.getTotalCount("2025-01", countInt, null);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("count", countInt);
        model.addAttribute("month", "2025-01");
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-mobile";
    }

    @GetMapping("/star/all")
    public String starAll(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size,
                          HttpServletRequest request,
                          Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/star/all?page=" + page;
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회 (전체)
        List<ProductListDto> products = boardService.getStarProducts(null, offset, size);
        int totalCount = boardService.getTotalStarCount(null);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 사용 가능한 star 값 목록 조회
        List<Integer> availableStars = boardService.getAvailableStarValues();

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", null);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star";
    }

    @GetMapping("/star/{star}")
    public String starFilter(@PathVariable Integer star,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size,
                             HttpServletRequest request,
                             Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/star/" + star + "?page=" + page;
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회 (star 필터링)
        List<ProductListDto> products = boardService.getStarProducts(star, offset, size);
        int totalCount = boardService.getTotalStarCount(star);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 사용 가능한 star 값 목록 조회
        List<Integer> availableStars = boardService.getAvailableStarValues();

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", star);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star";
    }

    @GetMapping("/mobile/star/all")
    public String starAllMobile(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 Model model) {
        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회 (전체)
        List<ProductListDto> products = boardService.getStarProducts(null, offset, size);
        int totalCount = boardService.getTotalStarCount(null);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 사용 가능한 star 값 목록 조회
        List<Integer> availableStars = boardService.getAvailableStarValues();

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", null);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star-mobile";
    }

    @GetMapping("/mobile/star/{star}")
    public String starFilterMobile(@PathVariable Integer star,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Model model) {
        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회 (star 필터링)
        List<ProductListDto> products = boardService.getStarProducts(star, offset, size);
        int totalCount = boardService.getTotalStarCount(star);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 사용 가능한 star 값 목록 조회
        List<Integer> availableStars = boardService.getAvailableStarValues();

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", star);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star-mobile";
    }

    @GetMapping("/satisfied/{count}")
    public String satisfied(@PathVariable String count,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size,
                            HttpServletRequest request,
                            Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/satisfied/" + count + "?page=" + page;
        }
        
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getSatisfiedProducts(countInt, offset, size);
        int totalCount = boardService.getTotalSatisfiedCount(countInt);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("selectedStar", countInt);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star";
    }

    @GetMapping("/mobile/satisfied/{count}")
    public String satisfiedMobile(@PathVariable String count,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model) {
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getSatisfiedProducts(countInt, offset, size);
        int totalCount = boardService.getTotalSatisfiedCount(countInt);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("selectedStar", countInt);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-star-mobile";
    }

    @GetMapping("/shortterm")
    public String shortTerm(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size,
                            HttpServletRequest request,
                            Model model) {
        // 모바일 기기 감지 후 리다이렉트
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/shortterm?page=" + page;
        }

        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getShortTermPopularProducts(offset, size);
        int totalCount = boardService.getShortTermPopularTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 페이징 범위 계산 (10개씩 표시)
        int pageGroupSize = 10;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-shortterm";
    }

    @GetMapping("/mobile/shortterm")
    public String shortTermMobile(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model) {
        // 페이징 계산
        int offset = (page - 1) * size;
        
        // 데이터 조회
        List<ProductListDto> products = boardService.getShortTermPopularProducts(offset, size);
        int totalCount = boardService.getShortTermPopularTotalCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // 모바일에서는 페이징 범위를 5개씩 표시
        int pageGroupSize = 5;
        int currentPageGroup = (page - 1) / pageGroupSize;
        int startPage = currentPageGroup * pageGroupSize + 1;
        int endPage = Math.min(startPage + pageGroupSize - 1, totalPages);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "board-shortterm-mobile";
    }
}

