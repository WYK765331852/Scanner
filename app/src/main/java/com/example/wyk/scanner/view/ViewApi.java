package com.example.wyk.scanner.view;

import android.graphics.Bitmap;
import android.widget.ProgressBar;

public interface ViewApi {
    void showProgressBar();

    void hideProgressBar();

    void setPreProcessError(String error);

    void setCorrectionError();

    void setThresholdError();

    void setSaveImgError();

    void setPreProcessSuccess(Bitmap bmp);

    void setCorrectionSuccess(Bitmap bmp);

    void setThresholdSuccess(Bitmap bmp);

    void setSaveImgSuccess();
}
