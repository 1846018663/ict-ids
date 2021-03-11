package com.hnu.ict.ids.service;


import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;

import java.util.List;

public interface IvsAppPlatformInfoService {

    int findAllTotal();


    int findbyStatusTotal(String status);


    List<PlatformInfoFrom> getPlatformAll();
}
