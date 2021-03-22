package com.hnu.ict.ids.control;


import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.ConfigTimeBean;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.utils.EnumUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Api(tags = "修改配置API")
@RestController
@RequestMapping("/config")
public class ConfigControl {

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 设置配置时间接口
     */
    @RequestMapping(value="/setTimeConfig" ,method = RequestMethod.POST)
    @ParamsNotNull(str ="typeId,dateTime")
    public PojoBaseResponse setTimeConfig(String typeId,int dateTime){
        PojoBaseResponse result=new PojoBaseResponse();
        Map<String,Integer> resultMap= redisTemplate.opsForHash().entries("setTimeConfig");
        String name=getEnumvalue(typeId);
        resultMap.put(typeId,dateTime);
        redisTemplate.opsForHash().putAll("setTimeConfig",resultMap);

        return result;
    }


    /**
     * 获取当前时间配置信息
     */
    @RequestMapping(value="/getTimeConfig" ,method = RequestMethod.GET)
    public PojoBaseResponse getTimeConfig(){
        PojoBaseResponse result=new PojoBaseResponse();
        Map<String,Object> resultMap= redisTemplate.opsForHash().entries("setTimeConfig");
        List<ConfigTimeBean> list=new ArrayList<>();

        for(String key:resultMap.keySet()){
            ConfigTimeBean bean=new ConfigTimeBean();
            String  name=getEnumvalue(key);
            bean.setCode(key);
            bean.setName(name);
            bean.setResult(Integer.parseInt(resultMap.get(key).toString()));
            list.add(bean);
        }


        result.setData(list);
        return result;
    }







    public static String getEnumvalue(String value) {
        Optional<ConfigEnum> m1 = EnumUtil.getEnumObject(ConfigEnum.class, e -> e.getValue().equals(value));
        return m1.isPresent() ? m1.get().getName() : null;
    }
}
