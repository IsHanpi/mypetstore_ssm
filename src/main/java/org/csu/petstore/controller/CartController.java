package org.csu.petstore.controller;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpSession;
import org.csu.petstore.persistence.InventoryMapper;
import org.csu.petstore.persistence.ItemMapper;
import org.csu.petstore.service.impl.CartServiceImpl;
import org.csu.petstore.vo.CartItemVO;
import org.csu.petstore.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartServiceImpl cartService;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
//    @Autowired
//    private AccountMapper accountMapper;
    @Autowired
    HttpSession session;

    @GetMapping("viewCart")
    public String viewCart(){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        if(cartVO == null){
            cartVO = new CartVO();
            cartVO.setCartItems(new ArrayList<CartItemVO>());
            cartVO.setSubTotal(BigDecimal.valueOf(0));
            cartVO.setNumOfItems(0);
        }
        return "cart/cart";
    }

    @GetMapping("AddItemToCart")
    public String AddItemToCart(@RequestParam(name = "workingItemId") String workingItemId){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        String url = "cart/cart";
        if(cartVO == null){
            cartVO = new CartVO();
            cartVO.setCartItems(new ArrayList<CartItemVO>());
        }
//        Account account = (Account) session.getAttribute("account");
//        if(account != null){
//
//        }
        return url;
    }

    @GetMapping("removeItem")
    public String removeItem(@RequestParam(name = "workingItemId") String workingItemId){
        CartVO cartVO  =(CartVO) session.getAttribute("cart");
        String url = "cart/cart";
//        Account account = (Account) session.getAttribute("account");
//        if(account != null){
//
//        }
        return url;
    }

    @PostMapping("updateCart")
    @ResponseBody
    public JSONObject updateCart(@RequestParam("itemId") String itemId,@RequestParam("quantityString") String quantityString){
        CartVO cartVO =(CartVO) session.getAttribute("cart");
        Account account = (Account) session.getAttribute("account");
        JSONObject jsonObject = new JSONObject();
        if(cartVO != null){
            cartService.setCart(cartVO);
            DecimalFormat df = new DecimalFormat("$#,##0.00");
            jsonObject.put("total", df.format(updateCart((cartVO,account,itemId,quantityString))));
            jsonObject.put("subTotal",df.format(cartService.getSubTotal()));
        }
        return jsonObject;
    }
    protected BigDecimal updateCart(CartVO cartVO,Account account,String itemId,String quantityString){
        int quantity = 0;
        if(!quantityString.equals("")){
            quantity = Integer.parseInt(quantityString);
        }
        cartService.setQuantityByItemId(account.getUsername(),itemId,quantity);
        BigDecimal total = cartService.getTotal(itemId);
        if(quantity < 1){
            cartService.incrementQuantityByItemId(account.getUsername(),itemId);
        }
        return total;
    }
}
