package com.smallnine.apiserver.interceptor;

import com.smallnine.apiserver.utils.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * 日誌攔截器
 * 自動記錄所有API請求和響應信息
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final String START_TIME = "startTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        
        // 設置追蹤ID
        LogUtil.setTraceId();
        
        // 獲取客戶端IP
        String clientIp = getClientIp(request);
        
        // 設置請求信息到MDC
        LogUtil.setRequestInfo(request.getMethod(), request.getRequestURI(), clientIp);
        
        // 記錄請求開始
        log.info("請求開始: {} {} - 來源IP: {} - User-Agent: {}", 
                request.getMethod(), 
                request.getRequestURI(),
                clientIp,
                request.getHeader("User-Agent"));
                
        // 記錄請求參數
        if (log.isDebugEnabled()) {
            StringBuilder params = new StringBuilder();
            request.getParameterMap().forEach((key, values) -> {
                params.append(key).append("=").append(String.join(",", values)).append(" ");
            });
            if (params.length() > 0) {
                log.debug("請求參數: {}", params.toString().trim());
            }
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可以在這裡記錄處理完成但還沒返回響應的信息
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                
                // 記錄請求完成
                String status = ex != null ? "FAILED" : "SUCCESS";
                log.info("請求完成: {} {} - 狀態: {} - HTTP狀態碼: {} - 耗時: {}ms", 
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        response.getStatus(),
                        duration);
                
                // 記錄異常信息
                if (ex != null) {
                    log.error("請求處理異常: {}", ex.getMessage(), ex);
                }
                
                // 性能監控
                if (duration > 2000) {
                    log.warn("慢請求警告: {} {} - 耗時: {}ms", 
                            request.getMethod(), request.getRequestURI(), duration);
                }
            }
        } finally {
            // 清除MDC信息
            LogUtil.clear();
        }
    }
    
    /**
     * 獲取真實的客戶端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}