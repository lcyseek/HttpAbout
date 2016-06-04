package com.example.luchunyang.okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static String BAIDU = "http://www.baidu.com/";
    private static String SINA = "http://www.sina.com.cn/";
    private static String BIYING = "http://cn.bing.com/";
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
        //OkHttp官方文档并不建议我们创建多个OkHttpClient，因此全局使用一个。 如果有需要，可以使用clone方法，再进行自定义。
        OkHttpClient client = new OkHttpClient();

        Request.Builder builder = new Request.Builder();
        //header(name, value)可以设置唯一的name、value。如果已经有值，旧的将被移除，然后添加新的
        //使用addHeader(name, value)可以添加多值（添加，不移除已有的）。
        final Request request = builder.url(BAIDU).addHeader("name", "value").build();

        final Call call = client.newCall(request);

        //异步执行
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: thread name="+Thread.currentThread().getName());//子线程

                if(!response.isSuccessful()){
                    return ;
                }

                //返回码
                int code = response.code();
                Log.i(TAG, "onResponse: code="+code);

                //响应头
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.i(TAG, "onResponse: key="+headers.name(i)+" value="+headers.value(i));
                }

                //响应体
                ResponseBody body = response.body();
                System.out.println(response.body().string());

                //拿到具体的head某个值
                //Log.i(TAG, "onResponse: ---->"+response.header("Cxy_all"));
            }
        });


        //同步执行,需要自己开线程
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Response response = call.execute();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public void getImage(View view) {
        Request request = new Request.Builder().url(LIUYAN).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(2, TimeUnit.SECONDS).readTimeout(2,TimeUnit.SECONDS).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.isSuccessful()){
                    final byte[] bytes = response.body().bytes();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            iv.setImageBitmap(bitmap);
                        }
                    });
                }else{
                    Log.i(TAG, "onResponse: faile");
                }
            }

        });
    }



    private static final MediaType MEDIA_TYPE = MediaType.parse("text/x-markdown; charset=utf-8");

    //Post方式提交文件、String等
    public void post(View view) {
        OkHttpClient client = new OkHttpClient();

        //Posting a File
        RequestBody body = RequestBody.create(MEDIA_TYPE,new File(Environment.getExternalStorageDirectory()+"/"+"abc"));

        //Posting form parameters
//        FormBody.Builder formBody = new FormBody.Builder();
//        formBody.addEncoded("search","Jurassic Park");
//        RequestBody body1 = formBody.build();

        //Post Streaming
//        RequestBody bo = new RequestBody() {
//            @Override
//            public MediaType contentType() {
//                return MEDIA_TYPE;
//            }
//
//            @Override
//            public void writeTo(BufferedSink sink) throws IOException {
//                sink.writeUtf8("Numbers\n");
//                sink.writeUtf8("-------\n");
//                for (int i = 2; i <= 997; i++) {
//                    sink.writeUtf8(" "+i);
//                }
//            }
//        };

        Request request = new Request.Builder().url("https://api.github.com/markdown/raw").post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: code="+response.code());
            }
        });
    }
}
