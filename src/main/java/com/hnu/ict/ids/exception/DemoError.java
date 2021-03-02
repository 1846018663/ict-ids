package com.hnu.ict.ids.exception;

import com.hnu.common.exception.error.IError;

public enum DemoError implements IError {

    // 车辆不存在
    VALID_CODE_NOT_EXIT("00001", "VALID_CODE NOT EXIT"),
    //手机号码为空
    MOBILE_IS_NULL("00002", "MOBILE IS NULL"),

    ;

    String errorCode;
    String errorMessage;

    private static final String NS = "IDS";

    DemoError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


    @Override
    public String getErrorCode() {
        return NS + "." + errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
