package com.example.user.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.video.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class VideoDownloader extends Thread{
    private static FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();

    PictureDetector p;
    Bitmap bmp;
    SessionManager session;
    public VideoDownloader(Bitmap bmp, PictureDetector p, SessionManager session){
        this.p = p;
        this.bmp = bmp;
        this.session = session;
    }


    @Override
    public void run(){

             // String fileName = downloadURL.substring(downloadURL.lastIndexOf("article/") + 8, downloadURL.length());

            String datas = Uploader.doImageCheck(bmp, session);
            if (datas != "NULL" && datas != "") {

                String fileURL="";
                int lastnum=1;
                String filename="";
                String ext="";

                JSONObject job = null;
                try {
                    job = new JSONObject(datas);
                    fileURL = (String) job.get("fileDir");
                    lastnum = Integer.parseInt((String) job.get("lastnum"));
                    filename = (String) job.get("filename");
                    ext = (String) job.get("ext");
                } catch (JSONException e) {
                    return;
                }

            final File sdCard = Environment.getExternalStorageDirectory();

            File dir = new File(sdCard.getAbsolutePath() + "/Verrapel/Video/" + filename);

            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (int i = 1; i <= lastnum; i++) {
                String filePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + filename + "/" + i + ext;
                File f = new File(filePath);
                if (!f.exists()) {
                    String fileUrl = fileURL + "/" + i + ext;

                    String localPath = filePath;

                    try {
                        URL imgUrl = new URL(fileUrl);
                        //서버와 접속하는 클라이언트 객체 생성
                        HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                        int response = conn.getResponseCode();

                        File file = new File(localPath);

                        InputStream is = conn.getInputStream();
                        OutputStream outStream = new FileOutputStream(file);

                        byte[] buf = new byte[1024];
                        int len = 0;

                        while ((len = is.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }

                        outStream.close();
                        is.close();
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            ArrayList<Mat> videoArray = new ArrayList<Mat>();

            for (int i = 1; i <= lastnum; i++) {
                String filePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + filename + "/" + i + ext;

                File file = new File(filePath);
                if (file.exists()) {
                    Bitmap bm = BitmapFactory.decodeFile(filePath);
                    Mat tmp = new Mat();

                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    Utils.bitmapToMat(resizedBitmap, tmp);
                    videoArray.add(tmp);
                /*
                mmr.setDataSource(filePath);
                String time = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                int VideoDuration = Integer.parseInt(time) * 1000;// This will give time in millesecond
                for (int ftime = 0; ftime < VideoDuration; ftime += 120000) {
                    Bitmap b = mmr.getFrameAtTime(ftime, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
                    byte[] artwork = mmr.getEmbeddedPicture();
                    ftime = (ftime + 120000) % VideoDuration;

                    Mat result = new Mat();
                    Utils.bitmapToMat(b, result);
                    videoArray.add(result);
                }
                //p.setDownloadFinish(true);

                */
                }
            }
            p.setVideoArray(videoArray);
        }
    }



    /*
    private static ArrayList<Mat> writeFile(InputStream is, OutputStream os, String filePath) throws IOException
    {
        ArrayList<Mat> videoArray = new ArrayList<Mat>();
        int c = 0;
        while((c = is.read()) != -1)
            os.write(c);
        os.flush();

        File filePre = new File(filePath+".tmp");
        File fileNow = new File(filePath);
        filePre.renameTo(fileNow);

        mmr.setDataSource(filePath);
        String time = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
        int VideoDuration = Integer.parseInt(time) * 1000;// This will give time in millesecond
        for(int ftime=0; ftime < VideoDuration; ftime+=120000) {
            Bitmap b = mmr.getFrameAtTime(ftime, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
            byte[] artwork = mmr.getEmbeddedPicture();
            ftime = (ftime + 120000) % VideoDuration;

            Mat result = new Mat();
            Utils.bitmapToMat(b, result);
            videoArray.add(result);
        }
        return videoArray;
    }
    static void download(final String DownloadURL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = new URL(DownloadURL).openStream();
                    String fileName = DownloadURL.substring(DownloadURL.lastIndexOf("article/") + 8, DownloadURL.length());

                    final File sdCard = Environment.getExternalStorageDirectory();
                    String filePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + fileName;
                    File dir = new File(sdCard.getAbsolutePath() + "/Verrapel/Video/");
                    boolean a = dir.mkdirs();

                    File file = new File(filePath);
                    OutputStream out = null;
                    out = new FileOutputStream(file);

                    writeFile(inputStream, out, filePath);
                    out.close();
                } catch (Exception e){
                    e.getStackTrace();
                }
            }
        }).start();
    }
    */
}
