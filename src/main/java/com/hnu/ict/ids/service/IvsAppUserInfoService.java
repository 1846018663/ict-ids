package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.IvsAppUserInfo;

public interface IvsAppUserInfoService {

    int insert(IvsAppUserInfo ivsAppUserInfo);

    int updateById(IvsAppUserInfo ivsAppUserInfo);

    IvsAppUserInfo findNumber(String number);

}
