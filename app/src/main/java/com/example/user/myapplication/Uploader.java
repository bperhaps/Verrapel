package com.example.user.myapplication;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.net.Proxy.Type.HTTP;

public class Uploader {

    private static byte[] bitmapToByteArray( Bitmap $bitmap ) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream() ;
        $bitmap.compress( Bitmap.CompressFormat.JPEG, 100, stream) ;
        byte[] byteArray = stream.toByteArray() ;
        return byteArray ;
    }
    /*
    * */
    public static String doImageCheck(final Bitmap $bitmap, SessionManager session) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap resizedBitmap = Bitmap.createBitmap($bitmap, 0, 0, $bitmap.getWidth(), $bitmap.getHeight(), matrix, true);
        byte[] data = bitmapToByteArray(resizedBitmap);
        String result="";
        try {

            URL url = new URL("http://localhost:3000/uploads/check");
            Log.i(TAG, "http://localhost:3000/uploads/check");
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            // open connection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true); //input 허용
            con.setDoOutput(true);  // output 허용
            con.setUseCaches(false);   // cache copy를 허용하지 않는다.
            con.setRequestMethod("POST");
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            con.setRequestProperty( "Cookie", session.getValue("cookie"));

            // write data
            DataOutputStream dos = new DataOutputStream(con.getOutputStream());
            Log.i(TAG, "Open OutputStream");
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            // 파일 전송시 파라메터명은 file1 파일명은 camera.jpg로 설정하여 전송
            dos.writeBytes("Content-Disposition: form-data; name=\"attachment\";filename=checkimage.jpg" +
                    lineEnd);


            dos.writeBytes(lineEnd);
            dos.write(data, 0, data.length);
            Log.i(TAG, data.length + "bytes written");
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            int responseStatusCode = con.getResponseCode();
            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;


            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            con.disconnect();

            result = sb.toString();

            Log.i("mstest", result);
            dos.flush(); // finish upload...

        } catch (Exception e) {
            e.getStackTrace();
        }
        Log.i(TAG, data.length + "bytes written successed ... finish!!");
        return result;
    }


    public static void doImageUpload(final byte[] $byteCode, final String fileName, final SessionManager session) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = $byteCode;
                try {
                    String result;
                    URL url = new URL("http://localhost:3000/uploads/single");
                    Log.i(TAG, "http://localhost:3000/uploads/single" );
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";

                    // open connection
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setDoInput(true); //input 허용
                    con.setDoOutput(true);  // output 허용
                    con.setUseCaches(false);   // cache copy를 허용하지 않는다.
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    con.setRequestProperty( "Cookie", session.getValue("cookie"));

                    // write data
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    Log.i(TAG, "Open OutputStream" );
                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    // 파일 전송시 파라메터명은 file1 파일명은 camera.jpg로 설정하여 전송
                    dos.writeBytes("Content-Disposition: form-data; name=\"attachment\";filename=" + fileName +
                            lineEnd);


                    dos.writeBytes(lineEnd);
                    dos.write(data,0,data.length);
                    Log.i(TAG, data.length+"bytes written" );
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    int responseStatusCode = con.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                        inputStream = con.getInputStream();
                    } else {
                        inputStream = con.getErrorStream();
                    }

                    dos.flush(); // finish upload...

                    File sdCard = Environment.getExternalStorageDirectory();
                    String FilePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + fileName;
                    File f = new File(FilePath);
                    if(f.exists()){
                        f.delete();
                    }


                } catch (Exception e) {
                    e.getStackTrace();
                }
                Log.i(TAG, data.length+"bytes written successed ... finish!!" );
            }
        }).start();
    }
}

