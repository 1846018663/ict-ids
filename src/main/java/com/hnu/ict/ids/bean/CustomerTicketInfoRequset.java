package com.hnu.ict.ids.bean;


import lombok.Data;

import java.util.List;

@Data
public class CustomerTicketInfoRequset {

    List<Tickets> tickets;
    String o_id;




}
