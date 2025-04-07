package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("account")
public class Account {
    @TableId("userid")
    private String username;
    private String email;
    @TableField("firstname")
    private String firstName;
    @TableField("lastname")
    private String lastName;
    private String status;
    @TableField(value = "addr1")
    private String address1;
    @TableField(value = "addr2")
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
}
