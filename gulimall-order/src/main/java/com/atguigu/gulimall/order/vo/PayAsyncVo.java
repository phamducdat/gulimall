package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * Payment Result Asynchronous Notification VO
 * <p>
 * Documentation: https://opendocs.alipay.com/open/270/105902
 */
@ToString
@Data
public class PayAsyncVo {
    // Transaction creation time
    private String gmt_create;
    // Character encoding format
    private String charset;
    // Transaction payment time
    private String gmt_payment;
    // Notification time
    private Date notify_time;
    // Order title
    private String subject;
    // Signature
    private String sign;
    // Buyer's Alipay user ID
    private String buyer_id;
    // Product description
    private String body;
    // Invoice amount
    private String invoice_amount;
    // Interface version
    private String version;
    // Notification validation ID
    private String notify_id;
    // Payment amount information
    private String fund_bill_list;
    // Notification type
    private String notify_type;
    // Merchant order number
    private String out_trade_no;
    // Order amount
    private String total_amount;
    // Transaction status
    private String trade_status;
    // Alipay transaction number
    private String trade_no;
    // Authorized app ID
    private String auth_app_id;
    // Actual received amount
    private String receipt_amount;
    // Points amount
    private String point_amount;
    // Developer's app ID
    private String app_id;
    // Payment amount
    private String buyer_pay_amount;
    // Signature type
    private String sign_type;
    // Seller's Alipay user ID
    private String seller_id;
}
