package com.coupang.util;

import jakarta.servlet.http.HttpServletRequest;

public class MobileDetector {
    
    private static final String[] MOBILE_AGENTS = {
        "Mobile", "Android", "iPhone", "iPad", "iPod", 
        "BlackBerry", "Windows Phone", "Opera Mini", 
        "IEMobile", "Mobile Safari"
    };
    
    /**
     * 요청이 모바일 기기에서 온 것인지 확인
     * @param request HTTP 요청 객체
     * @return 모바일 기기면 true, 아니면 false
     */
    public static boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null || userAgent.isEmpty()) {
            return false;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // 모바일 에이전트 확인
        for (String mobileAgent : MOBILE_AGENTS) {
            if (userAgent.contains(mobileAgent.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}



