package br.com.easybiz.security;

import java.io.IOException;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateInfo> clients = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        boolean isRateLimited = "POST".equalsIgnoreCase(method) && (
                "/auth/login".equals(path)
                || "/usuarios".equals(path)
                || "/auth/esqueci-senha".equals(path)
                || "/auth/redefinir-senha".equals(path)
        );

        if (!isRateLimited) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(httpRequest);
        String key = clientIp + ":" + path;

        RateInfo info = clients.compute(key, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateInfo(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (info.count.get() > MAX_REQUESTS) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Limite de requisicoes excedido. Tente novamente em 1 minuto.\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Remove entradas expiradas a cada 5 minutos para evitar memory leak.
     */
    @Scheduled(fixedRate = 300_000)
    public void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, RateInfo>> it = clients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, RateInfo> entry = it.next();
            if (now - entry.getValue().windowStart > WINDOW_MS) {
                it.remove();
            }
        }
    }

    /**
     * Usa apenas request.getRemoteAddr() para evitar spoofing via X-Forwarded-For.
     * Quando atrás de proxy reverso (Railway, Nginx), o Spring resolve o IP real
     * via server.forward-headers-strategy=framework no application.properties.
     */
    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static class RateInfo {
        final long windowStart;
        final AtomicInteger count;

        RateInfo(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}