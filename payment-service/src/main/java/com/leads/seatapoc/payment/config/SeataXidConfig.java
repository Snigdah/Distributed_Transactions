//package com.leads.seatapoc.payment.config;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.apache.seata.core.context.RootContext;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//// Reads the global transaction id (XID) sent by order-service
//// and joins this request to that global transaction.
//@Configuration
//public class SeataXidConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new HandlerInterceptor() {
//
//            // Before the request: join the global transaction
//            @Override
//            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//                String xid = request.getHeader(RootContext.KEY_XID);
//                if (xid != null && RootContext.getXID() == null) {
//                    RootContext.bind(xid);
//                }
//                return true;
//            }
//
//            // After the request: clean up
//            @Override
//            public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
//                                        Object handler, Exception ex) {
//                if (RootContext.getXID() != null) {
//                    RootContext.unbind();
//                }
//            }
//        });
//    }
//}
