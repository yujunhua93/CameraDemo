package com.example.camerademo1;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity3 extends AppCompatActivity {
        private  MediaExtractor mediaExtractor;
        private final static String TAG = "tag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mediaExtractor = new MediaExtractor();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaExtractor.setDataSource("https://laiyads.oss-cn-beijing.aliyuncs.com/dynamicResource/371/4f40ba7d56bb925b85f51f03e75b7655.mp4");//设置添加MP4文件路径
                    int count = mediaExtractor.getTrackCount();//获取轨道数量
                    Log.e(TAG, "轨道数量 = "+count);
                    for (int i = 0; i < count; i++){
                        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(0);
                        Log.e(TAG, i+"编号通道格式 = "+mediaFormat.getString(MediaFormat.KEY_MIME));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}