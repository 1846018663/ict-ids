package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.DemoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DemoMapper extends BaseMapper<DemoEntity> {

    @Select("select * from sms_info where mobile = #{mobile}")
    DemoEntity getSmsByMobile(@Param("mobile") String mobile);
}
