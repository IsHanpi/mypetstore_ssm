package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@TableName("lineitem")
@Data
public class LineItem {
    @TableField("orderid")
    private int orderId;
    @TableField("linenum")
    private int lineNum;
    private int quantity;
    @TableField("itemid")
    private String itemId;
    @TableField("unitprice")
    private BigDecimal unitPrice;
    @TableField(exist = false)
    private Item item;
    @TableField(exist = false)
    private BigDecimal total;
    public LineItem() {}
    public void setItem(Item item) {
        this.item = item;
        calculateTotal();
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }
    private void calculateTotal() {
        if(item != null && item.getListPrice() != null){
            total = item.getListPrice().multiply(new BigDecimal(quantity));
        }else {
            total = null;
        }
    }
}
