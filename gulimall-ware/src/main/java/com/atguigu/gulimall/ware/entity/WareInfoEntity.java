package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Warehouse Information
 * <p>
 * Represents the information of a warehouse.
 *
 * @autor zhengyuli
 * @date 2020-06-23 00:25:27
 */
@Data
@TableName("wms_ware_info")
public class WareInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * Warehouse name
     */
    private String name;

    /**
     * Warehouse address
     */
    private String address;

    /**
     * Area code
     */
    private String areacode;
}
