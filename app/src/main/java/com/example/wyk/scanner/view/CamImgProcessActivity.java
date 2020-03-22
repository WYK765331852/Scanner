package com.example.wyk.scanner.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.presenter.ImgProcPresenterApi;
import com.example.wyk.scanner.presenter.ImgProcPresenterApiImpl;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import static com.example.wyk.scanner.view.CameraActivity.mOriginalRGBA;

//Mat mOriginalRGBA 四通道CvType.CV_8UC4，格式为RGBA
public class CamImgProcessActivity extends AppCompatActivity implements ViewApi {
    ImageView originalIv;
    TextView reselectTv;
    TextView correctionTv;
    TextView saveTv;
    ProgressBar progressBar;

    ImgProcPresenterApi imgProcPresenter;

    Mat prePrecessingSrcMat;
    Mat preProcessingDstMat;
    Mat correctionDstMat;

    Bitmap originalBmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_album);

        originalIv = findViewById(R.id.app_album_original_iv);
        reselectTv = findViewById(R.id.app_album_reselect_tv);
        reselectTv.setText("重新拍照");
        correctionTv = findViewById(R.id.app_album_correction_tv);
        saveTv = findViewById(R.id.app_album_save_tv);
        progressBar = findViewById(R.id.app_album_progressbar);

//        获取屏幕宽度，放大图片
        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;
//        Bitmap: ARGB_8888——代表32位ARGB位图
//        originalBmp 四通道 ARGB
        originalBmp = Bitmap.createBitmap(mOriginalRGBA.width(), mOriginalRGBA.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mOriginalRGBA, originalBmp);
        originalBmp = rotateImg(90, originalBmp);
        originalBmp = scaleImg(originalBmp, windowWidth);
        originalIv.setImageBitmap(originalBmp);

        Mat preProcessingSrcMat = new Mat(originalBmp.getWidth(), originalBmp.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(originalBmp, preProcessingSrcMat);

//        preProcessingDstMat 四通道 RGBA
        preProcessingDstMat = new Mat(preProcessingSrcMat.width(), preProcessingSrcMat.height(), CvType.CV_8UC4);

        imgProcPresenter = new ImgProcPresenterApiImpl(this);
//        进入页面即开始自动预处理
        imgProcPresenter.preProcessImg(CamImgProcessActivity.this, preProcessingSrcMat, preProcessingDstMat);

        reselectTv.setOnClickListener(view -> {
            Intent reselectIntent = new Intent(CamImgProcessActivity.this, CameraActivity.class);
            startActivity(reselectIntent);
            this.finish();
        });

        correctionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                应该放入预处理之后的结果
//                imgProcPresenter.correctionImg(preProcessingDstMat, correctionDstMat);
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
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setPreProcessError() {
        Toast.makeText(CamImgProcessActivity.this, "预处理失败😟", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setCorrectionError() {
        Toast.makeText(CamImgProcessActivity.this, "文档校正失败😟", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSaveImgError() {
        Toast.makeText(CamImgProcessActivity.this, "保存图片失败😟", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPreProcessSuccess(Bitmap bmp) {
//        Toast.makeText(CamImgProcessActivity.this, "预处理完成😊", Toast.LENGTH_SHORT).show();
        originalIv.setImageBitmap(bmp);
    }

    @Override
    public void setCorrectionSuccess() {
        Toast.makeText(CamImgProcessActivity.this, "文档校正完成😃", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSaveImgSuccess() {
        Toast.makeText(CamImgProcessActivity.this, "图片已保存😃", Toast.LENGTH_SHORT).show();
    }

    //    旋转
    public Bitmap rotateImg(int angle, Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (resizeBmp != bmp && bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        return resizeBmp;
    }

    //    缩放
    public Bitmap scaleImg(Bitmap bmp, int newWidth) {
        if (bmp == null) {
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scale = ((float) newWidth) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        if (bmp != null && !bmp.isRecycled()) {
            //销毁原图
            bmp.recycle();
            bmp = null;
        }
        return newBmp;
    }
}