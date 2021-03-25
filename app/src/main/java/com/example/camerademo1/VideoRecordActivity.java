package com.example.camerademo1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class VideoRecordActivity extends AppCompatActivity {

    private AudioRecord mAudioRecord;

    private Integer mRecordBufferSize;

    private boolean mWhetherRecord;
    private File pcmFile;

    private TextView recordTv;
    private File handlerWavFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }else {
            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }else {
            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
        }

        recordTv =  findViewById(R.id.record_tv);
        recordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWhetherRecord){
                    stopRecord();
                    recordTv.setText("开始");
                }else {
                    startRecord();
                    recordTv.setText("结束");
                }
            }
        });
        initMinBufferSize();
        initAudioRecord();
    }

    private void initMinBufferSize(){
        //获取每一帧的字节流大小
        mRecordBufferSize = AudioRecord.getMinBufferSize(8000
                , AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT);
    }

    private void initAudioRecord(){
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                , 8000
                , AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT
                , mRecordBufferSize);
    }


    private void startRecord(){
        pcmFile = new File(VideoRecordActivity.this.getExternalCacheDir().getPath(),"audioRecord.pcm");
        mWhetherRecord = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();//开始录制
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(pcmFile);
                    byte[] bytes = new byte[mRecordBufferSize];
                    while (mWhetherRecord){
                        mAudioRecord.read(bytes, 0, bytes.length);//读取流
                        fileOutputStream.write(bytes);
                        fileOutputStream.flush();

                    }
                    Log.e("TAG", "run: 暂停录制" );
                    mAudioRecord.stop();//停止录制
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    addHeadData();//添加音频头部信息并且转成wav格式
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mAudioRecord.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void stopRecord(){
        mWhetherRecord = false;
        mAudioRecord.stop();
        mAudioRecord.release();
    }

    private void addHeadData(){
        pcmFile = new File(VideoRecordActivity.this.getExternalCacheDir().getPath(),"audioRecord.pcm");
        handlerWavFile = new File(VideoRecordActivity.this.getExternalCacheDir().getPath(),"audioRecord_handler.wav");
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        pcmToWavUtil.pcmToWav(pcmFile.toString(),handlerWavFile.toString());
    }

}