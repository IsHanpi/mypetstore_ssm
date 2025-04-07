package org.csu.petstore.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.csu.petstore.entity.Account;
import org.csu.petstore.entity.SignOn;
import org.csu.petstore.persistence.BannerDataMapper;
import org.csu.petstore.service.AccountService;
import org.csu.petstore.vo.AccountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpSession session;
    @Autowired
    private BannerDataMapper bannerDataMapper;

    //登录界面
    @GetMapping("viewLoginForm")
    public String viewLoginForm() {
        return "account/login";
    }

    //登录
    @PostMapping("signon")
    public String login(String username, String password, Model model) {
        SignOn signon = accountService.getAccount(username,password);
        //验证码
        String value1 = request.getParameter("vCode");
        String value2 = (String) session.getAttribute("checkcode");
        boolean isEquals = value2.equalsIgnoreCase(value1);//不区分大小写
        if (!isEquals){
            model.addAttribute("signOnMsg","验证码错误");
            return "account/login";
        }
        if(signon != null){
            AccountVO accountVO = accountService.getAccount(username);
            session.setAttribute("account",accountVO);
            return "catalog/main";
        }
        else {
            model.addAttribute("signOnMsg","用户名或密码错误");
            return "account/login";
        }
    }

    //注销部分，有bug，sign out后无法直接sign in
    @GetMapping("signOut")
    public String signOut(Model model, HttpSession session) {
        AccountVO accountVO  = new AccountVO();
        session.setAttribute("account",accountVO);
        model.addAttribute("account",accountVO);
        return "catalog/main";
    }

    //注册界面
    @GetMapping("viewRegisterForm")
    public String viewRegisterForm(Model model, HttpSession session) {
        List<String> category = new ArrayList<>();
        category.add("FISH");
        category.add("DOGS");
        category.add("REPTILES");
        category.add("CATS");
        category.add("BIRDS");
        session.setAttribute("categories",category);
        return "account/register";
    }

    //注册
    @PostMapping("register")
    public String register(){
        session.getAttribute("account");
        session.setAttribute("account",null);

        String value1 = request.getParameter("vCode");
        String value2 = (String) session.getAttribute("checkcode");
        boolean isEquals = value2.equalsIgnoreCase(value1);

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String address1 = request.getParameter("address1");
        String address2 = request.getParameter("address2");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String zip = request.getParameter("zip");
        String country = request.getParameter("country");
        String languagePreference = request.getParameter("languagePreference");
        String favouriteCategoryId = request.getParameter("favouriteCategoryId");
        String listOption = request.getParameter("listOption");
        String bannerOption = request.getParameter("bannerOption");

        AccountVO accountVO = new AccountVO();
        accountVO.setUsername(username);
        accountVO.setPassword(password);
        accountVO.setEmail(email);
        accountVO.setPhone(phone);
        accountVO.setFirstName(firstName);
        accountVO.setLastName(lastName);
        accountVO.setAddress1(address1);
        accountVO.setAddress2(address2);
        accountVO.setCity(city);
        accountVO.setState("OK");
        accountVO.setZip(zip);
        accountVO.setCountry(country);
        accountVO.setLanguagePreference(languagePreference);
        accountVO.setFavouriteCategoryId(favouriteCategoryId);
        accountVO.setListOption(Boolean.parseBoolean(listOption));
        accountVO.setBannerOption(Boolean.parseBoolean(bannerOption));

        if(!accountService.hadAccount(username)){
            if(isEquals){
                accountService.insertAccount(accountVO);
                return "account/login";
            }else session.setAttribute("messageAccount","验证码错误");
        }
        return "account/register";
    }

    //编辑用户信息
    @GetMapping("editAccount")
    public String editAccount(HttpSession session, Model model) {
        List<String> languages = new ArrayList<String>();
        languages.add("english");
        languages.add("japanese");
        session.setAttribute("languages",languages);

        List<String> categories = new ArrayList<>();
        categories.add("FISH");
        categories.add("DOGS");
        categories.add("REPTILES");
        categories.add("CATS");
        categories.add("BIRDS");
        session.setAttribute("categories",categories);
        return "account/editAccount";
    }

    //确认修改的信息
    @PostMapping("confirmEdit")
    public String confirmEdit(Model model){
        AccountVO account1 = (AccountVO) session.getAttribute("account");
        if (account1 != null) {
            String password = request.getParameter("password");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address1 = request.getParameter("address1");
            String address2 = request.getParameter("address2");
            String city = request.getParameter("city");
            String state = request.getParameter("state");
            String zip = request.getParameter("zip");
            String country = request.getParameter("country");
            String languagePreference = request.getParameter("languagePreference");
            String favouriteCategoryId = request.getParameter("favouriteCategoryId");
            String listOption = request.getParameter("listOption");
            String bannerOption = request.getParameter("bannerOption");

            account1.setPassword(password);
            account1.setFirstName(firstName);
            account1.setLastName(lastName);
            account1.setEmail(email);
            account1.setPhone(phone);
            account1.setAddress1(address1);
            account1.setAddress2(address2);
            account1.setCity(city);
            account1.setState(state);
            account1.setZip(zip);
            account1.setStatus("OK");
            account1.setCountry(country);
            account1.setLanguagePreference(languagePreference);
            account1.setFavouriteCategoryId(favouriteCategoryId);
            account1.setListOption(Boolean.parseBoolean(listOption));
            account1.setBannerOption(Boolean.parseBoolean(bannerOption));

            String s = bannerDataMapper.selectById(favouriteCategoryId).getBannername();
            s = s.substring(0,12) + "../" +s.substring(12);
            account1.setBannerName(s);
            accountService.updateAccount(account1);
            session.setAttribute("account", account1);
            model.addAttribute("account", account1);
            return "catalog/main";
        }
        else  {
            session.setAttribute("ErrorMessage","请先登陆！");
            return "common/error";
        }
    }

    //判断用户名是否存在
    @GetMapping("usernameIsExist")
    public void usernameIsExist(@RequestParam("username") String username, HttpServletResponse response){
        if(username.isEmpty()){
            return;
        }

        AccountVO accountVO = accountService.getAccount(username);
        try{
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out = response.getWriter();
            response.setHeader("Cache-Control", "no-cache");
            out.println("<?xml version='1.0' encoding='"+"utf-8"+"' ?>");

            if(accountVO != null){
                out.println("<msg>Exist</msg>");
            }else out.println("<msg>Not Exist</msg>");
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //生成验证码
    @GetMapping("verification")
    public String verificationCode() throws IOException {
        response.setContentType("image/jpeg");
        HttpSession session = request.getSession();
        int width=60;
        int height=20;

        //设置浏览器不要缓存此图片
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //创建内存图像并获得图形上下文
        BufferedImage image=new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        Graphics g=image.getGraphics();

        /*
         * 产生随机验证码
         * 定义验证码的字符表
         */
        String chars="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] rands=new char[4];
        for(int i=0;i<4;i++){
            int rand=(int) (Math.random() *36);
            rands[i]=chars.charAt(rand);
        }

        /*
         * 产生图像
         * 画背景
         */
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);

        /*
         * 随机产生120个干扰点
         */

        for(int i=0;i<120;i++){
            int x=(int)(Math.random()*width);
            int y=(int)(Math.random()*height);
            int red=(int)(Math.random()*255);
            int green=(int)(Math.random()*255);
            int blue=(int)(Math.random()*255);
            g.setColor(new Color(red,green,blue));
            g.drawOval(x, y, 1, 0);
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font(null, Font.ITALIC|Font.BOLD,18));

        //在不同高度输出验证码的不同字符
        g.drawString(""+rands[0], 1, 17);
        g.drawString(""+rands[1], 16, 15);
        g.drawString(""+rands[2], 31, 18);
        g.drawString(""+rands[3], 46, 16);
        g.dispose();

        //将图像传到客户端
        ServletOutputStream sos=response.getOutputStream();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", baos);
        byte[] buffer=baos.toByteArray();
        response.setContentLength(buffer.length);
        sos.write(buffer);
        baos.close();
        sos.close();

        session.setAttribute("checkcode", new String(rands));
        return "account/login";
    }

    @GetMapping("viewRegister")
    public String viewRegister(){
        return "account/register";
    }

    @GetMapping("userSame")
    @ResponseBody
    public void userSame(String username) throws IOException{
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        if(accountService.hadAccount(username)){
            out.println("<msg>Exist</msg>");
        }else out.println("<msg>Not Exist</msg>");
        out.flush();
        out.close();
    }
}
