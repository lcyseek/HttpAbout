package com.example.lcy.httpclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static String BAIDU = "http://www.baidu.com/";
    private static String SINA = "http://www.sina.com.cn/";
    private static String BIYING="http://cn.bing.com/";
    private static String LIUYAN = "http://gb.cri.cn/mmsource/images/2011/12/14/24/5004665712453389868.jpg";
    private Bitmap bitmap;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);
    }

    public void get(View view) throws IOException {
        new Thread(new GetRunnable()).start();
    }

    public void getImage(View view) {
        new GetImageTask().execute(LIUYAN);
    }

    public void post(View view) {
        new Thread(new PostRunnable()).start();
    }

    class PostRunnable implements Runnable{

        @Override
        public void run() {
            try{

                HttpClient client = new DefaultHttpClient();

                /*******************提交带参数的get请求.************/
                ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
                NameValuePair pair = new BasicNameValuePair("key","value");
                NameValuePair pair1 = new BasicNameValuePair("key1","value1");
                list.add(pair);
                list.add(pair1);

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"utf-8");

                /************************************************/

                HttpPost post = new HttpPost("http://www.baidu.com");
                //设置请求体内容
                post.setEntity(entity);

                HttpResponse response = client.execute(post);
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    System.out.println("post 成功");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class GetRunnable implements Runnable{

        @Override
        public void run() {

            try {

                //得到HttpClient对象
                HttpClient client = new DefaultHttpClient();

                HttpGet requst = new HttpGet(BAIDU);


                /*******************提交带参数的get请求.************/
                ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
                NameValuePair pair = new BasicNameValuePair("key","value");
                NameValuePair pair1 = new BasicNameValuePair("key1","value1");
                list.add(pair);
                list.add(pair1);

                String url = "http://www.baidu.com" + "?" + URLEncodedUtils.format(list,"utf-8");
                /************************************************/


                //客户端使用GET方式执行请教，获得服务器端的回应response
                HttpResponse response = client.execute(requst);

                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    //获取响应头
                    Header [] headers = response.getAllHeaders();
                    for (int i = 0; i < headers.length; i++) {
                        System.out.println(headers[i].getName()+"|"+headers[i].getValue());
                    }

                    //获取服务器响应内容
                    HttpEntity entity = response.getEntity();
                    //System.out.println(EntityUtils.toString(entity));

                    InputStream is = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    String data = "";
                    String line;
                    while((line = reader.readLine()) != null){
                        data += line;
                    }

                    is.close();
                    reader.close();

                    System.out.println(data);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    class GetImageTask extends AsyncTask<String,Object,Object>{

        @Override
        protected Object doInBackground(String... params) {
            try {
                //子线程
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse response = client.execute(get);


                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    InputStream is = response.getEntity().getContent();
                    bitmap = BitmapFactory.decodeStream(is);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            iv.setImageBitmap(bitmap);
        }
    }
}
