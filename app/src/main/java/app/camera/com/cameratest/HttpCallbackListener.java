package app.camera.com.cameratest;

/**
 * desc  ${DESC}
 * author wangyongkui  wangyongkui@simuyun.com
 * 日期 2017/9/1-16:46
 */
public interface HttpCallbackListener {
    /**
     * 数据响应成功
     *
     * @param response
     *            返回数据
     */
    void onFinish(String response);

    /**
     * 数据请求失败
     *
     * @param e
     *            返回错误信息
     */
    void onError(Exception e);
}
