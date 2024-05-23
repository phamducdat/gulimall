package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Order Transfer Object
 * <p>
 * Represents the data transfer object for an order.
 *
 * @autor zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
public class OrderTo {

    /**
     * ID
     */
    private Long id;

    /**
     * Member ID
     */
    private Long memberId;

    /**
     * Order number (order SN)
     */
    private String orderSn;

    /**
     * Coupon used
     */
    private Long couponId;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Member username
     */
    private String memberUsername;

    /**
     * Total order amount
     */
    private BigDecimal totalAmount;

    /**
     * Amount payable
     */
    private BigDecimal payAmount;

    /**
     * Freight amount
     */
    private BigDecimal freightAmount;

    /**
     * Promotion optimization amount (promotional price, full reduction, tiered price)
     */
    private BigDecimal promotionAmount;

    /**
     * Integration deduction amount
     */
    private BigDecimal integrationAmount;

    /**
     * Coupon deduction amount
     */
    private BigDecimal couponAmount;

    /**
     * Discount amount used for backend order adjustments
     */
    private BigDecimal discountAmount;

    /**
     * Payment method [1->Alipay; 2->WeChat; 3->UnionPay; 4->Cash on delivery]
     */
    private Integer payType;

    /**
     * Order source [0->PC order; 1->app order]
     */
    private Integer sourceType;

    /**
     * Order status [0->Pending payment; 1->Pending shipment; 2->Shipped; 3->Completed; 4->Closed; 5->Invalid order]
     */
    private Integer status;

    /**
     * Delivery company (shipping method)
     */
    private String deliveryCompany;

    /**
     * Delivery tracking number
     */
    private String deliverySn;

    /**
     * Auto-confirm time (days)
     */
    private Integer autoConfirmDay;

    /**
     * Points that can be earned
     */
    private Integer integration;

    /**
     * Growth value that can be earned
     */
    private Integer growth;

    /**
     * Invoice type [0->No invoice; 1->Electronic invoice; 2->Paper invoice]
     */
    private Integer billType;

    /**
     * Invoice header
     */
    private String billHeader;

    /**
     * Invoice content
     */
    private String billContent;

    /**
     * Bill recipient phone number
     */
    private String billReceiverPhone;

    /**
     * Bill recipient email
     */
    private String billReceiverEmail;

    /**
     * Recipient name
     */
    private String receiverName;

    /**
     * Recipient phone number
     */
    private String receiverPhone;

    /**
     * Recipient postal code
     */
    private String receiverPostCode;

    /**
     * Province/municipality
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
     * Detailed address
     */
    private String receiverDetailAddress;

    /**
     * Order note
     */
    private String note;

    /**
     * Confirm receipt status [0->Not confirmed; 1->Confirmed]
     */
    private Integer confirmStatus;

    /**
     * Deletion status [0->Not deleted; 1->Deleted]
     */
    private Integer deleteStatus;

    /**
     * Points used when placing the order
     */
    private Integer useIntegration;

    /**
     * Payment time
     */
    private Date paymentTime;

    /**
     * Shipment time
     */
    private Date deliveryTime;

    /**
     * Receipt confirmation time
     */
    private Date receiveTime;

    /**
     * Review time
     */
    private Date commentTime;

    /**
     * Modification time
     */
    private Date modifyTime;
}
