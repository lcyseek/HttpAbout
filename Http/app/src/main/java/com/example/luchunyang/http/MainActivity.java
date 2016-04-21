package com.example.luchunyang.http;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/* HTTP请求，而基本上目前有两个实现方式：HttpUrlConnection（Java原生API）和Apache的HttpClient。
* 普通JAVA人员可选用HttpClient，安卓开发人员则应该使用HttpUrlConnection，理由如下：
* 1.HttpClient是apache的开源实现，而HttpUrlConnection是安卓标准实现，安卓SDK虽然集成了HttpClient，但官方支持的却是HttpUrlConnection；
* 2.HttpUrlConnection直接支持GZIP压缩；HttpClient也支持，但要自己写代码处理；我们之前测试HttpUrlConnection的GZIP压缩在传大文件分包trunk时有问题，只适合小文件，不过这个BUG后来官方说已经修复了；
* 3.HttpUrlConnection直接支持系统级连接池，即打开的连接不会直接关闭，在一段时间内所有程序可共用；HttpClient当然也能做到，但毕竟不如官方直接系统底层支持好；
* HttpUrlConnection直接在系统层面做了缓存策略处理，加快重复请求的速度。
* 在Android 2.2版本之前，HttpClient拥有较少的bug，因此使用它是最好的选择。
* 而在Android 2.3版本及以后，HttpURLConnection则是最佳的选择。它的API简单，体积较小，因而非常适用于Android项目
*
* okhttp   compile 'com.squareup.okhttp:okhttp:2.4.0'
* Android-Async-Http compile 'com.loopj.android:android-async-http:1.4.9'
*/

public class MainActivity extends AppCompatActivity {

    private EditText et_result;
    private ImageView iv;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                et_result.setText((String) msg.obj);
            } else if (msg.what == 2) {
                byte[] data = (byte[]) msg.obj;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                iv.setImageBitmap(bitmap);
            } else if (msg.what == 3) {
                iv.setImageBitmap((Bitmap) msg.obj);
            }

            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_result = (EditText) findViewById(R.id.et_result);
        iv = (ImageView) findViewById(R.id.iv);
    }

    public void getText(View view) throws IOException {
        new Thread(new GetTextRunnable()).start();
    }

    public void getImage(View view) {
        new Thread(new GetImageRunnable()).start();
    }

    public void someTest(View view) {
//        new Thread(new SomeTest()).start();
        new Thread(new SomeTest1()).start();
    }


    public void getMp3(View view) {
        new Thread(new GetMp3Runnable()).start();
    }

    public void post(View view) {
        new Thread(new PostRunnable()).start();
    }

    public void okHttpActivity(View view) {
        startActivity(new Intent(this,OkHttpActivity.class));
    }


    class SomeTest1 implements Runnable {

        @Override
        public void run() {

            try {
                URL url = new URL("http://www.baidu.com/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

//                String len = connection.getHeaderField("Content-Type");
                Map<String, List<String>> heads = connection.getHeaderFields();

                for (String key : heads.keySet()) {
                    System.out.println(key + "  " + heads.get(key));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class SomeTest implements Runnable {

        @Override
        public void run() {
            try {
                URL url = new URL("http://www.b1a1i1du.com/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //设置连接主机超时（单位：毫秒）.这个时间只是指建立socket的时间，而并不是指发送数据以及数据传输的时间。所以在一般的连接处理中，这个时间已经是非常地长了
                connection.setConnectTimeout(3000);
                //设置从主机读取数据超时（单位：毫秒）
                connection.setReadTimeout(3000);

                System.out.println("---->1");
                //错误的url会在此发生错误
                int retCode = connection.getResponseCode();
                System.out.println("---->2");

                System.out.println("retcode:" + retCode);
                if (retCode >= 300) {
                    throw new Exception("HTTP Request is not success, Response code is " + retCode);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PostRunnable implements Runnable {

        @Override
        public void run() {
            try {
                URL url = new URL("http://www.baidu.com/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("Charset", "UTF-8");

                String param = "name=" + URLEncoder.encode("丁丁", "UTF-8");
                byte[] bytes = param.getBytes();
                connection.getOutputStream().write(bytes);

                int retCode = connection.getResponseCode();
                if (retCode == HttpURLConnection.HTTP_OK) {
                    StringBuffer sb = new StringBuffer();
                    String readLine;
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    while ((readLine = responseReader.readLine()) != null) {
                        sb.append(readLine).append("\n");
                    }
                    responseReader.close();
                    System.out.println(sb.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class GetMp3Runnable implements Runnable{

        @Override
        public void run() {
            try {
                URL url = new URL("http://mobilecdn.kugou.com/api/v3/search/song?keyword=" + URLEncoder.encode("心太软","utf-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("contentType","utf-8");

                String data = null;

                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String line;
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                in.close();

                System.out.println("data|"+data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class GetTextRunnable implements Runnable {

        @Override
        public void run() {
            try {
                URL url = new URL("http://www.baidu.com/");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Charset", "utf-8");

                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(isr);
                String line;
                final StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                Message message = Message.obtain();
                message.what = 1;
                message.obj = buffer.toString();
                handler.sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class GetImageRunnable implements Runnable {

        @Override
        public void run() {
            try {
                URL url = new URL("http://www.dianyingo.com/d/file/gupiao/2016-03-24/6f464822033ffbcba9b458251e50c752.png");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();

                ////可以直接使用InputStream
                //Bitmap bitmap = BitmapFactory.decodeStream(is);

                byte[] data = new byte[1024];
                int len = 0;
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                while ((len = is.read(data)) != -1) {
                    b.write(data, 0, len);
                }

                Message message = Message.obtain();
                message.what = 2;
                message.obj = b.toByteArray();
                handler.sendMessage(message);


//                Message message = Message.obtain();
//                message.what = 3;
//                message.obj = bitmap;
//                handler.sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

