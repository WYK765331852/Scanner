package com.example.wyk.scanner.view;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.model.ImageModelApi;
import com.example.wyk.scanner.dialog.AlbumAlertDialog;
import com.example.wyk.scanner.model.ImageModelApiImpl;
import com.example.wyk.scanner.model.PreProcessUtil;
import com.example.wyk.scanner.presenter.ImgProcPresenterApi;
import com.example.wyk.scanner.presenter.ImgProcPresenterApiImpl;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;

public class AlbumActivity extends AppCompatActivity implements ViewApi {

    private AlbumAlertDialog albumAlertDialog;
    public static final int CHOOSE_PHOTO = 1;

    private ImageView originalPic;
    private TextView reselectTv;
    private TextView correctionTv;
    private TextView saveTv;
    private ProgressBar progressBar;

    private ImgProcPresenterApi presenterApi;
    private Bitmap originalBmp;
    private Mat srcMat;
    private Mat dstMat;
    private PreProcessUtil processUtil;
    private int windowWidth;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.d(TAG_TEST, "OpenCV Load Successfully!");
                    srcMat = new Mat();
                    dstMat = new Mat();
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
            Toast.makeText(AlbumActivity.this, "OpenCV Load Successfully!", Toast.LENGTH_SHORT).show();
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_album);
        originalPic = findViewById(R.id.app_album_original_iv);
        reselectTv = findViewById(R.id.app_album_reselect_tv);
        correctionTv = findViewById(R.id.app_album_correction_tv);
        saveTv = findViewById(R.id.app_album_save_tv);
        progressBar = findViewById(R.id.app_album_progressbar);

        processUtil = new PreProcessUtil();
        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        windowWidth = displayMetrics.widthPixels;

        albumAlertDialog = new AlbumAlertDialog(AlbumActivity.this);
        albumAlertDialog.setOnBottomBtClickListener(new AlbumAlertDialog.OnBottomBtClickListener() {
            @Override
            public void onPositiveBtClickListener() {
                if (ContextCompat.checkSelfPermission(AlbumActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AlbumActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
                } else {
                    Intent picPickIntent = new Intent(Intent.ACTION_PICK, null);
                    picPickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(picPickIntent, CHOOSE_PHOTO);
                }
            }

            @Override
            public void onNegativeBtClickListener() {
                albumAlertDialog.dismiss();
            }
        }).show();

        presenterApi = new ImgProcPresenterApiImpl(this);

        reselectTv.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(AlbumActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AlbumActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
            } else {
                Intent picPickIntent = new Intent(Intent.ACTION_PICK, null);
                picPickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(picPickIntent, CHOOSE_PHOTO);
            }
        });
        correctionTv.setOnClickListener(view -> {
            Toast.makeText(AlbumActivity.this, "还没写哦", Toast.LENGTH_SHORT).show();
        });
        saveTv.setOnClickListener(view -> {
            Bitmap saveBitmap = ((BitmapDrawable) originalPic.getDrawable()).getBitmap();
            presenterApi.saveImg(AlbumActivity.this, saveBitmap);
        });
    }

    //    询问是否有权限———→若无，则requestCode返回值为CHOOSE_PHOTO；执行此方法。
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                Intent picPickIntent = new Intent(Intent.ACTION_PICK, null);
                picPickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(picPickIntent, CHOOSE_PHOTO);
                break;
            default:
                break;
        }
    }

    //    requestCode返回后调用此方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        albumAlertDialog.dismiss();
        switch (requestCode) {
            case CHOOSE_PHOTO:
                Uri uri = data.getData();
                Log.d(TAG_TEST, "SD path: " + uri.toString());
//            ContentResolver通过URI来查询ContentProvider中提供的数据
                ContentResolver contentResolver = this.getContentResolver();
                try {
                    originalBmp = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                    originalBmp = processUtil.scaleImg(originalBmp, windowWidth);
                    originalPic.setImageBitmap(originalBmp);

                    if (originalBmp != null) {
                        srcMat.create(originalBmp.getWidth(), originalBmp.getHeight(), CvType.CV_8UC4);
                        Utils.bitmapToMat(originalBmp, srcMat);
                        dstMat.create(srcMat.width(), srcMat.height(), CvType.CV_8UC4);
                        presenterApi.preProcessImg(AlbumActivity.this, srcMat, dstMat, 0.0);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Toast.makeText(AlbumActivity.this, "还没选择照片呢~", Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setPreProcessError(String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AlbumActivity.this, "预处理失败😟: " + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void setCorrectionError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AlbumActivity.this, "文档校正失败😟", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void setSaveImgError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AlbumActivity.this, "图片保存失败😟", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void setPreProcessSuccess(Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AlbumActivity.this, "预处理完成😊", Toast.LENGTH_SHORT).show();
            }
        });
        originalPic.setImageBitmap(bmp);
    }

    @Override
    public void setCorrectionSuccess() {
//        Looper.prepare();
//        Toast.makeText(AlbumActivity.this, "文档校正完成😃", Toast.LENGTH_SHORT).show();
//        Looper.loop();
    }

    @Override
    public void setSaveImgSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AlbumActivity.this, "图片已保存😃", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
