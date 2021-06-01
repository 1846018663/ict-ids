package com.hnu.ict.ids.bean;

import lombok.Data;
import org.apache.ibatis.annotations.Delete;

@Data
public class TraveStatisticsBean {
    Integer total;
    Integer servenTotal;
    Integer monthTotal;

}
