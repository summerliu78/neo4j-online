package com.ambergarden.samples.neo4j.util;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/7/25
 * Time: 14:03
 * To change this template use File | Settings | File Templates.
 */
public class HttpClients {


    public static String urlPost(String url, Map mParam) {
        //关闭
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");
        try {
            //POST的URL
            HttpPost httppost = new HttpPost(url);
            //建立HttpPost对象

            List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            //建立一个NameValuePair数组，用于存储欲传送的参数
             Iterator iterator  =mParam.entrySet().iterator();
             while(iterator.hasNext()){
                 Map.Entry  entry = (Map.Entry) iterator.next();
                 params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
             }
            //添加参数
            httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            //设置编码
            HttpResponse response = new DefaultHttpClient().execute(httppost);
            //发送Post,并返回一个HttpResponse对象
            if (response.getStatusLine().getStatusCode() == 200) {//如果状态码为200,就是正常返回
                String result = EntityUtils.toString(response.getEntity());
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    return "";
    }

 // 不带参数的post
    public static  String urlPostNoParam(String url ,String json){
        //关闭
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");
        PostMethod postMethod = new PostMethod(url);
        try {
            String str = "";
            String charSet = "utf-8";
            int soTimeOut = 100000;
            HttpClient client = new HttpClient();
            postMethod.setRequestEntity(new StringRequestEntity(json, "text/xml; charset=" + charSet, charSet));
            postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
            postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, soTimeOut);
            int statusCode = client.executeMethod(postMethod);
            BufferedReader in = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream(), charSet));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            if (statusCode != HttpStatus.SC_OK) {

               return "error";
            }
         return sb.toString();

        }catch (Exception e ){
         e.printStackTrace();
        }
        return  "";
    }

    public static String urlGet(String url, String param) {
        //关闭
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");
        HttpClient httpClient = new HttpClient();
        // 设置 Http 连接超时为5秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(50000);
        //step2： 创建GET方法的实例，类似于在浏览器地址栏输入url
        GetMethod getMethod = null;
        if (StringUtils.isBlank(param)) {
            getMethod = new GetMethod(url);
        } else {
            getMethod = new GetMethod(url + "?" + param);
        }
        // 设置 get 请求超时为 5 秒
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 50000);
        // 设置请求重试处理，用的是默认的重试处理：请求三次
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        try {
            //step3: 执行getMethod 类似于点击enter，让浏览器发出请求
            int statusCode = httpClient.executeMethod(getMethod);
        /*    if (statusCode != HttpStatus.SC_OK) {
                LogHelper.record.error("Method failed: " + getMethod.getStatusLine());
            }*/
            //step4: 读取内容,浏览器返回结果
            byte[] responseBody = getMethod.getResponseBody();
            //处理内容
            return new String(responseBody,"utf-8");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放连接 （一定要记住）
            getMethod.releaseConnection();
        }
        return "";
    }
}
