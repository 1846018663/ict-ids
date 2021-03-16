package com.hnu.ict.ids.bean;

import lombok.Data;

import java.util.List;

@Data
public class CustomerHttpAPIBean {
    Long o_id;
    String travel_id;
    double distance;
    int expected_time;
    String all_travel_plat;
    String driver_content;
    int c_id;
    int driver_id;
    int reservation_status;
    int it_number;
    int ret_status;
    List<TicketInfo> ticket_info;
    int waiting_space;
    String oper_time;

}
