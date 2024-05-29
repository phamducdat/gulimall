package com.atguigu.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付完成后 支付宝异步回调
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * Handles Alipay asynchronous callback after payment completion.
     */
    @PostMapping("/payed/notify")
    public String handleAlipayAsyncNotify(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException, UnsupportedEncodingException {
        // Verify the signature (ensure the request is sent by Alipay to prevent data tampering and forgery)
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(name, valueStr.toString());
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(),
                alipayTemplate.getSign_type()
        ); // Call SDK to verify the signature

        if (signVerified) {
            // Signature verification successful
            String appId = payAsyncVo.getApp_id();
            // Query order information by order number
            OrderEntity orderEntity = orderService.getOrderByOrderSn(payAsyncVo.getOut_trade_no());

            if (orderEntity != null && alipayTemplate.getApp_id().equals(appId)) {
                // Payment completed -> Save payment information & update order status
                String result = orderService.handPayResult(payAsyncVo);
                return result;
            } else {
                return "error";
            }
        } else {
            // Signature verification failed
            return "error";
        }
    }

}
