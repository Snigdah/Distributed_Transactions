//package com.leads.seatapoc.order.config;
//
//import feign.RequestInterceptor;
//import org.apache.seata.core.context.RootContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//// Sends the global transaction id (XID) with every Feign call,
//// so inventory-service and payment-service join the same global transaction.
//@Configuration
//public class FeignXidConfig {
//
//    @Bean
//    public RequestInterceptor seataXidInterceptor() {
//        return request -> {
//            String xid = RootContext.getXID();
//            if (xid != null) {
//                request.header(RootContext.KEY_XID, xid);   // header name: TX_XID
//            }
//        };
//    }
//}
