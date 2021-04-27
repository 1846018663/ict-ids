package com.hnu.ict.ids.bean;

import lombok.Data;
import org.apache.ibatis.annotations.Delete;

@Data
public class TravelStatisticsBean {
    Integer total;
    Integer servenTotal;
    Integer monthTotal;

}
