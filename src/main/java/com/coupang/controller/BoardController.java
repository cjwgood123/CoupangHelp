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

    /** 10페이지(200건) 단위 블록 로드 */
    private static final int BLOCK_PAGE_SIZE = 20;
    private static final int BLOCK_SIZE = 200;

    /** 구매했어요 메인: yymm 없으면 DB에 있는 가장 마지막 연월로 리다이렉트 */
    @GetMapping("/board/{count}")
    public String board(@PathVariable String count,
                        @RequestParam(required = false) String yymm,
                        @RequestParam(defaultValue = "1") int block,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(required = false) String search,
                        HttpServletRequest request,
                        Model model) {
        String defaultYymm = boardService.getLatestYymm();
        if (defaultYymm == null) defaultYymm = BoardService.currentYymm();
        if (MobileDetector.isMobile(request)) {
            String url = "/mobile/board/" + count + "?block=" + block + "&page=" + page;
            if (yymm != null && yymm.matches("\\d{6}")) url += "&yymm=" + yymm;
            else url += "&yymm=" + defaultYymm;
            if (search != null && !search.trim().isEmpty())
                url += "&search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:" + url;
        }
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }
        if (yymm == null || !yymm.matches("\\d{6}")) {
            String q = "?yymm=" + defaultYymm + "&block=" + block + "&page=" + page;
            if (search != null && !search.trim().isEmpty())
                q += "&search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/board/" + count + q;
        }
        String useYymm = yymm;
        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getProducts(useYymm, countInt, offset, BLOCK_SIZE, search);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("count", countInt);
        model.addAttribute("yymm", useYymm);
        model.addAttribute("month", formatMonthLabel(useYymm));
        model.addAttribute("search", search);
        model.addAttribute("blockMode", true);
        List<Integer> months2025 = boardService.getAvailableMonthsForYear(2025);
        List<Integer> months2026 = boardService.getAvailableMonthsForYear(2026);
        model.addAttribute("availableMonths2025", months2025);
        model.addAttribute("availableMonths2026", months2026);
        model.addAttribute("availableYymm2025", months2025.stream().map(m -> "2025" + String.format("%02d", m)).toList());
        model.addAttribute("availableYymm2026", months2026.stream().map(m -> "2026" + String.format("%02d", m)).toList());
        model.addAttribute("firstYymm2025", months2025.isEmpty() ? null : "2025" + String.format("%02d", months2025.get(0)));
        model.addAttribute("firstYymm2026", months2026.isEmpty() ? null : "2026" + String.format("%02d", months2026.get(0)));

        return "board";
    }

    @GetMapping("/mobile/board/{count}")
    public String boardMobile(@PathVariable String count,
                              @RequestParam(required = false) String yymm,
                              @RequestParam(defaultValue = "1") int block,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) String search,
                              Model model) {
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }
        String defaultYymm = boardService.getLatestYymm();
        if (defaultYymm == null) defaultYymm = BoardService.currentYymm();
        if (yymm == null || !yymm.matches("\\d{6}")) {
            String q = "?yymm=" + defaultYymm + "&block=" + block + "&page=" + page;
            if (search != null && !search.trim().isEmpty())
                q += "&search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/mobile/board/" + count + q;
        }
        String useYymm = yymm;
        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getProducts(useYymm, countInt, offset, BLOCK_SIZE, search);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("count", countInt);
        model.addAttribute("yymm", useYymm);
        model.addAttribute("month", formatMonthLabel(useYymm));
        model.addAttribute("search", search);
        model.addAttribute("blockMode", true);
        List<Integer> months2025 = boardService.getAvailableMonthsForYear(2025);
        List<Integer> months2026 = boardService.getAvailableMonthsForYear(2026);
        model.addAttribute("availableMonths2025", months2025);
        model.addAttribute("availableMonths2026", months2026);
        model.addAttribute("availableYymm2025", months2025.stream().map(m -> "2025" + String.format("%02d", m)).toList());
        model.addAttribute("availableYymm2026", months2026.stream().map(m -> "2026" + String.format("%02d", m)).toList());
        model.addAttribute("firstYymm2025", months2025.isEmpty() ? null : "2025" + String.format("%02d", months2025.get(0)));
        model.addAttribute("firstYymm2026", months2026.isEmpty() ? null : "2026" + String.format("%02d", months2026.get(0)));

        return "board-mobile";
    }

    private static String formatMonthLabel(String yymm) {
        if (yymm == null || yymm.length() < 6) return yymm != null ? yymm : "";
        return yymm.substring(0, 4) + "년 " + Integer.parseInt(yymm.substring(4, 6)) + "월";
    }

    /** 기존 URL 호환: /2025-11/all/{count} → /board/{count}?yymm=202511 */
    @GetMapping("/2025-11/all/{count}")
    public String boardLegacy(@PathVariable String count,
                              @RequestParam(defaultValue = "1") int block,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) String search,
                              HttpServletRequest request) {
        String q = "?yymm=202511&block=" + block + "&page=" + page;
        if (search != null && !search.trim().isEmpty())
            q += "&search=" + java.net.URLEncoder.encode(search, java.nio.charset.StandardCharsets.UTF_8);
        return "redirect:/board/" + count + q;
    }

    @GetMapping("/mobile/2025-11/all/{count}")
    public String boardMobileLegacy(@PathVariable String count,
                                    @RequestParam(defaultValue = "1") int block,
                                    @RequestParam(defaultValue = "1") int page) {
        return "redirect:/mobile/board/" + count + "?yymm=202511&block=" + block + "&page=" + page;
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
    public String starAll(@RequestParam(defaultValue = "1") int block,
                          @RequestParam(defaultValue = "1") int page,
                          HttpServletRequest request,
                          Model model) {
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/star/all?block=" + block + "&page=" + page;
        }

        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getStarProducts(null, offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        List<Integer> availableStars = boardService.getAvailableStarValues();

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", null);
        model.addAttribute("blockMode", true);

        return "board-star";
    }

    @GetMapping("/star/{star}")
    public String starFilter(@PathVariable Integer star,
                             @RequestParam(defaultValue = "1") int block,
                             @RequestParam(defaultValue = "1") int page,
                             HttpServletRequest request,
                             Model model) {
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/star/" + star + "?block=" + block + "&page=" + page;
        }

        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getStarProducts(star, offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        List<Integer> availableStars = boardService.getAvailableStarValues();

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", star);
        model.addAttribute("blockMode", true);

        return "board-star";
    }

    @GetMapping("/mobile/star/all")
    public String starAllMobile(@RequestParam(defaultValue = "1") int block,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model) {
        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getStarProducts(null, offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        List<Integer> availableStars = boardService.getAvailableStarValues();

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", null);
        model.addAttribute("blockMode", true);

        return "board-star-mobile";
    }

    @GetMapping("/mobile/star/{star}")
    public String starFilterMobile(@PathVariable Integer star,
                                    @RequestParam(defaultValue = "1") int block,
                                    @RequestParam(defaultValue = "1") int page,
                                    Model model) {
        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getStarProducts(star, offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        List<Integer> availableStars = boardService.getAvailableStarValues();

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("availableStars", availableStars);
        model.addAttribute("selectedStar", star);
        model.addAttribute("blockMode", true);

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
    public String shortTerm(@RequestParam(defaultValue = "1") int block,
                            @RequestParam(defaultValue = "1") int page,
                            HttpServletRequest request,
                            Model model) {
        if (MobileDetector.isMobile(request)) {
            return "redirect:/mobile/shortterm?block=" + block + "&page=" + page;
        }

        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getShortTermPopularProducts(offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("blockMode", true);

        return "board-shortterm";
    }

    @GetMapping("/mobile/shortterm")
    public String shortTermMobile(@RequestParam(defaultValue = "1") int block,
                                  @RequestParam(defaultValue = "1") int page,
                                  Model model) {
        int offset = (block - 1) * BLOCK_SIZE;
        List<ProductListDto> products = boardService.getShortTermPopularProducts(offset, BLOCK_SIZE);
        int totalPagesInBlock = products.isEmpty() ? 0 : (int) Math.ceil((double) products.size() / BLOCK_PAGE_SIZE);
        boolean hasNextBlock = products.size() >= BLOCK_SIZE;

        model.addAttribute("products", products);
        model.addAttribute("currentBlock", block);
        model.addAttribute("currentPage", Math.min(page, Math.max(1, totalPagesInBlock)));
        model.addAttribute("totalPagesInBlock", totalPagesInBlock);
        model.addAttribute("totalCount", -1);
        model.addAttribute("hasNextBlock", hasNextBlock);
        model.addAttribute("blockMode", true);

        return "board-shortterm-mobile";
    }

    @GetMapping("/sales-chart")
    public String salesChart(HttpServletRequest request, Model model) {
        // 모바일 기기 감지 후 리다이렉트 (필요시)
        // if (MobileDetector.isMobile(request)) {
        //     return "redirect:/mobile/sales-chart";
        // }

        return "sales-chart";
    }
}

