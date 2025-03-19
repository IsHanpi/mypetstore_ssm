package org.csu.petstore.controller;

import jakarta.servlet.http.HttpSession;
import org.csu.petstore.entity.Product;
import org.csu.petstore.service.CatalogService;
import org.csu.petstore.vo.CategoryVO;
import org.csu.petstore.vo.ItemVO;
import org.csu.petstore.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.List;

@Controller
@RequestMapping("/catalog")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("index")
    public String index() {
        return "catalog/main.html";
    }

    @GetMapping("viewCategory")
    public String viewCategory(String categoryId, Model model) {
        CategoryVO categoryVO = catalogService.getCategory(categoryId);
        model.addAttribute("category", categoryVO);
        return "catalog/category";
    }

    @GetMapping("viewProduct")
    public String viewProduct(String productId, Model model) {
        ProductVO productVO = catalogService.getProduct(productId);
        model.addAttribute("product", productVO);
        return "catalog/product";
    }

    @GetMapping("viewItem")
    public String viewItem(String itemId, Model model) {
        ItemVO itemVO = catalogService.getItem(itemId);
        model.addAttribute("item", itemVO);
        return "catalog/item";
    }

    @GetMapping("productName")
    @ResponseBody
    public String productName(String keyword) {
        List<Product> productList = catalogService.searchProductList(keyword);
        String result = JSON.toJSONString(productList);
        return result;
    }
    @PostMapping("searchProducts")
    public String searchProducts(String keyword, HttpSession session) {
        String url;
        if(!keyword.isEmpty()) {
            List<Product> productList = catalogService.searchProductList(keyword);
            if (!productList.isEmpty()) {
                session.setAttribute("productList", productList);
                url = "catalog/searchProducts";
            } else {
                session.setAttribute("ErrorMessage","未找到相关商品");
                url = "common/error";
            }
        }else {
            session.setAttribute("ErrorMessage","未找到相关商品");
            url = "common/error";
        }
        return url;
    }
}
