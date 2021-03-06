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
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
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
        new Thread(new GetTextRunnable()).start();
    }

    public void getImage(View view) {
        new GetImageTask().execute(LIUYAN);
    }

    public void post(View view) {
        new Thread(new PostRunnable()).start();
    }

    public void getMp3(View view) {
        new Thread(new GetMp3Runnable()).start();
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

                HttpEntity entity = new UrlEncodedFormEntity(list,"utf-8");
                FileEntity fileEntity = new FileEntity(new File("somefile.txt"),"text/plain; charset=\"UTF-8\"");

                /************************************************/

                HttpPost post = new HttpPost("http://www.baidu.com");

                //设置请求体内容
                post.setEntity(entity);
//                post.setEntity(fileEntity);

                HttpResponse response = client.execute(post);
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    System.out.println("post 成功");
                }

                post.abort();


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class GetMp3Runnable implements Runnable{

        @Override
        public void run() {
            try {
                HttpClient client = new DefaultHttpClient();

                ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
                NameValuePair pair = new BasicNameValuePair("keyword","心太软");
                list.add(pair);

                String url = "http://mobilecdn.kugou.com/api/v3/search/song"+"?"+URLEncodedUtils.format(list,"utf-8");

                HttpGet get = new HttpGet(url);
                HttpResponse response = client.execute(get);
                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    HttpEntity entity = response.getEntity();
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

    class GetTextRunnable implements Runnable{
        @Override
        public void run() {

            try {

                //得到HttpClient对象
                HttpClient client = new DefaultHttpClient();

                /*******************提交带参数的get请求.************/
                ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
                NameValuePair pair = new BasicNameValuePair("key","value");
                NameValuePair pair1 = new BasicNameValuePair("key1","value1");
                list.add(pair);
                list.add(pair1);


                //URLEncodedUtils 和 URLEncoder 的区别 : URLEncoder是java的，只能把一个字符串转码。
                //而URLEncodedUtils是HttpClient的，可以把一个ArrayList<NameValuePair>一起转化,比较方便
                String url = "http://www.baidu.com" + "?" + URLEncodedUtils.format(list,"utf-8");
                //System.out.println(url);//http://www.baidu.com?key=value&key1=value1
                /************************************************/


                HttpGet requst = new HttpGet(BAIDU);

                //设置请求头
                requst.setHeader("Connection","Keep-Alive");
                requst.setHeader("Accept-Charset","GB2312,utf-8;q=0.7,*;q=0.7");


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
