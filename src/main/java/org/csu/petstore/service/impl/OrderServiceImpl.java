package org.csu.petstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Getter;
import lombok.Setter;
import org.csu.petstore.entity.*;
import org.csu.petstore.persistence.*;
import org.csu.petstore.service.CatalogService;
import org.csu.petstore.service.OrderService;
import org.csu.petstore.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@Service("orderService")
public class OrderServiceImpl implements OrderService {
    @Getter
    @Setter
    private OrderVO order;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CatalogService catalogService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private LineItemMapper lineItemMapper;
    @Autowired
    private SequenceMapper sequenceMapper;

    @Override
    public void initOrder(AccountVO account, CartVO cart) {
        order.setUsername(account.getUsername());
        order.setOrderDate(new Date());

        order.setShipToFirstName(account.getFirstName());
        order.setShipToLastName(account.getLastName());
        order.setShipAddress1(account.getAddress1());
        order.setShipAddress2(account.getAddress2());
        order.setShipCity(account.getCity());
        order.setShipState(account.getState());
        order.setShipZip(account.getZip());
        order.setShipCountry(account.getCountry());

        order.setBillToFirstName(account.getFirstName());
        order.setBillToLastName(account.getLastName());
        order.setBillAddress1(account.getAddress1());
        order.setBillAddress2(account.getAddress2());
        order.setBillCity(account.getCity());
        order.setBillState(account.getState());
        order.setBillZip(account.getZip());
        order.setBillCountry(account.getCountry());

        order.setTotalPrice(cart.getSubTotal());

        order.setCreditCard("999 9999 9999 9999");
        order.setExpiryDate("12/03");
        order.setCardType("Visa");
        order.setCourier("UPS");
        order.setLocale("CA");
        order.setStatus("P");

        for (CartItemVO cartItem : cart.getCartItems()) {
            addLineItem(cartItem);
        }
    }
    public void addLineItem(CartItemVO cartItem) {
        if(order.getLineItems() == null){
            order.setLineItems(new ArrayList<LineItemVO>());
        }
        LineItemVO lineItem = new LineItemVO();
        ItemVO itemVO = new ItemVO();
        Item item = itemMapper.selectById(cartItem.getItemId());
        Inventory inventory=inventoryMapper.selectById(cartItem.getItemId());
        Product product = productMapper.selectById(item.getProductId());

        itemVO.setItemId(cartItem.getItemId());
        itemVO.setListPrice(item.getListPrice());
        itemVO.setProductId(product.getProductId());
        itemVO.setProductName(product.getName());
        String [] temp = product.getDescription().split("\"");
        itemVO.setDescriptionImage(temp[1]);
        itemVO.setDescriptionText(temp[2].substring(1));
        itemVO.setQuantity(inventory.getQuantity());

        lineItem.setItem(itemVO);
        lineItem.setUnitPrice(item.getUnitCost());
        lineItem.setQuantity(cartItem.getQuantity());
        lineItem.setTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));

        addLineItem(lineItem);
    }
    public void addLineItem(LineItemVO lineItem) {
        order.getLineItems().add(lineItem);
    }

    public void insertOrder(OrderVO order) {
        order.setOrderId(getNextId("ordernum"));
        for (int i = 0; i < order.getLineItems().size(); i++) {
            LineItemVO lineItem = order.getLineItems().get(i);
            String itemId = lineItem.getItem().getItemId();
            int increment = lineItem.getQuantity();

            Inventory inventory = new Inventory();
            inventory.setItemId(itemId);
            inventory.setQuantity(increment);

            inventoryMapper.updateById(inventory);
        }

        orderMapper.insert(getOrder(order));
        orderStatusMapper.insert(getOrderStatus(order));
        for (int i = 0; i < order.getLineItems().size(); i++) {
            LineItem lineItem = getLineItem(order.getLineItems().get(i),order.getOrderId(),i+1);
            lineItemMapper.insert(lineItem);
        }
    }


    public OrderVO getOrder(int orderId) {
        Order order = orderMapper.selectById(orderId);
        QueryWrapper<LineItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderid",orderId);
        List<LineItem> lineItems = lineItemMapper.selectList(queryWrapper);
        List<LineItemVO> lineItemVOS = new ArrayList<>();

        for (LineItem lineItem : lineItems) {
            LineItemVO lineItemVO = new LineItemVO();
            ItemVO itemVO = catalogService.getItem(lineItem.getItemId());
            lineItemVO.setQuantity(lineItem.getQuantity());
            lineItemVO.setUnitPrice(lineItem.getUnitPrice());
            lineItemVO.setTotal(lineItem.getUnitPrice().multiply(BigDecimal.valueOf(lineItem.getQuantity())));
            lineItemVO.setItem(itemVO);
            lineItemVOS.add(lineItemVO);
        }
        OrderVO orderVO = getOrder(order);
        orderVO.setLineItems(lineItemVOS);
        return orderVO;
    }

    public List<Order> getOrdersByUsername(String username) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userid",username);
        return orderMapper.selectList(queryWrapper);
    }

    public int getNextId(String name) {
        QueryWrapper<Sequence> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        Sequence sequence;
        sequence = sequenceMapper.selectOne(queryWrapper);
        if (sequence == null) {
            throw new RuntimeException("Error: A null sequence was returned from the database (could not get next " + name
                    + " sequence).");
        }
        Sequence parameterObject = new Sequence(name, sequence.getNextId() + 1);
        UpdateWrapper<Sequence> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("name",parameterObject.getName());
        sequenceMapper.update(parameterObject,queryWrapper);
        return sequence.getNextId();
    }

    public Order getOrder(OrderVO orderVO){
        Order order = new Order();
        order.setOrderId(orderVO.getOrderId());
        order.setCourier(orderVO.getCourier());
        order.setOrderDate(orderVO.getOrderDate());
        order.setBillAddress1(orderVO.getBillAddress1());
        order.setBillCity(orderVO.getBillCity());
        order.setBillState(orderVO.getBillState());
        order.setBillAddress2(orderVO.getBillAddress2());
        order.setBillCountry(orderVO.getBillCountry());
        order.setBillToFirstName(orderVO.getBillToFirstName());
        order.setBillToLastName(orderVO.getBillToLastName());
        order.setBillZip(orderVO.getBillZip());
        order.setCardType(orderVO.getCardType());
        order.setCreditCard(orderVO.getCreditCard());
        order.setExpiryDate(orderVO.getExpiryDate());
        order.setLocale(orderVO.getLocale());
        order.setShipAddress1(orderVO.getShipAddress1());
        order.setShipAddress2(orderVO.getShipAddress2());
        order.setShipCountry(orderVO.getShipCountry());
        order.setShipCity(orderVO.getShipCity());
        order.setShipToFirstName(orderVO.getShipToFirstName());
        order.setShipToLastName(orderVO.getShipToLastName());
        order.setShipState(orderVO.getShipState());
        order.setShipZip(orderVO.getShipZip());
        order.setTotalPrice(orderVO.getTotalPrice());
        order.setUsername(orderVO.getUsername());
        return  order;
    }

    public OrderVO getOrder(Order order){
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(order.getOrderId());
        orderVO.setCourier(order.getCourier());
        orderVO.setOrderDate(order.getOrderDate());
        orderVO.setBillAddress1(order.getBillAddress1());
        orderVO.setBillCity(order.getBillCity());
        orderVO.setBillState(order.getBillState());
        orderVO.setBillAddress2(order.getBillAddress2());
        orderVO.setBillCountry(order.getBillCountry());
        orderVO.setBillToFirstName(order.getBillToFirstName());
        orderVO.setBillToLastName(order.getBillToLastName());
        orderVO.setBillZip(order.getBillZip());
        orderVO.setCardType(order.getCardType());
        orderVO.setCreditCard(order.getCreditCard());
        orderVO.setExpiryDate(order.getExpiryDate());
        orderVO.setLocale(order.getLocale());
        orderVO.setShipAddress1(order.getShipAddress1());
        orderVO.setShipAddress2(order.getShipAddress2());
        orderVO.setShipCountry(order.getShipCountry());
        orderVO.setShipCity(order.getShipCity());
        orderVO.setShipToFirstName(order.getShipToFirstName());
        orderVO.setShipToLastName(order.getShipToLastName());
        orderVO.setShipState(order.getShipState());
        orderVO.setShipZip(order.getShipZip());
        orderVO.setTotalPrice(order.getTotalPrice());
        orderVO.setUsername(order.getUsername());
        return orderVO;
    }

    public OrderStatus getOrderStatus(OrderVO order){
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(order.getOrderId());
        orderStatus.setTimestamp(order.getOrderDate());
        orderStatus.setStatus(order.getStatus());
        orderStatus.setLineNum(order.getOrderId());
        return orderStatus;
    }

    public LineItem getLineItem(LineItemVO lineItemVO,int orderId,int lineNum){
        LineItem lineItem = new LineItem();
        lineItem.setItemId(lineItemVO.getItem().getItemId());
        lineItem.setOrderId(orderId);
        lineItem.setQuantity(lineItemVO.getQuantity());
        lineItem.setUnitPrice(lineItemVO.getUnitPrice());
        lineItem.setLineNum(lineNum);
        return lineItem;
    }

    @Override
    public String getStatusByOrderId(int orderid) {
        return orderStatusMapper.selectById(orderid).getStatus();
    }
}
