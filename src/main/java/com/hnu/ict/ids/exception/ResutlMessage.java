package com.hnu.ict.ids.exception;

import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;

public enum ResutlMessage {
    SUCCESS("200", "操作成功"),
    FAIL("300", "操作失败"),
    WARN("301", "非法操作"),
    ERROR("500", "操作失败");

    private String name;
    private String value;
    private ResutlMessage(String name, String value) {
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
