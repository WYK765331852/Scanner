package com.example.wyk.scanner.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.bean.ImageBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ImageWaterfallAdapter extends RecyclerView.Adapter<ImageWaterfallAdapter.BaseViewHolder> {
    private List<File> files;
    private Context context;

    public ImageWaterfallAdapter(List<File> files, Context context) {
        this.files = files;
        this.context = context;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manipulation_cv, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {

//        Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver()
//                .openInputStream(filesUri.get(position)));
//        Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));

        holder.imageView.setImageURI(Uri.fromFile(files.get(position)));

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void replaceAll(List<File> list) {
        files.clear();
        if (list != null && list.size() > 0) {
            files.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addItem(int position, List<File> list) {
        files.addAll(position, list);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        files.remove(position);
        notifyDataSetChanged();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_manipulation_iv);
        }
    }

}
