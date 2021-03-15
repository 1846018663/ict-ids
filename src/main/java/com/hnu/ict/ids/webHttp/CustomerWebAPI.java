package com.hnu.ict.ids.webHttp;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class CustomerWebAPI {

   static Logger logger=  LoggerFactory.getLogger(CustomerWebAPI.class);

    /**
     * dopost请求
     * @param url
     * @param msgEn
     * @return
     */
    public String doPost(String url, String msgEn) throws Exception {
        URL urlServlet = null;
        BufferedReader reader = null;
        StringBuffer lines = new StringBuffer();
        try {
            urlServlet = new URL(url);
            HttpURLConnection urlConnect = (HttpURLConnection) urlServlet
                    .openConnection();
                // 设置连接参数
            urlConnect.setRequestMethod("POST");
            urlConnect.setDoInput(true);
            urlConnect.setDoOutput(true);
            urlConnect.setRequestProperty("Content-type", "application/json");
            urlConnect.setAllowUserInteraction(true);

            // 开启流，写入流数据
            if (null != msgEn || "".equals(msgEn)) {
                OutputStream output = urlConnect.getOutputStream();
                output.write(msgEn.getBytes("UTF-8"));
                output.flush();
                output.close();
            }
            // 获取返回输出流
            InputStream input = urlConnect.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input,"GBK"));
            String strMesg = "";
            while ((strMesg = reader.readLine()) != null) {
                lines.append(strMesg);
            }
        } catch (IOException e) {
            throw new Exception("网络请求失败:" + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new Exception("网络请求关闭流失败:" + e.getMessage());
                }
            }
        }
        return lines.toString();
    }


    public String doHttpsPost(String url, String strData) throws Exception {
        URL uploadServlet = null;
        BufferedReader reader = null;
        StringBuffer lines = new StringBuffer();
        try {
            TrustManager[] tm = { new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            } };
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            // 初始化
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 获取SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");

            uploadServlet = new URL(url);
            HttpsURLConnection servletConnection = (HttpsURLConnection) uploadServlet.openConnection();//在此获取https连接
            // 设置连接参数
            servletConnection.setRequestMethod("POST");
            servletConnection.setDoOutput(true);
            servletConnection.setDoInput(true);
            servletConnection.setUseCaches(false);
            servletConnection.setRequestProperty("Content-type", "application/json");
            servletConnection.setAllowUserInteraction(true);
            servletConnection.setConnectTimeout(10000);
            servletConnection.setReadTimeout(10000);
            // 设置当前实例使用的SSLSoctetFactory
            servletConnection.setSSLSocketFactory(ssf);
            servletConnection.connect();


            // 开启流，写入数据
            if(strData!=null && !"".equals(strData)){
                OutputStream output = servletConnection.getOutputStream();
                output.write(strData.getBytes("UTF-8"));
                output.flush();
                output.close();
            }

            // 获取返回的数据
            InputStream inputStream = servletConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
        } catch (Exception e) {
            throw new Exception("电子合同签约失败:" + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new Exception("电子合同开户关闭流失败:" + e.getMessage());
                }
            }
        }
        return lines.toString();
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


    }
}
