package com.hnu.ict.ids.exception;

public enum PushStatusEnum {



    SUCCESS("回调乘客服务系统结果成功",1),
    FAIL("回调乘客服务系统结果失败",2);


    private String name;
    private Integer value;
    private PushStatusEnum(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }



}
