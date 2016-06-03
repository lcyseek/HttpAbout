package com.example.lcy.asynchttpclient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static String BAIDU = "http://www.baidu.com/";
    private static String SINA = "http://www.sina.com.cn/";
    private static String BIYING="http://cn.bing.com/";
    private static String url = BAIDU;
    private static String GOOGLE = "http://www.google.com/";
    private static String LIUYAN = "http://gb.cri.cn/mmsource/images/2011/12/14/24/5004665712453389868.jpg";

    public static final String TAG = MainActivity.class.getSimpleName();
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = (ImageView) findViewById(R.id.iv);
    }

    public void get(View view) {
        AsyncHttpClient client = new AsyncHttpClient();

        //设置连接超时。如果网址为GOOGLE，三秒后就返回
        client.setConnectTimeout(3000);
        RequestParams params = new RequestParams();
        params.put("username","seek");
        params.put("userpwd","123456");

//        client.get(BAIDU, params,new TextHttpResponseHandler() {
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//
//            }
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, String responseString) {
//                System.out.println("--->"+responseString);
//            }
//        });


        client.get(GOOGLE, params,new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.i(TAG, "onStart: ");
                super.onStart();
            }

            //成功
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i(TAG, "onSuccess: statusCode="+statusCode);
                for(Header header:headers){
                    System.out.println(header.getName()+"|"+header.getValue());
                    try {
                        System.out.println(new String(responseBody,"utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }

            //失败
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "onFailure: statusCode="+statusCode);
                error.printStackTrace();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.i(TAG, "onRetry: ");
                super.onRetry(retryNo);
            }
            
        });

    }

    public void getImage(View view) {

        iv.setImageBitmap(null);

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(this, LIUYAN, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(responseBody,0,responseBody.length);
                iv.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i(TAG, "onFailure: ");
            }
        });
    }

    public void post(View view) throws FileNotFoundException {

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("username","seek");
        params.put("userpwd","123456");
        params.put("file", new File("test.txt")); // Upload a File

        client.post(this, BAIDU, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MainActivity.this,"Fail",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void syncHttp(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "syncHttp: start!");
                //同步请求.要运行在线程里.
                SyncHttpClient client = new SyncHttpClient();
                client.get(BAIDU, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess: "+new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.i(TAG, "onFailure: "+statusCode);
                    }
                });

                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "syncHttp: end!");
            }
        }).start();


    }
}
