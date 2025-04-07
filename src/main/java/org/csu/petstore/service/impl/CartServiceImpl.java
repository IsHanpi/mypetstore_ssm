package org.csu.petstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Getter;
import lombok.Setter;
import org.csu.petstore.entity.CartDBItem;
import org.csu.petstore.entity.Item;
import org.csu.petstore.entity.Product;
import org.csu.petstore.persistence.CartMapper;
import org.csu.petstore.persistence.InventoryMapper;
import org.csu.petstore.persistence.ItemMapper;
import org.csu.petstore.persistence.ProductMapper;
import org.csu.petstore.service.CartService;
import org.csu.petstore.vo.CartItemVO;
import org.csu.petstore.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service("cartService")
public class CartServiceImpl implements CartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Getter
    @Setter
    private CartVO cart;

    //数据库中购物车商品
    public List<CartItemVO> getCartItems(String account) {
        List<CartItemVO> cartItems = new ArrayList<>();
        QueryWrapper<CartDBItem> wrapper = new QueryWrapper<>();
        wrapper.eq("LoginAccount", account);
        List<CartDBItem> list = cartMapper.selectList(wrapper);
        for (CartDBItem cartDBItem : list) {
            CartItemVO cartItemVO = new CartItemVO();
            Item item = itemMapper.selectById(cartDBItem.getItemId());
            cartItemVO.setItemId(item.getItemId());
            Product product = productMapper.selectById(item.getProductId());
            String subString1 = product.getDescription();
            subString1 = subString1.substring(0, 12) + "../";
            cartItemVO.setProDescription(subString1);
            cartItemVO.setListPrice(item.getListPrice());
            cartItemVO.setTotal(item.getListPrice().multiply(BigDecimal.valueOf(cartItemVO.getQuantity())));
            cartItemVO.setInStock(true);
            cartItems.add(cartItemVO);
        }
        return cartItems;
    }

    //某商品是否在购物车中
    public boolean containsItemId(String account, String itemId) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount", account);
        queryWrapper.eq("ItemId", itemId);
        return cartMapper.selectOne(queryWrapper) != null;
    }

    //商品加入购物车时，原本没有则新建
    public void addItem(String account, Item item, boolean isInStock) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount", account);
        queryWrapper.eq("ItemId", item.getItemId());
        CartDBItem cartDBItem = cartMapper.selectOne(queryWrapper);
        if (cartDBItem == null) {
            cartDBItem = new CartDBItem();
            cartDBItem.setItemId(item.getItemId());
            cartDBItem.setQuantity(1);
            cartDBItem.setLoginAccount(account);
            cartMapper.insert(cartDBItem);

            Product product = productMapper.selectById(item.getProductId());

            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setQuantity(1);
            createCartItem(cartItemVO,item,product,item.getItemId(),cartDBItem);
            cartItemVO.setTotal(item.getListPrice());
            List<CartItemVO> cartItemVOS = cart.getCartItems();
            if(cartItemVOS == null) {
                cartItemVOS = new ArrayList<>();
            }
            cartItemVOS.add(cartItemVO);
            cart.setCartItems(cartItemVOS);
            if(cart.getNumOfItems() == 0){
                loadCartFromDB(account);
            }else cart.setNumOfItems(cart.getNumOfItems() + 1);
            cart.setSubTotal(cart.getSubTotal().add(item.getListPrice()));
        }else {
            UpdateWrapper<CartDBItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("LoginAccount", account).eq("ItemId", item.getItemId()).set("Quantity", cartDBItem.getQuantity()+1);
            cartMapper.update(updateWrapper);
            cart.setSubTotal(cart.getSubTotal().add(item.getListPrice()));
            if(cart.getCartItems() == null){
                loadCartFromDB(account);
            }
        }
    }
    public void createCartItem(CartItemVO cartItemVO, Item item, Product product, String itemId, CartDBItem cartDBItem) {
        cartItemVO.setItemId(itemId);
        cartItemVO.setProName(product.getName());
        String subString1 = product.getDescription();
        subString1 = subString1.substring(0, 12) + "../" + subString1.substring(12);
        cartItemVO.setProDescription(subString1);
        cartItemVO.setInStock(inventoryMapper.selectById(itemId).getQuantity() > 0);
        cartItemVO.setListPrice(item.getListPrice());
    }
    public void loadCartFromDB(String account) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount", account);
        List<CartDBItem> list = cartMapper.selectList(queryWrapper);
        List<CartItemVO> cartItemVOS = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.valueOf(0);
        for (CartDBItem cartDBItem : list) {
            CartItemVO cartItemVO = new CartItemVO();
            Item item = itemMapper.selectById(cartDBItem.getItemId());
            Product product = productMapper.selectById(item.getProductId());

            cartItemVO.setQuantity(cartDBItem.getQuantity());
            createCartItem(cartItemVO,item,product,cartDBItem.getItemId(),cartDBItem);
            cartItemVO.setTotal(item.getListPrice().multiply(BigDecimal.valueOf(cartDBItem.getQuantity())));

            cartItemVOS.add(cartItemVO);
            subTotal = subTotal.add(cartItemVO.getTotal());
        }
        cart.setCartItems(cartItemVOS);
        cart.setNumOfItems(list.size());
        cart.setSubTotal(subTotal);
    }

    //移除购物车中商品
    public void removeItem(String account, String itemId) {
        CartItemVO cartItemVO = getCartItemFromCart(itemId);
        if (cartItemVO != null) {
            cart.getCartItems().remove(cartItemVO);
            deleteItem(account,itemId);
            calculateTotal();
        }else {
            deleteItem(account,itemId);
            loadCartFromDB(account);
        }
    }
    public CartItemVO getCartItemFromCart(String itemId) {
        List<CartItemVO> list = cart.getCartItems();
        CartItemVO cartItemVO = new CartItemVO();
        for (CartItemVO cartItemVO1 : list) {
            if (cartItemVO1.getItemId().equals(itemId)) {
                cartItemVO = cartItemVO1;
                break;
            }
        }
        return cartItemVO;
    }
    public BigDecimal calculateTotal() {
        List<CartItemVO> cartItemVOS = cart.getCartItems();
        BigDecimal subTotal = BigDecimal.valueOf(0);
        for (CartItemVO cartItemVO : cartItemVOS) {
            subTotal = subTotal.add(cartItemVO.getTotal());
        }
        return subTotal;
    }

    //增加购物车中商品数量
    public void incrementQuantityByItemId(String account, String itemId) {
        CartItemVO cartItemVO = getCartItemFromCart(itemId);
        if (cartItemVO == null) {
            loadCartFromDB(account);
            cartItemVO = getCartItemFromCart(itemId);
        }
        addItemQuantity(account,itemId);
        cartItemVO.setQuantity(cartItemVO.getQuantity()+1);
        cartItemVO.setTotal(cartItemVO.getTotal().add(cartItemVO.getListPrice()));
    }

    //设置商品数量
    public void setQuantityByItemId(String account, String itemId, int quantity) {
        updateItemQuantity(account,itemId,quantity);
        CartItemVO cartItemVO = getCartItemFromCart(itemId);
        if(cartItemVO != null){
            cartItemVO.setQuantity(quantity);
            cartItemVO.setTotal(cartItemVO.getListPrice().multiply(BigDecimal.valueOf(quantity)));
        }else loadCartFromDB(account);
    }

    //获取购物车中的商品列表
    public List<CartItemVO> getAllCartItems() {
        return cart.getCartItems();
    }

    //获取单个商品的总价
    public BigDecimal getTotal(String itemId) {
        BigDecimal total = BigDecimal.valueOf(0);
        List<CartItemVO> cartItemVOS = cart.getCartItems();
        for (CartItemVO cartItemVO : cartItemVOS) {
            if (cartItemVO.getItemId().equals(itemId)) {
                total = total.add(cartItemVO.getTotal());
                break;
            }
        }
        return total;
    }

    //所有商品的总价
    public BigDecimal getSubTotal() {
        return cart.getSubTotal();
    }

    //用户购物车商品
    public List<Item> getItemList(String account){
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        List<CartDBItem> list = cartMapper.selectList(queryWrapper);
        List<Item> items = new ArrayList<>();
        for (CartDBItem cartDBItem : list) {
            Item item = new Item();
            item = itemMapper.selectById(cartDBItem.getItemId());
            items.add(item);
        }
        return items;
    }

    //清空购物车商品
    public void deleteAllCartItems(String account) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        cartMapper.delete(queryWrapper);
    }

    //添加商品数量
    public void addItemQuantity(String account, String itemId) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        queryWrapper.eq("ItemId",itemId);
        CartDBItem cartDBItem = cartMapper.selectOne(queryWrapper);
        UpdateWrapper<CartDBItem> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("LoginAccount",account);
        updateWrapper.eq("ItemId",itemId);
        if (cartDBItem != null) {
            cartDBItem.setQuantity(cartDBItem.getQuantity()+1);
            cartMapper.update(cartDBItem, updateWrapper);
        }
    }

    //减少商品数量
    public void decreaseItemQuantity(String account, String itemId) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        queryWrapper.eq("ItemId",itemId);
        CartDBItem cartDBItem = cartMapper.selectOne(queryWrapper);
        if (cartDBItem != null) {
            cartDBItem.setQuantity(cartDBItem.getQuantity()-1);
            UpdateWrapper<CartDBItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("LoginAccount",account);
            updateWrapper.eq("ItemId",itemId);
            cartMapper.update(cartDBItem, updateWrapper);
        }
    }

    //更新数据库商品数量
    public void updateItemQuantity(String account, String itemId, int number) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        queryWrapper.eq("ItemId",itemId);
        CartDBItem cartDBItem = cartMapper.selectOne(queryWrapper);
        if (cartDBItem != null) {
            cartDBItem.setQuantity(number);
            UpdateWrapper<CartDBItem> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("LoginAccount",account);
            updateWrapper.eq("ItemId",itemId);
            cartMapper.update(cartDBItem, updateWrapper);
        }
    }

    //创建商品
    public void createItem(String account, String itemId, int quantity) {
        CartDBItem cartDBItem = new CartDBItem(account, itemId, quantity);
        cartMapper.insert(cartDBItem);
    }

    //删除商品
    public void deleteItem(String account, String itemId) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        queryWrapper.eq("ItemId",itemId);
        cartMapper.delete(queryWrapper);
    }

    //获取商品数量
    public int getItemQuantity(String account, String itemId) {
        QueryWrapper<CartDBItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LoginAccount",account);
        queryWrapper.eq("ItemId",itemId);
        CartDBItem cartDBItem = cartMapper.selectOne(queryWrapper);
        int quantity = 0;
        if (cartDBItem != null) {
            quantity = cartDBItem.getQuantity();
        }
        return quantity;
    }
}
