package org.csu.petstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sequence")
public class Sequence {
    private String name;
    @TableField("nextid")
    private int nextId;
    public Sequence() {}
    public Sequence(String name, int nextId) {
        this.name = name;
        this.nextId = nextId;
    }
}
