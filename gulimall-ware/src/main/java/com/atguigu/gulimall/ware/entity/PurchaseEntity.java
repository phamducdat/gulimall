package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Purchase Information
 * <p>
 * Represents the details of a purchase order.
 *
 * @autor zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Purchase order ID
     */
    @TableId
    private Long id;

    /**
     * Assignee ID
     */
    private Long assigneeId;

    /**
     * Assignee name
     */
    private String assigneeName;

    /**
     * Contact phone number
     */
    private String phone;

    /**
     * Priority level
     */
    private Integer priority;

    /**
     * Status
     */
    private Integer status;

    /**
     * Warehouse ID
     */
    private Long wareId;

    /**
     * Total amount
     */
    private BigDecimal amount;

    /**
     * Creation date
     */
    private Date createTime;

    /**
     * Update date
     */
    private Date updateTime;
}
