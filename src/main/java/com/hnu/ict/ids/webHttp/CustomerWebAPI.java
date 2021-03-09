package com.hnu.ict.ids.webHttp;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class CustomerWebAPI {

   static Logger logger=  LoggerFactory.getLogger(CustomerWebAPI.class);

    public static String doPost(JSONObject date) {
        RestTemplate restTemplate=new RestTemplate();
        ResponseEntity<String> responseEntity=restTemplate.postForEntity("https://applets.dxzhcl.com/order/updateorder",date,String.class); //提交的body内容为user对象，请求的返回的body类型为String
        String body=responseEntity.getBody();
        return body;
    }

    public static void main(String[] args) {
        JSONObject json=new JSONObject();
        json.put("o_id","16125739316761591");
        json.put("travel_id","2021020609130137877");
        json.put("distance",4200.0);
        json.put("expected_time",10);
        json.put("all_travel_plat","1401,1402,1403,1404,1405,1406");
        json.put("driver_content","请司机师傅于20210206094500前往【光电园】接上车乘客，再开往目的地【康庄】 ");
        json.put("c_id",22);
        json.put("driver_id",69);
        json.put("reservation_status",1);
        json.put("it_number",1);
        json.put("ret_status",0);
        JSONObject jn=new JSONObject();
        jn.put("u_id",64);
        jn.put("seat_number",1);
        json.put("ticket_info",jn);
        json.put("oper_time",69);
        json.put("driver_id",1615270536);




        logger.info(CustomerWebAPI.doPost(json));
    }
}
