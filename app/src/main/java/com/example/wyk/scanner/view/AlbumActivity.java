package com.example.wyk.scanner.view;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import java.io.FileNotFoundException;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;

public class AlbumActivity extends AppCompatActivity {

    private AlbumAlertDialog albumAlertDialog;
    public static final int CHOOSE_PHOTO = 1;

    private ImageView pic;
    private TextView reselectTv;
    private TextView correctionTv;
    private TextView saveTv;
    private ProgressBar progressBar;

    private ImageModelApi imageApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_album);
        pic = findViewById(R.id.app_album_original_iv);
        reselectTv = findViewById(R.id.app_album_reselect_tv);
        correctionTv = findViewById(R.id.app_album_correction_tv);
        saveTv = findViewById(R.id.app_album_save_tv);
        progressBar = findViewById(R.id.app_album_progressbar);

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

        });
        saveTv.setOnClickListener(view -> {
            Toast.makeText(AlbumActivity.this, "还没写呢~", Toast.LENGTH_SHORT).show();
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
                    Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                    pic.setImageBitmap(bitmap);
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
}
