package com.hnu.ict.ids.exception;

public enum NetworkEnum {

    METHOD_POST("1001","POST"),
    METHOD_EGET("1002","GET"),
    TYPE_HTTP("2001","HTTP"),
    TYPE_HTTPS("2002","HTTPS"),
    STATUS_SUCCEED("3001","SUCCEED"),
    STATUS_FAILED("3002","FAILED"),
    ALGORITHM_INRERFACE_ADD("4001","新增行程算法接口"),
    ALGORITHM_INRERFACE_DEL("4002","取消已有行程算法接口"),
    ALGORITHM_INRERFACE_UP("4003","添加已有行程算法接口"),
    ALGORITHM_PLANNING("4004","路劲规划与距离算法接口"),
    ALGORITHM_SCHEDULE("4005","包车算法接口"),
    PASSENGRT_SERVICE_CALL_BACK("5001","乘客服务回调行程预约成功接口"),
    EALY_WARNING("6001","通知乘客服务系统预警通知");


    private String code;
    private String value;
    private NetworkEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getName() {
        return code;
    }

    public void setName(String name) {
        this.code = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
