package com.hnu.ict.ids.exception;

import com.hnu.ict.ids.utils.EnumUtil;

import java.util.Optional;

public enum ConfigEnum {

    CARTIMECONFIG("车辆分配时间设置","6001"),
    APPOINTMENT("预约响应时间设置","6002");


    private String name;
    private String value;
    private ConfigEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }



}
