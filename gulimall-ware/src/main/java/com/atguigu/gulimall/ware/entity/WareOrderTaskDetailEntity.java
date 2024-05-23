package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Inventory Work Order Detail
 *
 * @author zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("wms_ware_order_task_detail")
//@TableName("product_physical_stock")
public class WareOrderTaskDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU Name
     */
    private String skuName;

    /**
     * Quantity purchased
     */
    private Integer skuNum;

    /**
     * Work order ID
     */
    private Long taskId;

    /**
     * Warehouse ID
     */
    private Long wareId;

    /**
     * Lock status (1 - Locked, 2 - Unlocked, 3 - Deducted)
     */
    private Integer lockStatus;
}

