package com.atguigu.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Order
 * <p>
 * Author: zhengyuli
 * Email: zli78122@usc.edu
 * Date: 2020-06-23 00:20:14
 */
@Data
@TableName("oms_order")
public class OrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * Member ID
     */
    private Long memberId;

    /**
     * Order Number
     */
    private String orderSn;

    /**
     * Used Coupon
     */
    private Long couponId;

    /**
     * Creation Time
     */
    private Date createTime;

    /**
     * Member Username
     */
    private String memberUsername;

    /**
     * Total Order Amount
     */
    private BigDecimal totalAmount;

    /**
     * Payable Amount
     */
    private BigDecimal payAmount;

    /**
     * Freight Amount
     */
    private BigDecimal freightAmount;

    /**
     * Promotion Discount Amount (promotional price, full reduction, tiered price)
     */
    private BigDecimal promotionAmount;

    /**
     * Integration Deduction Amount
     */
    private BigDecimal integrationAmount;

    /**
     * Coupon Deduction Amount
     */
    private BigDecimal couponAmount;

    /**
     * Backend Adjustment Discount Amount
     */
    private BigDecimal discountAmount;

    /**
     * Payment Method [1->Alipay; 2->WeChat; 3->UnionPay; 4->Cash on Delivery]
     */
    private Integer payType;

    /**
     * Order Source [0->PC Order; 1->App Order]
     */
    private Integer sourceType;

    /**
     * Order Status [0->Pending Payment; 1->Pending Shipment; 2->Shipped; 3->Completed; 4->Closed; 5->Invalid Order]
     */
    private Integer status;

    /**
     * Delivery Company (Shipping Method)
     */
    private String deliveryCompany;

    /**
     * Delivery Tracking Number
     */
    private String deliverySn;

    /**
     * Automatic Confirmation Time (days)
     */
    private Integer autoConfirmDay;

    /**
     * Earnable Integration Points
     */
    private Integer integration;

    /**
     * Earnable Growth Points
     */
    private Integer growth;

    /**
     * Invoice Type [0->No Invoice; 1->Electronic Invoice; 2->Paper Invoice]
     */
    private Integer billType;

    /**
     * Invoice Header
     */
    private String billHeader;

    /**
     * Invoice Content
     */
    private String billContent;

    /**
     * Invoice Receiver Phone Number
     */
    private String billReceiverPhone;

    /**
     * Invoice Receiver Email
     */
    private String billReceiverEmail;

    /**
     * Receiver Name
     */
    private String receiverName;

    /**
     * Receiver Phone Number
     */
    private String receiverPhone;

    /**
     * Receiver Postal Code
     */
    private String receiverPostCode;

    /**
     * Province/Direct-controlled Municipality
     */
    private String receiverProvince;

    /**
     * City
     */
    private String receiverCity;

    /**
     * Region
     */
    private String receiverRegion;

    /**
     * Detailed Address
     */
    private String receiverDetailAddress;

    /**
     * Order Note
     */
    private String note;

    /**
     * Confirmation Status [0->Not Confirmed; 1->Confirmed]
     */
    private Integer confirmStatus;

    /**
     * Deletion Status [0->Not Deleted; 1->Deleted]
     */
    private Integer deleteStatus;

    /**
     * Integration Points Used at Order Placement
     */
    private Integer useIntegration;

    /**
     * Payment Time
     */
    private Date paymentTime;

    /**
     * Shipment Time
     */
    private Date deliveryTime;

    /**
     * Receipt Confirmation Time
     */
    private Date receiveTime;

    /**
     * Comment Time
     */
    private Date commentTime;

    /**
     * Last Modification Time
     */
    private Date modifyTime;

    /**
     * Order Items Collection
     * <p>
     * The orderItems attribute does not exist in the database table
     */
    @TableField(exist = false)
    private List<OrderItemEntity> orderItems;
}

