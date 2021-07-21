package com.hnu.ict.ids.exception;

public enum CarTypeEnum {

    SZ("深圳",1001),
    SH("上海",1003);


    private String name;
    private int value;
    private CarTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }



}
