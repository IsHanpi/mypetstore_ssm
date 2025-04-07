package org.csu.petstore.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.csu.petstore.entity.Order;
import org.csu.petstore.service.impl.CartServiceImpl;
import org.csu.petstore.service.impl.OrderServiceImpl;
import org.csu.petstore.vo.AccountVO;
import org.csu.petstore.vo.CartItemVO;
import org.csu.petstore.vo.CartVO;
import org.csu.petstore.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private CartServiceImpl cartService;

    @PostMapping("confirmOrder")
    public String confirmOrder() {
        String shippingAddressRequired = request.getParameter("shippingAddressRequired");
        OrderVO order = (OrderVO) session.getAttribute("order");
        if (request.getParameter("cartType") != null) order.setCardType(request.getParameter("cartType"));
        if (request.getParameter("creditCard") != null) order.setCreditCard(request.getParameter("creditCard"));
        if (request.getParameter("expiryDate") != null) order.setExpiryDate(request.getParameter("expiryDate"));
        if (request.getParameter("billToFirstName") != null)
            order.setBillToFirstName(request.getParameter("billToFirstName"));
        if (request.getParameter("billToLastName") != null)
            order.setBillToLastName(request.getParameter("billToLastName"));
        if (request.getParameter("billAddress1") != null) order.setBillAddress1(request.getParameter("billAddress1"));
        if (request.getParameter("billAddress2") != null) order.setBillAddress2(request.getParameter("billAddress2"));
        if (request.getParameter("billCity") != null) order.setBillCity(request.getParameter("billCity"));
        if (request.getParameter("billState") != null) order.setBillState(request.getParameter("billState"));
        if (request.getParameter("billZip") != null) order.setBillZip(request.getParameter("billZip"));
        if (request.getParameter("billCountry") != null) order.setBillCountry(request.getParameter("billCountry"));
        if (shippingAddressRequired != null) {
            String shipToFirstName = request.getParameter("shipToFirstName");
            String shipToLastName = request.getParameter("shipToLastName");
            String shipAddress1 = request.getParameter("shipAddress1");
            String shipAddress2 = request.getParameter("shipAddress2");
            String shipCity = request.getParameter("shipCity");
            String shipState = request.getParameter("shipState");
            String shipZip = request.getParameter("shipZip");
            String shipCountry = request.getParameter("shipCountry");

            order.setShipToFirstName(shipToFirstName);
            order.setShipToLastName(shipToLastName);
            order.setShipAddress1(shipAddress1);
            order.setShipAddress2(shipAddress2);
            order.setShipCity(shipCity);
            order.setShipState(shipState);
            order.setShipZip(shipZip);
            order.setCourier(shipCountry);

            session.setAttribute("order", order);
        }
        return "order/confirm";
    }

    @GetMapping("newOrder")
    public String newOrder() {
        AccountVO account = (AccountVO) session.getAttribute("account");
        CartVO cart = (CartVO) session.getAttribute("cart");
        String url = "order/newOrder";
        if (account == null) {
            session.setAttribute("message", "You must sign on before attempting to check out.  Please sign on and try checking out again.");
            url = "account/signon";
        } else if (cart != null) {
            OrderVO order = new OrderVO();
            orderService.setOrder(order);
            orderService.initOrder(account, cart);
            session.setAttribute("order", order);
        } else {
            session.setAttribute("ErrorMessage", "An order could not be created because a cart could not be found.");
            url = "common/error";
        }
        return url;
    }

    @GetMapping("viewOrder")
    public String viewOrder() {
        AccountVO account = (AccountVO) session.getAttribute("account");
        CartVO cartVO = (CartVO) session.getAttribute("cart");
        OrderVO orderVO = (OrderVO) session.getAttribute("order");
        String url = "order/viewOrder";
        if (orderVO != null) {
            cartService.setCart(cartVO);
            orderService.insertOrder(orderVO);
            session.setAttribute("order", orderVO);
            List<CartItemVO> cartItemList = cartService.getCartItems(account.getUsername());
            BigDecimal subTotal = cartVO.getSubTotal();
            //清空购物车
            cartVO = null;
            cartService.deleteAllCartItems(account.getUsername());
            session.setAttribute("cart", cartVO);

            session.setAttribute("cartItemList", cartItemList);
            session.setAttribute("subTotal", subTotal);
            session.setAttribute("message", "Thank you, your order has been submitted.");
        } else {
            session.setAttribute("ErrorMessage", "An error occurred processing your order (order was null).");
            url = "common/error";
        }
        return url;
    }

    @GetMapping("listOrder")
    public String listOrder(Model model) {
        AccountVO accountVO = (AccountVO) session.getAttribute("account");
        List<Order> list = orderService.getOrdersByUsername(accountVO.getUsername());
        List<OrderVO> orderList = new ArrayList<>();
        for (Order order : list) {
            OrderVO orderVO = new OrderVO();
            orderVO.setOrderId(order.getOrderId());
            orderVO.setOrderDate(order.getOrderDate());
            orderVO.setTotalPrice(order.getTotalPrice());
            orderVO.setStatus(orderService.getStatusByOrderId(order.getOrderId()));
            orderList.add(orderVO);
        }
        model.addAttribute("orderList",orderList);
        return "order/listOrder";
    }

    @GetMapping("checkOrder")
    public String checkOrder(String orderId){
        String url = "order/checkOrder";
        if (!orderId.isEmpty()) {
            OrderVO order;
            order = orderService.getOrder(Integer.parseInt(orderId));
            session.setAttribute("order", order);
        } else {
            session.setAttribute("ErrorMessage", "订单历史记录为空.");
            url = "common/error";
        }
        return url;
    }
}
