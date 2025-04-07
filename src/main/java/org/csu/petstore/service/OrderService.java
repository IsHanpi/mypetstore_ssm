package org.csu.petstore.service;

import org.csu.petstore.entity.LineItem;
import org.csu.petstore.entity.Order;
import org.csu.petstore.entity.OrderStatus;
import org.csu.petstore.vo.*;

import java.util.List;

public interface OrderService {
    public void initOrder(AccountVO account, CartVO cart);
    public void addLineItem(CartItemVO cartItem);
    public void addLineItem(LineItemVO lineItem);
    public void insertOrder(OrderVO order);
    public OrderVO getOrder(int orderId);
    public List<Order> getOrdersByUsername(String username);
    public int getNextId(String name);
    public Order getOrder(OrderVO orderVO);
    public OrderVO getOrder(Order order);
    public OrderStatus getOrderStatus(OrderVO order);
    public LineItem getLineItem(LineItemVO lineItemVO, int orderId, int lineNum);
    String getStatusByOrderId(int orderid);
}
