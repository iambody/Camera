package app.camera.com.cameratest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera camera;
    SurfaceView surfaceView;
    Window window;
    //    ImageView imageView;
    LinearLayout hsh;
    List<Bitmap> bitmaps = new ArrayList<>();
    String identityUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        hsh = (LinearLayout) findViewById(R.id.hsh);
        identityUrl = getIntent().getStringExtra("cameraurl");

        InitSurface();

    }

    private void InitSurface() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceView.getHolder().addCallback(this);

    }

    private void InitCamera() {
        //CameraID表示0或者1，表示是前置摄像头还是后置摄像头
        camera = Camera.open(1);
//        camera.setOneShotPreviewCallback(this);
        getPreViewImage();
        Camera.Parameters parameters = camera.getParameters();
        //开启闪光灯
        // parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//        强制竖屏
        camera.setDisplayOrientation(90);
//        Display display  = wm.getDefaultDisplay();//得到当前屏幕

        parameters.setPictureSize(100, 100);
        parameters.setPictureFormat(PixelFormat.JPEG);//设置照片的格式
        parameters.setJpegQuality(85);//设置照片的质量

        //设置放大倍数
        parameters.setZoom(1);
        camera.setParameters(parameters);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        InitCamera();
        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        handle.sendEmptyMessage(BUFFERTAG);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void rotateMyBitmap(Bitmap bmp) {

        //*****旋转一下
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);

        Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

//*******显示一下
        bitmaps.add(nbmp2);
//        imageView.setImageBitmap(nbmp2);
        //***************
        ImageView image = new ImageView(MainActivity.this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(500, 500);
        p.setMargins(10, 10, 10, 10);
        image.setLayoutParams(p);
        image.setImageBitmap(nbmp2);
        //****************
        hsh.addView(image);
        findFace(nbmp2, bitmaps.size(), testHandler);

    }


    void getPreViewImage() {
        camera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                //生成 bitmap
                Camera.Size size = camera.getParameters().getPreviewSize();
                try {
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

                        //**********************
                        //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                        rotateMyBitmap(bmp);
                        //**********************************

                        stream.close();
                    }
                } catch (Exception ex) {
                    Log.e("Sys", "Error:" + ex.getMessage());
                }
                Log.i("camera444444", "previewFrame调用");
            }
        });

    }

    final static int BUFFERTAG = 1;
    final static int BUFFERTAG1 = 0;

    Handler handle = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case BUFFERTAG:
//                    if (isGetBuffer) {
                    getPreViewImage();
//                        btnGetBuffer.setText("开始图片1");
                    handle.sendEmptyMessageDelayed(BUFFERTAG, 3000);
//
//                    } else {
//                        camera.setPreviewCallback(null);
//                    }
                    break;
                case BUFFERTAG1:
                    camera.setPreviewCallback(null);
//                    handler.sendEmptyMessageDelayed(BUFFERTAG, 5000);
                    break;


            }

        }

        ;
    };


    private void findFace(final Bitmap bitmap, final int postion, final Handler handler) {
        if (null == bitmap) return;
        final int MAX_FACES = 1;
        //因为这是一个耗时的操作，所以放到另一个线程中运行
        new Thread(new Runnable() {
            @Override
            public void run() {
                FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACES];
                //格式必须为RGB_565才可以识别
                Bitmap bmp = bitmap.copy(Bitmap.Config.RGB_565, true);
                //返回识别的人脸数
                int faceCount = new FaceDetector(bmp.getWidth(), bmp.getHeight(), MAX_FACES).findFaces(bmp, faces);
                bmp.recycle();
                Message message = new Message();
                message.what = faceCount;
                message.arg1 = postion;
                message.obj = bitmap;
                handler.sendMessage(message);

            }


        }).start();

    }

    Handler testHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int postion = msg.arg1;
            Bitmap bitmap = (Bitmap) msg.obj;
            switch (msg.what) {
                case 0:
                    Toast.makeText(MainActivity.this, postion + "请对准摄像头！！！", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(MainActivity.this, postion + "已经对准摄像头！！！", Toast.LENGTH_LONG).show();


                    uploadIv(bitmap, postion + "postion.jpg");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 开始上传帧图片
     */
    public void uploadIv(final Bitmap bitmap, final String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String paths = DownloadUtils.postObject(bitmap, "face/", name);
                if (!TextUtils.isEmpty(paths)) {
                    Log.i("isiissis", name + "上传成功" + paths);
                    Message message = new Message();
                    message.what = 100;
                    message.obj = DownloadUtils.urlStr + paths;
                    upIvHandler.sendMessage(message);
                } else {
                    Log.i("isiissis", name + "上传失败");
                }


            }
        }.start();
    }

    String host = "https://d9-app.simuyun.com/auth/v2/ocr/facecompare?param=";

    public String getHost(String Ivurl) {
        JSONObject objects = new JSONObject();
        try {
            objects.put("faceUrl1", identityUrl);
            objects.put("faceUrl2", Ivurl);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return host + objects.toString();
    }

    Handler upIvHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (100 == msg.what) {
                //上传成功后需要进行匹配校验
                final String ivurl = (String) msg.obj;
                Log.i("isiissis", "请求匹配url****************************" + getHost(ivurl));

                RequestParams entity = new RequestParams(getHost(ivurl));
                //数据请求，这里先要设置回到的call接口对象,数据在接口对象的方法中获取
                x.http().get(entity, new org.xutils.common.Callback.CacheCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONObject objresult = obj.getJSONObject("result");
                            if ("1".equals(objresult.getString("ismatch"))) {//匹配成功
                                try {
                                    Toast.makeText(MainActivity.this, "人脸匹配成功;匹配度为：" + String.valueOf(objresult.getString("similarity")), Toast.LENGTH_LONG).show();
                                    MainActivity.this.startActivity(new Intent(MainActivity.this, ResultAc.class));

                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "人脸匹配成功", Toast.LENGTH_LONG).show();
                                    MainActivity.this.startActivity(new Intent(MainActivity.this, ResultAc.class));
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "人脸匹配失败", Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {

                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }

                    @Override
                    public boolean onCache(String result) {
                        return false;
                    }
                });


            }
            super.handleMessage(msg);

        }
    };

    /**
     * 成功需要弹出显示
     */
    Handler PipeiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (101 == msg.what) {
                String OsTRING = (String) msg.obj;
                try {
                    JSONObject obj = new JSONObject(OsTRING);
                    JSONObject objresult = obj.getJSONObject("result");
                    if ("1".equals(objresult.getString("comparePass"))) {//匹配成功
                        Toast.makeText(MainActivity.this, "人脸匹配成功，你是林功尧", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "人脸匹配失败，你不是林功尧！", Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            super.handleMessage(msg);
        }
    };
}
