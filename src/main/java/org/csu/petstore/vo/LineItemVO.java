package org.csu.petstore.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LineItemVO {
    private ItemVO item;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private int quantity;
    public LineItemVO(){};
}
