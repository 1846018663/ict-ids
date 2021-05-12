package com.hnu.ict.ids.bean;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class UpdatePlatfromBean {
    @JSONField(ordinal=1)
    Integer p_id;//站点编号
    @JSONField(ordinal=2)
    String p_name;//车站名称
    @JSONField(ordinal=3)
    Integer p_type;//车站类型，1--大站台，2--中站台，3--小站台
    @JSONField(ordinal=4)
    Integer p_route_type;//站点路线类型，1-干线站，2-支线站，3-虚拟站，4-其他
    @JSONField(ordinal=5)
    String next_p_ids;//最近下一个站点ID，可填多个，英文逗号分隔
    @JSONField(ordinal=6)
    String next_p_distances;
    @JSONField(ordinal=7)
    String p_gps;
    @JSONField(ordinal=8)
    Double p_radius;

}
