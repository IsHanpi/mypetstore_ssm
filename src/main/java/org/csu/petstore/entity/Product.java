package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Product {
    @TableId(value = "productid")  // TableId用于主键
    private String productId;
    @TableField("category")  //TableField用于其他字段
    private String categoryId;
    private String name;
    @TableField("descn")
    private String description;
}
