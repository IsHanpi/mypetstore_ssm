package org.csu.petstore.controller;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpSession;
import org.csu.petstore.entity.Account;
import org.csu.petstore.entity.Item;
import org.csu.petstore.entity.Product;
import org.csu.petstore.persistence.AccountMapper;
import org.csu.petstore.persistence.InventoryMapper;
import org.csu.petstore.persistence.ItemMapper;
import org.csu.petstore.service.CatalogService;
import org.csu.petstore.service.impl.CartServiceImpl;
import org.csu.petstore.vo.AccountVO;
import org.csu.petstore.vo.CartItemVO;
import org.csu.petstore.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartServiceImpl cartService;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private CatalogService catalogService;
    @Autowired
    HttpSession session;

    //购物车页面
    @GetMapping("viewCart")
    public String viewCart(){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        AccountVO account = (AccountVO) session.getAttribute("account");
        if(cartVO == null){
            cartVO = new CartVO();
            cartVO.setCartItems(new ArrayList<CartItemVO>());
            cartVO.setSubTotal(BigDecimal.valueOf(0));
            cartVO.setNumOfItems(0);
        }

        cartService.setCart(cartVO);
        cartService.loadCartFromDB(account.getUsername());

        AccountVO accountVO = (AccountVO) session.getAttribute("account");
        if(accountVO != null && accountVO.isListOption()){
            List<Product> list = catalogService.getProductList(accountVO.getFavouriteCategoryId());
            session.setAttribute("myList", list);
        }
        session.setAttribute("cart", cartVO);
        return "cart/cart";
    }

    @GetMapping("addItemToCart")
    public String AddItemToCart(@RequestParam(name = "workingItemId") String workingItemId){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        String url = "cart/cart";

        if(cartVO == null){
            cartVO = new CartVO();
            cartVO.setCartItems(new ArrayList<CartItemVO>());
        }

        Account account = (Account) session.getAttribute("account");
        if(account != null){
            cartService.setCart(cartVO);
            if(cartService.containsItemId(account.getUsername(),workingItemId)){
                cartService.incrementQuantityByItemId(account.getUsername(),workingItemId);
            }else {
                boolean isInStock = inventoryMapper.selectById(workingItemId).getQuantity() > 0;
                Item item = itemMapper.selectById(workingItemId);
                cartService.addItem(account.getUsername(),item,isInStock);
            }
            session.setAttribute("cart", cartVO);
        }else {
            session.setAttribute("ErrorMessage", "请先登录");
            url = "common/error";
        }
        return url;
    }

    @GetMapping("removeItem")
    public String removeItem(@RequestParam(name = "workingItemId") String workingItemId){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        String url = "cart/cart";
        AccountVO account = (AccountVO) session.getAttribute("account");
        if(account != null){
            if(cartVO != null){
                cartService.setCart(cartVO);
                cartService.removeItem(account.getUsername(),workingItemId);
            }else {
                session.setAttribute("ErrorMessage","购物车为空");
                url = "common/error";
            }
        }else {
            session.setAttribute("ErrorMessage", "请先登录");
            url = "common/error";
        }
        return url;
    }

    @PostMapping("updateCart")
    @ResponseBody
    public JSONObject updateCart(@RequestParam("itemId") String itemId,@RequestParam("quantityString") String quantityString){
        CartVO cart =(CartVO) session.getAttribute("cart");
        AccountVO account = (AccountVO) session.getAttribute("account");
        JSONObject jsonObject = new JSONObject();
        if(cart != null){
            cartService.setCart(cart);
            DecimalFormat df = new DecimalFormat("$#,##0.00");
            jsonObject.put("total", df.format(updateCart(account,itemId,quantityString)));
            jsonObject.put("subTotal",df.format(cartService.getSubTotal()));
        }
        return jsonObject;
    }

    protected BigDecimal updateCart(AccountVO account,String itemId,String quantityString){
        int quantity = 0;
        if(!quantityString.isEmpty()){
            quantity = Integer.parseInt(quantityString);
        }
        cartService.setQuantityByItemId(account.getUsername(),itemId,quantity);

        BigDecimal total = cartService.getTotal(itemId);
        if(quantity < 1){
            cartService.removeItem(account.getUsername(),itemId);
        }
        return total;
    }
}
