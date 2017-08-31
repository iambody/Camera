package app.camera.com.cameratest;

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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        hsh = (LinearLayout) findViewById(R.id.hsh);


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
        findFace(nbmp2,bitmaps.size(),testHandler);

    }

    boolean isPreview;


//    class MyAdapter extends PagerAdapter {
//        List<Bitmap> bitmaps = new ArrayList<>();
//
//        public MyAdapter(List<Bitmap> bitmaps) {
//            this.bitmaps = bitmaps;
//        }
//
//        @Override
//        public int getCount() {
//            return bitmaps.size();
//        }
//
//        public void freash(Bitmap data) {
//            bitmaps.add(data);
//            this.notifyDataSetChanged();
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view==object;
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            ImageView image=new ImageView(MainActivity.this);
//
//            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(100,100);
//            p.setMargins(20,20,20,20);
//            image.setLayoutParams(p);
//            image.setImageBitmap(bitmaps.get(position));
//            container.addView(image);
//            return image;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
////            super.destroyItem(container, position, object);
//            container.removeView((View) object);
//        }
//
//    }

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
                Message message=new Message();
                message.what=faceCount;
                message.arg1=postion;
                handler.sendMessage(message);

            }


        }).start();

    }

Handler testHandler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
     int postion=   msg.arg1;
        switch (msg.what){
            case 0:
                Toast.makeText(MainActivity.this, postion+"请对准摄像头！！！", Toast.LENGTH_LONG).show();
                break;
            case 1:
                Toast.makeText(MainActivity.this, postion+"已经对准摄像头！！！", Toast.LENGTH_LONG).show();
                break;
        }
        super.handleMessage(msg);
    }
};
    //    @Override
//    public void onPreviewFrame(byte[] data, Camera camera) {
////生成 bitmap
//        Camera.Size size = camera.getParameters().getPreviewSize();
//        try {
//            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
//            if (image != null) {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
//
//                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//
//                //**********************
//                //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
//                rotateMyBitmap(bmp);
//                //**********************************
//
//                stream.close();
//            }
//        } catch (Exception ex) {
//            Log.e("Sys", "Error:" + ex.getMessage());
//        }
//        Log.i("camera444444", "previewFrame调用");
//    }

}
