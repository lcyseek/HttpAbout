package com.example.luchunyang.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;

/**
 * okhttp的简单使用:
 * 一般的get请求
 * 一般的post请求
 * 基于Http的文件上传
 * 文件下载
 * 加载图片
 * 支持请求回调，直接返回对象、对象集合
 * 支持session的保持
 */
public class OkHttpActivity extends AppCompatActivity {

    private Button button;
    private ImageView iv;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            iv.setImageBitmap((Bitmap) msg.obj);
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http);
        button = (Button) findViewById(R.id.btn);
        iv = (ImageView) findViewById(R.id.iv);
    }

    public void getString(View view) {

        OkHttpClient okHttpClient = new OkHttpClient();
        //https://publicobject.com/helloworld.txt
        //使用header(name, value)可以设置唯一的name、value。如果已经有值，旧的将被移除，然后添加新的。
        //使用addHeader(name, value)可以添加多值（添加，不移除已有的）。
        Request request = new Request.Builder().url("http://publicobject.com/helloworld.txt")
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5").tag("tag").build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.out.println("onFailure");
            }

            //一般情况下，比如我们希望获得返回的字符串，可以通过response.body().string()获取；
            //如果希望获得返回的二进制字节数组，则调用response.body().bytes()；
            // 如果你想拿到返回的inputStream，则调用response.body().byteStream()
            @Override
            public void onResponse(Response response) throws IOException {

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    System.out.println(headers.name(i) + ": " + headers.value(i));
                }

                //响应体的 string() 方法对于小文档来说十分方便、高效。但是如果响应体太大（超过1MB），应避免适应 string() 方法 ，因为他会将把整个文档加载到内存中。
                //对于超过1MB的响应body，应使用流的方式来处理body。
                System.out.println("Thread Name:" + Thread.currentThread().getName());
                System.out.println(response.body().string());
                try {
                    //因为是线程，所以不能更新UI
                    button.setText("设置");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

//        call.cancel();
        //使用Call.cancel()可以立即停止掉一个正在执行的call。如果一个线程正在写请求或者读响应，将会引发IOException。当call没有必要的时候，使用这个api可以节约网络资源。例如当用户离开一个应用时。不管同步还是异步的call都可以取消。
        //你可以通过tags来同时取消多个请求。当你构建一请求时，使用RequestBuilder.tag(tag)来分配一个标签。之后你就可以用OkHttpClient.cancel(tag)来取消所有带有这个tag的call。all.cancel();

    }

    public void getImage(View view) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url("http://img2.fengniao.com/product/94/224/ceacecfpc9yb.jpg").build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Headers headers = response.headers();
                    for (int i = 0; i < headers.size(); i++) {
                        System.out.println(headers.name(i) + ": " + headers.value(i));
                    }


                    byte[] bytes = response.body().bytes();
                    System.out.println("len-->" + bytes.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Message message = Message.obtain();
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Call.execute()该不会开启异步线程。
    public void getWait(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url("http://publicobject.com/helloworld.txt").build();
                    Call call = okHttpClient.newCall(request);
                    System.out.println("--->before");
                    Response response = call.execute();//大概几秒
                    System.out.println("--->after " + response.body().string());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final String json = ""
            + "Releases\n"
            + "--------\n"
            + "\n"
            + " * _1.0_ May 6, 2013\n"
            + " * _1.1_ June 15, 2013\n"
            + " * _1.2_ August 11, 2013\n";

    public void put(View view) {

        try {

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setConnectTimeout(3, TimeUnit.SECONDS);
            okHttpClient.setWriteTimeout(3, TimeUnit.SECONDS);
            okHttpClient.setReadTimeout(3, TimeUnit.SECONDS);
            RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MARKDOWN, json);
            Request request = new Request.Builder().url("https://api.github.com/markdown/raw").post(requestBody).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Headers headers = response.headers();
                    for (int i = 0; i < headers.size(); i++) {
                        System.out.println(headers.name(i) + ": " + headers.value(i));
                    }

                    System.out.println(response.body().string());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Post方式提交流
    public void putByStream(View view) {

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MEDIA_TYPE_MARKDOWN;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }

            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };


        Request request = new Request.Builder().url("https://api.github.com/markdown/raw").post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                System.out.println(response.body().string());
            }
        });
    }

    public void putFile(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    File file = new File("README.md");
                    Request request = new Request.Builder().url("https://api.github.com/markdown/raw")
                            .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file)).build();
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static final String IMGUR_CLIENT_ID = "...";
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public void putPart(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(3,TimeUnit.SECONDS);
                    client.setReadTimeout(3,TimeUnit.SECONDS);
                    RequestBody requestBody = new MultipartBuilder()
                            .type(MultipartBuilder.FORM)
                            .addPart(
                                    Headers.of("Content-Disposition", "form-data; name=\"title\""),
                                    RequestBody.create(null, "Square Logo"))
                            .addPart(
                                    Headers.of("Content-Disposition", "form-data; name=\"image\""),
                                    RequestBody.create(MEDIA_TYPE_PNG, new File("website/static/logo-square.png")))
                            .build();

                    Request request = new Request.Builder().header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                            .url("https://api.imgur.com/3/image")
                            .post(requestBody).build();


                    Response response = client.newCall(request).execute();
                    Headers headers = response.headers();

                    for (int i = 0; i < headers.size(); i++) {
                        System.out.println(headers.name(i) + ": " + headers.value(i));
                    }
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
