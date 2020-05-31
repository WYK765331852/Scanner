package com.example.wyk.scanner.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.model.PreProcessUtil;
import com.example.wyk.scanner.presenter.ImgProcPresenterApi;
import com.example.wyk.scanner.presenter.ImgProcPresenterApiImpl;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;
import static com.example.wyk.scanner.view.CameraActivity.mOriginalRGBA;

//Mat mOriginalRGBA 四通道CvType.CV_8UC4，格式为RGBA
public class CamImgProcessActivity extends AppCompatActivity implements ViewApi {
    ImageView originalIv;
    TextView reselectTv;
    TextView preProcessTv;
    TextView correctionTv;
    TextView thresholdTv;
    TextView saveTv;
    ProgressBar progressBar;
    Toolbar camProcToolbar;

    ImgProcPresenterApi imgProcPresenter;

    Bitmap originalBmp;
    PreProcessUtil processUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_camera_img_proc);

        originalIv = findViewById(R.id.app_cam_original_iv);
        reselectTv = findViewById(R.id.app_cam_reselect_tv);
        reselectTv.setText("重新拍照");
        preProcessTv = findViewById(R.id.app_cam_preproc_tv);
        correctionTv = findViewById(R.id.app_cam_correction_tv);
        thresholdTv = findViewById(R.id.app_cam_threshold_tv);
        saveTv = findViewById(R.id.app_cam_save_tv);
        progressBar = findViewById(R.id.app_cam_progressbar);
        camProcToolbar = findViewById(R.id.app_cam_preproc_toolbar);

        setSupportActionBar(camProcToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        processUtil = new PreProcessUtil();
//        获取屏幕宽度，放大图片
        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;
//        Bitmap: ARGB_8888——代表32位ARGB位图
//        originalBmp 四通道 ARGB
        originalBmp = Bitmap.createBitmap(mOriginalRGBA.width(), mOriginalRGBA.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mOriginalRGBA, originalBmp);

        originalBmp = processUtil.rotateImg(90, originalBmp);
        originalBmp = processUtil.scaleImg(originalBmp, windowWidth);
        originalIv.setImageBitmap(originalBmp);

        Mat preProcessingSrcMat = new Mat(originalBmp.getWidth(), originalBmp.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(originalBmp, preProcessingSrcMat);

        imgProcPresenter = new ImgProcPresenterApiImpl(this);

        reselectTv.setOnClickListener(view -> {
            Intent reselectIntent = new Intent(CamImgProcessActivity.this, CameraActivity.class);
            startActivity(reselectIntent);
            this.finish();
        });

        preProcessTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgProcPresenter.preProcessImg(CamImgProcessActivity.this, preProcessingSrcMat, 90.0);
            }
        });

        correctionTv.setOnClickListener(view -> {
            Bitmap corSrcBmp = ((BitmapDrawable) originalIv.getDrawable()).getBitmap();
            Mat corSrcMat = new Mat(corSrcBmp.getWidth(), corSrcBmp.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(corSrcBmp, corSrcMat);
            if (corSrcMat != null) {
                imgProcPresenter.correctionDocImg(CamImgProcessActivity.this, corSrcMat);
            }
        });

        thresholdTv.setOnClickListener(view -> {
            Bitmap thSrcBmp = ((BitmapDrawable) originalIv.getDrawable()).getBitmap();
            Mat thSrcMat = new Mat(thSrcBmp.getWidth(), thSrcBmp.getHeight(), CvType.CV_8UC4);
            Utils.bitmapToMat(thSrcBmp, thSrcMat);
            if (thSrcMat != null) {
                imgProcPresenter.thresholdImg(CamImgProcessActivity.this, thSrcMat);
            }
        });

        saveTv.setOnClickListener(view -> {
            Bitmap saveSrc = ((BitmapDrawable) originalIv.getDrawable()).getBitmap();
//                放入需要保存的图片，可以把image view转化为bitmap存入
            imgProcPresenter.saveImg(CamImgProcessActivity.this, saveSrc);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgProcPresenter.onDestroy();
        if (originalBmp != null && !originalBmp.isRecycled()) {
            originalBmp = null;
        }
        originalIv.setImageBitmap(null);
        Log.d(TAG_TEST, "run on destroy!");
    }

    @Override
    public void showProgressBar() {
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setPreProcessError(String error) {
        runOnUiThread(() -> Toast.makeText(CamImgProcessActivity.this, "预处理失败😟: " + error, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void setCorrectionError() {
        runOnUiThread(() -> Toast.makeText(CamImgProcessActivity.this, "文档校正失败😟", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void setThresholdError() {
        runOnUiThread(() -> Toast.makeText(CamImgProcessActivity.this, "二值化失败😟", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void setSaveImgError() {
        runOnUiThread(() -> Toast.makeText(CamImgProcessActivity.this, "保存图片失败😟", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void setPreProcessSuccess(Bitmap bmp) {
        Toast.makeText(CamImgProcessActivity.this, "预处理完成😊", Toast.LENGTH_SHORT).show();
//        if (originalBmp != null && !originalBmp.isRecycled()) {
//            originalBmp = null;
//        }
//        originalIv.setImageBitmap(null);
        originalIv.setImageBitmap(bmp);

    }

    @Override
    public void setCorrectionSuccess(Bitmap bmp) {
        Toast.makeText(CamImgProcessActivity.this, "文档校正完成😃", Toast.LENGTH_SHORT).show();
        originalIv.setImageBitmap(bmp);
    }

    @Override
    public void setThresholdSuccess(Bitmap bmp) {
        Toast.makeText(CamImgProcessActivity.this, "二值化完成😃", Toast.LENGTH_SHORT).show();
        originalIv.setImageBitmap(bmp);
    }

    @Override
    public void setSaveImgSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CamImgProcessActivity.this, "图片已保存😃", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
