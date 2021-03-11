package com.hnu.ict.ids.service;


import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;

import java.util.List;

public interface IvsAppPlatformInfoService {

    int findAllTotal(String cityCode);


    int findbyStatusTotal(String status,String cityCode);


    List<PlatformInfoFrom> getPlatformAll(String cityCode);

    IvsAppPlatformInfo getByPlatformId(int platforId);
}
