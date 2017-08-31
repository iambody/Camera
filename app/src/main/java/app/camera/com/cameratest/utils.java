package app.camera.com.cameratest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.Message;

/**
 * desc  ${DESC}
 * author wangyongkui  wangyongkui@simuyun.com
 * 日期 2017/8/31-14:02
 */
public class utils {
    private void findFace(final Bitmap bitmap, int postion, final Handler handler) {
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
                bmp = null;
                Message message = new Message();
                message.arg1 = (0 == faceCount ? 0 : 1);
                handler.sendMessage(message);
//                if (0 == faceCount) {
//
//                } else {
//                    return true;
////                    final Bitmap bitmaps = parseBitmap(bitmap, faces, faceCount);
////                    //显示处理后的图片
////                    mIv.post(new Runnable() {
////                        @Override
////                        public void run() {
////                            mIv.setImageBitmap(bitmap);
////                        }
////                    });
//                }
            }
        }).start();

    }

    /**
     * 在人脸上画矩形
     */
    private Bitmap parseBitmap(Bitmap mFaceBitmap, FaceDetector.Face[] faces, int faceCount) {
        Bitmap bitmap = Bitmap.createBitmap(mFaceBitmap.getWidth(), mFaceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);

        canvas.drawBitmap(mFaceBitmap, 0, 0, mPaint);
        for (int i = 0; i < faceCount; i++) {
            //双眼的中心点
            PointF midPoint = new PointF();
            faces[i].getMidPoint(midPoint);
            //双眼的距离
            float eyeDistance = faces[i].eyesDistance();
            //画矩形
            canvas.drawRect(midPoint.x - eyeDistance, midPoint.y - eyeDistance, midPoint.x + eyeDistance, midPoint.y + eyeDistance, mPaint);
        }

        return bitmap;
    }
}
