package com.hnu.ict.ids.bean;


import lombok.Data;

import java.util.List;

@Data
public class SeatBean {
    private String travel_id;
    private Integer car_id;
    private String correspond_order_id;
    private String correspond_order_number;
    private List<SeatUserBean> order_u_id;
    private Integer it_number;
    private List<SeatPreference> user_preference;


}
