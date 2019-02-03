package com.example.user.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import org.json.JSONException;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Rect2d;

import java.util.ArrayList;

public class PictureDetector extends Activity
        implements CameraBridgeViewBase.CvCameraViewListener2 {
    //변수셋팅

    private SessionManager session;

    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private ImageView objectImgView;
    private Mat Loading = new Mat();
    private Bitmap bitmapobj;


    private int doSelect = 0;
    private boolean trackerInit = false;
    Rect2d bbox = new Rect2d(0,0,0,0);


    //JNI 함수 선언
    public native boolean Square(long matAddrInput, long matAddrResult);
    //public native boolean Tracking(long matAddrInput, long matAddrResult, Rect2d rect, boolean trackerInit);
    public native void getImageFromCamera(long matAddrInput, long matResultAddr);
    public native void drawOnCamera(long matAddrInput, long matResultAddr, long objectMat);




    //라이브러리 로드
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //창 예시
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.picture_detector);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        //  objectImgView = (ImageView)findViewById(R.id.objectImg);
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        session = new SessionManager(getApplicationContext());
        read_image_file();


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }
    @Override
    public void onCameraViewStopped() {

    }
    ArrayList<Mat> videoArray = new ArrayList<Mat>();
    private long firstDetectStartTime;
    private long detectStartTime;
    private long videoStartTime;
    private long nowTime;

    private boolean firstDetectChecker = true;
    private boolean firstSquareCheckr = true;
    private boolean videoPlayFirst = true;

    private boolean videoFunc = false;
    private boolean downloadFinish = false;
    int videoCnt = 0;

    public void setDownloadFinish(boolean downloadFinish) {
        this.downloadFinish = downloadFinish;
    }

    public void setVideoArray(ArrayList<Mat> videoArray){
        this.videoArray = videoArray;
    }

    VideoDownloader videoDownloader;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();
        if ( matResult != null ) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
/*
        File sdCard = Environment.getExternalStorageDirectory();
        String path = sdCard.getAbsolutePath() + "/Verrapel/Video/1528138203924.mp4";
        //File dir = new File ();

        mmr.setDataSource(path);
        Bitmap b = mmr.getFrameAtTime(test, FFmpegMediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
        byte [] artwork = mmr.getEmbeddedPicture();
        test += 120000;
        Utils.bitmapToMat(b, matResult);
        */

        if(Square(matInput.getNativeObjAddr(), matResult.getNativeObjAddr()))
        {
            if (firstDetectChecker) {
                firstDetectStartTime = System.currentTimeMillis();
                firstDetectChecker = false;
            }

            detectStartTime = System.currentTimeMillis();
            long steady = (System.currentTimeMillis() - firstDetectStartTime);
            if( steady > 1500) {
                if (firstSquareCheckr) {
                    videoArray.clear();
                    Mat image = new Mat();
                    getImageFromCamera(matInput.getNativeObjAddr(), image.getNativeObjAddr());
                    Bitmap bmp;
                    firstSquareCheckr = false;
                    videoFunc = true;
                    try {

                        bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(image, bmp);
                        //String downloadUrl = Uploader.doImageCheck(bmp);
                        videoDownloader = new VideoDownloader(bmp, this, session);
                        videoDownloader.start();
                             // a.doInBackground(downloadUrl);
                    } catch (CvException e) {
                        Log.d("Exception", e.getMessage());
                    }

                } else {
                    long time = System.currentTimeMillis() - videoStartTime;
                    if( videoArray.size() != 0 && time > 100) {
                        videoStartTime = System.currentTimeMillis();
                        videoCnt = videoCnt % videoArray.size();
                        Mat frame = videoArray.get(videoCnt);
                        drawOnCamera(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), frame.getNativeObjAddr());
                        videoCnt++;
                    } else {
                        if(videoDownloader.isAlive() && videoArray.size() == 0)
                            drawOnCamera(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), Loading.getNativeObjAddr());
                        else {
                            if(videoArray.size() != 0) {
                                videoCnt = videoCnt % videoArray.size();
                                Mat frame = videoArray.get(videoCnt);
                                drawOnCamera(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), frame.getNativeObjAddr());
                                videoCnt++;
                            }
                        }
                    }
                }
            }
            //
        } else {
            if(!firstSquareCheckr && (System.currentTimeMillis() - detectStartTime) < 1000) {
                if(videoArray.size() != 0) {
                    videoCnt = videoCnt % videoArray.size();
                    Mat frame = videoArray.get(videoCnt);
                    drawOnCamera(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(), frame.getNativeObjAddr());
                    videoCnt++;
                }
            } else if(!firstSquareCheckr){
                videoFunc = false;
                firstSquareCheckr = true;
                firstDetectChecker = true;
                videoArray.clear();
                downloadFinish = false;
                videoCnt =0;
            }
        }

        return matResult;
    }

    private void read_image_file() {
        //이미지 불러오기

        BitmapDrawable drawableobj = (BitmapDrawable)getResources().getDrawable(R.drawable.loading);
        bitmapobj = drawableobj.getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapobj, 0, 0, bitmapobj.getWidth(), bitmapobj.getHeight(), matrix, true);
        Utils.bitmapToMat(resizedBitmap, Loading);


        //imageview에 이미지 올리기
        //  Bitmap bitmapInput = Bitmap.createBitmap(surfInput.cols(), surfInput.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(surfInput, bitmapInput);
        //objectImgView.setImageBitmap(bitmapInput);
    }


    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }

        //모든 퍼미션이 허가되었음
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    } else {
                        //read_image_file();
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( PictureDetector.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


}