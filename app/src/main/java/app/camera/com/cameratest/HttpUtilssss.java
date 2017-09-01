package app.camera.com.cameratest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * desc  ${DESC}
 * author wangyongkui  wangyongkui@simuyun.com
 * 日期 2017/9/1-16:45
 */
public class HttpUtilssss {
    /**
     * 发送Get请求
     *
     * @param address
     *            请求数据地址
     * @param listener
     *            耗时回调监听
     */
    public static void sendGetHttpRequest(final String address,
                                          final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    // 创建一个URL对象并传入地址
                    URL url = new URL(address);
                    // 用地址打开连接通道
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方式为get
//                    connection.setRequestMethod("GET");
                    // 设置连接超时为8秒
                    connection.setConnectTimeout(8000);
                    // 设置读取超时为8秒
                    connection.setReadTimeout(8000);
                    // 设置可取
                    connection.setDoInput(true);
                    // 设置可读
                    connection.setDoOutput(true);
                    // 得到输入流
                    InputStream in = connection.getInputStream();
                    // 创建高效流对象
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    // 创建StringBuilder对象存储数据
                    StringBuilder response = new StringBuilder();
                    String line;// 一次读取一行
                    while ((line = reader.readLine()) != null) {
                        response.append(line);// 得到的数据存入StringBuilder
                    }
                    if (listener != null) {// 如果监听被回调
                        // 回调onFinish()方法
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        // 回调onError()方法
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {// 通道不为null
                        connection.disconnect();// 关闭通道
                    }
                }
            }
        }).start();
    }
}
