package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("orderstatus")
public class OrderStatus {
    @TableId("orderid")
    private int orderId;
    @TableField("linenum")
    private int lineNum;
    @TableField("timestamp")
    private Date timestamp;
    private String status;
}
