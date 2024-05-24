package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * Brand
 *
 * @Author Zhengyuli
 * @Email zli78122@usc.edu
 * @Date 2020-06-22 23:03:41
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Brand ID
     */
    @NotNull(message = "Brand ID must be specified when updating", groups = {UpdateGroup.class})
    @Null(message = "Brand ID must not be specified when adding", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * Brand Name
     */
    @NotBlank(message = "Brand name is required", groups = {AddGroup.class, UpdateGroup.class})
    private String name;
    /**
     * Brand Logo URL
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "Logo must be a valid URL", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * Description
     */
    private String descript;
    /**
     * Display Status [0 - Not Displayed; 1 - Displayed]
     */
    @NotNull(groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
    @ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * Search Initial Letter
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "Search initial letter must be a letter", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * Sort Order
     */
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "Sort order must be greater than or equal to 0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;
}

