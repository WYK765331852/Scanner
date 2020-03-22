package com.example.wyk.scanner.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.wyk.scanner.R;

public class AlbumAlertDialog extends Dialog {

    private Button positiveBt;
    private Button negativeBt;

    private OnBottomBtClickListener onBottomBtClickListener;


    public AlbumAlertDialog(Context context) {
        super(context, R.style.AlbumDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_album_dialog);

        positiveBt = findViewById(R.id.app_album_dialog_positive_bt);
        negativeBt = findViewById(R.id.app_album_dialog_negative_bt);

        positiveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomBtClickListener.onPositiveBtClickListener();
            }
        });
        negativeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBottomBtClickListener.onNegativeBtClickListener();
            }
        });

    }

    public AlbumAlertDialog setOnBottomBtClickListener(OnBottomBtClickListener onBottomBtClickListener1) {
        this.onBottomBtClickListener = onBottomBtClickListener1;
        return this;
    }

    public interface OnBottomBtClickListener {
        void onPositiveBtClickListener();

        void onNegativeBtClickListener();
    }

}
