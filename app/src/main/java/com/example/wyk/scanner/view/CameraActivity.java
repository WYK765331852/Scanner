package com.example.wyk.scanner.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wyk.scanner.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static com.example.wyk.scanner.view.MainActivity.CAMERA_PIC;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static String TAG_TEST = "OpenCV_test ";

    private JavaCameraView cameraView;
    private ImageView photoIv;
    private Toolbar camToolbar;

    public static Mat mOriginalRGBA;
    private boolean isPhotoTaking;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.d(TAG_TEST, "OpenCV Load Successfully!");
                    cameraView.enableView();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG_TEST, "Internal OpenCV library not found.");
//            调用外部opencv manager app
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG_TEST, "OpenCV library found inside package. Using it!");
            Toast.makeText(CameraActivity.this, "OpenCV Load Successfully!", Toast.LENGTH_SHORT).show();
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_camera);

        cameraView = findViewById(R.id.app_camera_view);
        cameraView.setCvCameraViewListener(this);
        cameraView.setVisibility(SurfaceView.VISIBLE);

        photoIv = findViewById(R.id.app_camera_photo_iv);
        photoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPhotoTaking = true;
            }
        });

        camToolbar = findViewById(R.id.app_camera_toolbar);
        setSupportActionBar(camToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null) {
            cameraView.disableView();
        }
    }

    /**
     * 当摄像机预览开始时，这个方法就会被调用。在调用该方法之后，框架将通过onCameraFrame()回调向客户端发送。
     *
     * @param width  - 帧的宽度
     * @param height - 帧的高度
     */
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    /**
     * 当摄像机预览由于某种原因被停止时，这个方法就会被调用。
     * 在调用这个方法之后，不会通过onCameraFrame()回调来传递任何帧。
     */
    @Override
    public void onCameraViewStopped() {

    }

    /**
     * 当需要完成框架的交付时，将调用此方法。
     * 返回值-是一个修改后的帧，需要在屏幕上显示。
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        if (isPhotoTaking) {
            isPhotoTaking = false;

            mOriginalRGBA = rgba.clone();
//            Mat affineTrans = Imgproc.getRotationMatrix2D(new Point(rgba.rows()/2, rgba.cols()/2), -90, 1);
//            Imgproc.warpAffine(rgba, mOriginalRGBA, affineTrans, mOriginalRGBA.size(), Imgproc.INTER_NEAREST);
//            mOriginalRGBA = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC4);
//            Imgproc.cvtColor(rgba, mOriginalRGBA, Imgproc.COLOR_RGBA2BGR);
            if (mOriginalRGBA != null) {
                Log.d(TAG_TEST, "get mOriginalRGBA!");
                Intent patternIntent = new Intent(CameraActivity.this, CamImgProcessActivity.class);
                patternIntent.putExtra("PicPattern", CAMERA_PIC);
                startActivity(patternIntent);
            }
        }
        return rgba;
    }

}
