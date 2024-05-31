package com.atguigu.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Payment Information Table
 * <p>
 * Author: zhengyuli
 * Email: zli78122@usc.edu
 * Date: 2020-06-23 00:20:14
 */
@Data
@TableName("oms_payment_info")
public class PaymentInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * Order number (external business number)
     */
    private String orderSn;

    /**
     * Order id
     */
    private Long orderId;

    /**
     * Alipay transaction number
     */
    private String alipayTradeNo;

    /**
     * Total payment amount
     */
    private BigDecimal totalAmount;

    /**
     * Transaction content
     */
    private String subject;

    /**
     * Payment status
     */
    private String paymentStatus;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Confirmation time
     */
    private Date confirmTime;

    /**
     * Callback content
     */
    private String callbackContent;

    /**
     * Callback time
     */
    private Date callbackTime;
}
