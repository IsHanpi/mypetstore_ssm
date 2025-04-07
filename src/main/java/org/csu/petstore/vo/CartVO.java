package org.csu.petstore.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVO {
    private List<CartItemVO> cartItems;
    private int numOfItems;
    private BigDecimal subTotal;
}
