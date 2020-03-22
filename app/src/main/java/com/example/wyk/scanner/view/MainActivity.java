package com.example.wyk.scanner.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wyk.scanner.R;
import com.example.wyk.scanner.StaggeredDividerItemDecoration;
import com.example.wyk.scanner.adapter.ImageWaterfallAdapter;
import com.example.wyk.scanner.bean.ImageBean;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button cameraBt;
    Button albumBt;

    SmartRefreshLayout smartRefreshLayout;
    RecyclerView manipulatedPicRclv;
    StaggeredGridLayoutManager layoutManager;
    ImageWaterfallAdapter adapter;

    final int spanCount = 2;
    public static int CAMERA_PIC = 0;
    public static int ALBUM_PIC = 1;

    List<ImageBean> imageBeans = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_main);

        toolbar = findViewById(R.id.app_toolbar);
        toolbar.setTitle(getTime());
        toolbar.setTitleTextColor(getResources().getColor(R.color.app_black));

        cameraBt = findViewById(R.id.app_main_pattern_camera_button);
        albumBt = findViewById(R.id.app_main_pattern_album_button);

        cameraBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(cameraIntent);
            }
        });

        albumBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent albumIntent = new Intent(MainActivity.this, AlbumActivity.class);
                albumIntent.putExtra("PicPattern", ALBUM_PIC);
                startActivity(albumIntent);
            }
        });

        smartRefreshLayout = findViewById(R.id.item_manipulation_refresh_layout);
        manipulatedPicRclv = findViewById(R.id.app_main_manipulation_rcl);
        layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
//        防止item位置切换
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        manipulatedPicRclv.addItemDecoration(new StaggeredDividerItemDecoration(this, 10, spanCount));
//        解决底部滚动到顶部时，顶部item上方偶尔会出现一大片间隔的问题
        manipulatedPicRclv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int[] first = new int[spanCount];
                layoutManager.findFirstCompletelyVisibleItemPositions(first);
                /**
                 * SCROLL_STATE_IDLE——The RecyclerView is not currently scrolling.
                 * @see #getScrollState()
                 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE && (first[0] == 1 || first[1] == 1)) {
                    layoutManager.invalidateSpanAssignments();
                }
            }
        });

//        设置下拉刷新
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.replaceAll(getData());
                        /**
                         * finish refresh.
                         * 完成刷新
                         * @return RefreshLayout
                         */
                        smartRefreshLayout.finishRefresh();
                    }
                }, 1500);
            }
        });
//        设置上拉加载
        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addItem(adapter.getItemCount(), getData());
                        /**
                         * finish load more.
                         * 完成加载
                         * @return RefreshLayout
                         */
                        smartRefreshLayout.finishLoadMore();
                    }
                }, 1500);
            }
        });
    }

    private List<ImageBean> getData() {
        List<ImageBean> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {

        }
        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    private String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");// HH:mm:ss
//      获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

}
