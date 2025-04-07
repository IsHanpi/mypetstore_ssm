package org.csu.petstore.service;

import org.csu.petstore.entity.Item;
import org.csu.petstore.vo.CartItemVO;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    public List<CartItemVO> getCartItems(String account);
    public boolean containsItemId(String account, String itemId);
    public void addItem(String account,Item item,boolean isInStock);
    public void removeItem(String account,String itemId);
    public void incrementQuantityByItemId(String account,String itemId);
    public void setQuantityByItemId(String account,String itemId,int quantity);
    public List<CartItemVO> getAllCartItems();
    public BigDecimal getTotal(String itemId);
    public BigDecimal getSubTotal();
    public void addItemQuantity(String account,String itemId);
    public void decreaseItemQuantity(String account,String itemId);
    public void updateItemQuantity(String account,String itemId,int number);
    public void createItem(String account,String itemId,int quantity);
    public void deleteItem(String account,String itemId);
    public int getItemQuantity(String account,String itemId);
    public List<Item> getItemList(String account);
    void deleteAllCartItems(String account);
}
