package app.camera.com.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ycl.chooseavatar.library.OnChoosePictureListener;
import com.ycl.chooseavatar.library.UpLoadHeadImageDialog;
import com.ycl.chooseavatar.library.YCLTools;

/**
 * desc  ${DESC}
 * author wangyongkui  wangyongkui@simuyun.com
 * 日期 2017/9/4-17:24
 */
public class CameraActivity extends Activity implements OnChoosePictureListener {
    ImageView select_camram;
    TextView up_camram;
    Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        select_camram = (ImageView) findViewById(R.id.select_camram);
        up_camram = (TextView) findViewById(R.id.up_camram);
        up_camram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpBitmap(System.currentTimeMillis() + ".jpg");
            }
        });
        select_camram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpLoadHeadImageDialog(CameraActivity.this).show();
            }
        });
        YCLTools.getInstance().setOnChoosePictureListener(this);
    }


    @Override
    public void OnChoose(String filePath) {
        bitmap = BitmapFactory.decodeFile(filePath);
        select_camram.setImageBitmap(bitmap);
    }

    @Override
    public void OnCancel() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        YCLTools.getInstance().upLoadImage(requestCode, resultCode, data);
    }

    private void UpBitmap(final String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String paths = DownloadUtils.postObject(bitmap, "face/", name);
                if (!TextUtils.isEmpty(paths)) {

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

    Handler upIvHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (100 == msg.what) {
                //上传成功后需要进行匹配校验
                final String ivurl = (String) msg.obj;
                Toast.makeText(CameraActivity.this, "图片上传成功：" + ivurl, Toast.LENGTH_LONG).show();

                //开始跳转
                CameraActivity.this.startActivity(new Intent(CameraActivity.this, MainActivity.class).putExtra("cameraurl", ivurl));
            }
            super.handleMessage(msg);

        }
    };
}
