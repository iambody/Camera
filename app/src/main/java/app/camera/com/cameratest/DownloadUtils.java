package app.camera.com.cameratest;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * desc  ${DESC}
 * author wangyongkui  wangyongkui@simuyun.com
 * 日期 2017/9/1-14:25
 */
public class DownloadUtils {

    public interface RequestCallBack {
        void onRequestStart();

        void onRequestProgress(String progress);

        void onRequstFinished();

        void onRequstFailure();
    }

    private static String accessKeyId = "p1LT6HCasmsOyPHG";
    private static String signature = "EoN77P/dvoy5qPfXNabiVHB4nK8=";
    private static String encodePolicy = "eyJleHBpcmF0aW9uIjoiMjAyMC0wMS0wMVQxMjowMDowMC4wMDBaIiwiY29uZGl0aW9ucyI6W1siY29udGVudC1sZW5ndGgtcmFuZ2UiLDAsNTI0Mjg4MDAwXV19";

    // 提交表单的URL为bucket域名
    public static String urlStr = "https://upload.simuyun.com/";

    public static String postObject(  Bitmap bitmap, String type, String filename) {
        // 表单域
        Map<String, String> textMap = new LinkedHashMap<>();
        String remotePath = type.concat(UUID.randomUUID().toString().concat(".png"));
        textMap.put("key", remotePath);
        // Content-Disposition
        textMap.put("Content-Disposition", "attachment;filename=" + filename);
        // OSSAccessKeyId
        textMap.put("OSSAccessKeyId", accessKeyId);
        // policy
        textMap.put("policy", encodePolicy);
        // Signature
        textMap.put("Signature", signature);
//        Map<String, String> fileMap = new HashMap<String, String>();
//        fileMap.put("file", localFilePath);
        if (formUpload(urlStr, textMap, bitmap, filename)) {
            return remotePath;
        }
        return null;
    }


    private static boolean formUpload(String urlStr, Map<String, String> textMap,
                                      Bitmap bitmap, String filename) {
        String res = "";
        HttpURLConnection conn = null;
        String boundary = String.valueOf(System.currentTimeMillis());
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Map.Entry<String, String>> iter = textMap.entrySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    if (i == 0) {
                        strBuf.append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    } else {
                        strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    }
                    i++;
                }
                out.write(strBuf.toString().getBytes());
            }


            String contentType = "application/octet-stream";
            StringBuffer strBuf1 = new StringBuffer();
            strBuf1.append("\r\n").append("--").append(boundary).append(
                    "\r\n");
            strBuf1.append("Content-Disposition: form-data; name=\""
                    + "file" + "\"; filename=\"" + filename
                    + "\"\r\n");
            strBuf1.append("Content-Type: " + contentType + "\r\n\r\n");


            out.write(strBuf1.toString().getBytes());
            out.write(BitmapToByte(bitmap));


            StringBuffer strBufn = new StringBuffer();
            out.write(strBufn.toString().getBytes());


            byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();

            int resCode = conn.getResponseCode();
            if (resCode == 204) {
                return true;
            }
            System.out.println("--------post1---res=" + resCode);

            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
        } catch (Exception e) {
            System.err.println("Send post request exception: " + e);
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return false;
    }

    /**
     * bitmap转换成byte流
     *
     * @param bitmap
     * @return
     */
    public static byte[] BitmapToByte(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] bitmapdata = stream.toByteArray();
        return bitmapdata;

    }

    private static boolean formUpload(String urlStr, Map<String, String> textMap,
                                      Map<String, String> fileMap) {
        String res = "";
        HttpURLConnection conn = null;
        String boundary = String.valueOf(System.currentTimeMillis());
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Map.Entry<String, String>> iter = textMap.entrySet().iterator();
                int i = 0;
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    if (i == 0) {
                        strBuf.append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    } else {
                        strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    }
                    i++;
                }
                out.write(strBuf.toString().getBytes());
            }

            if (fileMap != null) {
                Iterator<Map.Entry<String, String>> iter = fileMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();

                    String contentType = "application/octet-stream";
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(boundary).append(
                            "\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\""
                            + inputName + "\"; filename=\"" + filename
                            + "\"\r\n");
                    strBuf.append("Content-Type: " + contentType + "\r\n\r\n");


                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                }
                StringBuffer strBuf = new StringBuffer();
                out.write(strBuf.toString().getBytes());
            }

            byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();

            int resCode = conn.getResponseCode();
            if (resCode == 204) {
                return true;
            }
            System.out.println("--------post1---res=" + resCode);

            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
        } catch (Exception e) {
            System.err.println("Send post request exception: " + e);
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }
        return false;
    }

}
