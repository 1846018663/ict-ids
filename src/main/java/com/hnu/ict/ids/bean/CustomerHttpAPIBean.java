package com.hnu.ict.ids.bean;

import lombok.Data;

import java.util.List;

@Data
public class CustomerHttpAPIBean {
    String o_ids;
    String travel_id;
    double distance;
    Integer expected_time;
    String all_travel_plat;
    String driver_content;
    Integer c_id;
    Integer driver_id;
    Integer reservation_status;
    Integer it_number;
    Integer ret_status;
    List<TicketInfo> ticket_info;
    Integer waiting_space;
    String oper_time;

}
