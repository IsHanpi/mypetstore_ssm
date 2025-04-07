package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("cart")
@Data
public class CartDBItem {
    @TableField("LoginAccount")
    private String LoginAccount;
    @TableField("ItemId")
    private String ItemId;
    @TableField("Quantity")
    private int Quantity;
    public CartDBItem() {}
    public CartDBItem(String Account, String ItemId, int Quantity) {
        this.LoginAccount = Account;
        this.ItemId = ItemId;
        this.Quantity = Quantity;
    }
}
