package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Inventory work order
 *
 * @author zhengyuli
 * @email zli78122@usc.edu
 * @date 2020-06-23 00:25:27
 */
@Data
@TableName("wms_ware_order_task")
public class WareOrderTaskEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * Order SN (Serial Number)
     */
    private String orderSn;

    /**
     * Consignee (Recipient)
     */
    private String consignee;

    /**
     * Consignee's phone number
     */
    private String consigneeTel;

    /**
     * Delivery address
     */
    private String deliveryAddress;

    /**
     * Order comments
     */
    private String orderComment;

    /**
     * Payment method [1: Online payment, 2: Cash on delivery]
     */
    private Integer paymentWay;

    /**
     * Task status
     */
    private Integer taskStatus;

    /**
     * Order description
     */
    private String orderBody;

    /**
     * Tracking number
     */
    private String trackingNo;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Warehouse ID
     */
    private Long wareId;

    /**
     * Task comments
     */
    private String taskComment;

}
