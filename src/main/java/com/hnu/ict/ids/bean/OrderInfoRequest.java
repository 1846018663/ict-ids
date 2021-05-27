package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class OrderInfoRequest {
   String oId;
   String  fromId;
   String toId;
   String startTime;
   String orderTime;
   Integer ticketNumber;
   Integer setTime;


}
