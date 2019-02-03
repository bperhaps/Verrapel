package com.example.user.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CameraRecorder extends Activity implements SurfaceHolder.Callback{
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    private Camera cam;
    private MediaRecorder mediaRecorder;

    private Button start;
    private SurfaceView sv;
    private SurfaceHolder sh;
    private SessionManager session;

    private long FileName=0;

    private void setting(){
        cam = Camera.open();
        cam.setDisplayOrientation(90);
        sv = (SurfaceView)findViewById(R.id.surfaceView);
        sh = sv.getHolder();
        sh.addCallback(this);
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_recorder);
        start = (Button) findViewById(R.id.Button1);
        start.setOnClickListener(captrureListener);
        session = new SessionManager(getApplicationContext());
        setting();
    }


    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (cam == null) {
                cam.setPreviewDisplay(holder);
                cam.startPreview();
            }
        } catch (IOException e) {
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        refreshCamera(cam);
    }
    public void refreshCamera(Camera camera) {
        if (sh.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            cam.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            cam.setPreviewDisplay(sh);
            cam.startPreview();
        } catch (Exception e) {
        }
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        cam = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // mCamera.release();

    }


    boolean recording = false;
    View.OnClickListener captrureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                start.setEnabled(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraRecorder.this, "succeed", Toast.LENGTH_LONG).show();
                        cam.takePicture(shutterCallback, rawCallback, jpegCallback);
                    }
                });

        }
    };


    //사진 메소드
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public static final String TAG ="test" ;

        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public static final String TAG = "tegst";

        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //이미지의 너비와 높이 결정
            int w = camera.getParameters().getPictureSize().width;
            int h = camera.getParameters().getPictureSize().height;

            int orientation = setCameraDisplayOrientation(CameraRecorder.this,
                    CAMERA_FACING, camera);

            //byte array를 bitmap으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);
            //int w = bitmap.getWidth();
            //int h = bitmap.getHeight();

            //이미지를 디바이스 방향으로 회전
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap =  Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

            //bitmap을 byte array로 변환
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] currentData = stream.toByteArray();

            //파일로 저장
            new SaveImageTask().execute(currentData);
            //Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        private static final String TAG = "test";

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/DCIM/Verrapel");
                boolean a = dir.mkdirs();
                FileName = System.currentTimeMillis();

                String fileName = String.format("%d.jpg", FileName);
                File outFile = new File(dir, fileName);

                Uploader.doImageUpload(data[0], fileName, session);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to "
                        + outFile.getAbsolutePath());

               // refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            setting();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/Verrapel/Video");
                dir.mkdirs();
                String fileName = String.format("%d.mp4", FileName);
                String FilePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + fileName;

                mediaRecorder = new MediaRecorder();
                cam.unlock();
                mediaRecorder.setCamera(cam);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //mediaRecorder.setAudioEncoder(3);
                //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
                mediaRecorder.setOrientationHint(90);

                mediaRecorder.setOutputFile(FilePath);
                mediaRecorder.setPreviewDisplay(sh.getSurface());
                mediaRecorder.prepare();
                mediaRecorder.start();
                mHandler.sendEmptyMessage(0);
                recording = true;
            } catch (final Exception ex) {
                ex.printStackTrace();
                mediaRecorder.release();
                return;
            }
        }

        private byte[] filetoByteArray(String path) {
            byte[] data;
            try {
                InputStream inputStream = new FileInputStream(path);
                int byteReads;
                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                while ((byteReads = inputStream.read()) != -1) {
                    output.write(byteReads);
                }

                data = output.toByteArray();
                output.close();
                inputStream.close();
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        int timer=0;
        @SuppressLint("HandlerLeak")
        Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                timer++;
                ProgressBar progress = (ProgressBar) findViewById(R.id.timerProgress) ;
                progress.setProgress(timer/25) ;
                // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
                mHandler.sendEmptyMessageDelayed(0,1);
                if(timer == 2500) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    cam.lock();
                    recording = false;
                    timer = 0;
                    start.setEnabled(true);
                    progress.setProgress(0) ;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File sdCard = Environment.getExternalStorageDirectory();
                            String fileName = String.format("%d.mp4", FileName);
                            String FilePath = sdCard.getAbsolutePath() + "/Verrapel/Video/" + fileName;
                            Uploader.doImageUpload(filetoByteArray(FilePath), fileName, session);
                        }
                    }).start();

                    mHandler.removeMessages(0);
                }
            }

        };

    }

}

