package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("profile")
public class Profile {
    @TableId
    private String userid;
    private String langpref;
    private String favcategory;
    private int mylistopt;
    private int banneropt;
}
