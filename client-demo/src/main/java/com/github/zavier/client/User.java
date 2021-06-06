package com.github.zavier.client;

import com.github.zavier.annotation.ClassDoc;
import com.github.zavier.annotation.FieldDoc;

@ClassDoc(desc = "用户")
public class User {
    @FieldDoc(desc = "姓名")
    private String name;

    @FieldDoc(desc = "年龄")
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
