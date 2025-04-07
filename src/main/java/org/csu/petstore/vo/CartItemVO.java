package org.csu.petstore.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private String itemId;
    private String proDescription;
    private String proName;
    private BigDecimal listPrice;
    private int quantity;
    private boolean inStock;
    private BigDecimal total;
}
