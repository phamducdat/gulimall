package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResponseVO;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Login Interceptor - LoginUserInterceptor
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    // ThreadLocal: Data shared by the same thread
    // Store MemberResponseVO object in ThreadLocal
    public static ThreadLocal<MemberResponseVO> loginUser = new InheritableThreadLocal<>();

    /**
     * Intercept before the target method is executed
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // Allow request: Query order information by order number
        boolean statusMatch = antPathMatcher.match("/order/order/status/**", uri);
        // Allow request: Alipay asynchronous callback after payment completion
        boolean payedMatch = antPathMatcher.match("/payed/notify", uri);
        // Allow request: Query Alipay payment status
        boolean queryPayMatch = antPathMatcher.match("/queryPayStatus", uri);

        if (statusMatch || payedMatch || queryPayMatch) {
            // Allow
            return true;
        }

        MemberResponseVO loginMember = (MemberResponseVO) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginMember != null) {
            // User has logged in -> Allow
            // Store loginMember in ThreadLocal
            loginUser.set(loginMember);
            return true;
        } else {
            // User not logged in -> Redirect to login page
            request.getSession().setAttribute("msg", "Please log in first!");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
