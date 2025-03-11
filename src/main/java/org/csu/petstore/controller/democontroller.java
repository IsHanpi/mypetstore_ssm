package org.csu.petstore.controller;

import org.csu.petstore.entity.Category;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class democontroller {
    @RequestMapping("/demo")
    public Object demo(){
        return "demo1.html";
    }
    @GetMapping("demo1")
    public String demo1(Model model){
        model.addAttribute("message" , "你启动了吗");
        return "demo1";
    }
}
