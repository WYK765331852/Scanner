package com.example.wyk.scanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.bean.ImageBean;

import java.util.List;

public class ImageWaterfallAdapter extends RecyclerView.Adapter<ImageWaterfallAdapter.BaseViewHolder> {
    private List<ImageBean> imageBeanList;

    public ImageWaterfallAdapter(List<ImageBean> list) {
        this.imageBeanList = list;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manipulation_cv, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.imageView.setImageResource(imageBeanList.get(position).getImage());

    }

    @Override
    public int getItemCount() {
        return imageBeanList.size();
    }

    public void replaceAll(List<ImageBean> list) {
        imageBeanList.clear();
        if (list != null && list.size() > 0) {
            imageBeanList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addItem(int position, List<ImageBean> list) {
        imageBeanList.addAll(position, list);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        imageBeanList.remove(position);
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
