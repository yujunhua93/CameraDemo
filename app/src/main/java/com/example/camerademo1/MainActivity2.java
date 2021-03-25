package com.example.camerademo1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.camerademo1.databinding.ActivityMain2Binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity2 extends AppCompatActivity {

    List<String> list = new ArrayList<>();
    Random random = new Random();
    private Camera2Proxy mCameraProxy;
    private CameraPreview mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMain2Binding activityMain2Binding = DataBindingUtil.setContentView(this,R.layout.activity_main2);
        final User user = new User("xxx","123");
        activityMain2Binding.setUser(user);
        TextView textView = findViewById(R.id.age_tv);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setName("yyy");
                user.setAge("321");
            }
        });


        addData();
        final MyTagAdapter myTagAdapter = new MyTagAdapter(this,list);
        FlowLayout flowLayout = findViewById(R.id.layout);
        flowLayout.setTagAdapter(myTagAdapter);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
                myTagAdapter.notifyDataSetChanged();
            }
        });
        myTagAdapter.setOnDeleteListener(new MyTagAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position) {
                list.remove(position);
                myTagAdapter.notifyDataSetChanged();
            }
        });

//        mCameraView = findViewById(R.id.camera_preview);
//        mCameraProxy = mCameraView.getCameraProxy();
//        mCameraProxy.startPreview();
    }

    private void addData(){
        for (int i = 0; i < 4; i++) {
            int j = random.nextInt(10);
            StringBuilder stringBuilder = new StringBuilder();
            for (int k = 0; k <  j; k++) {
                stringBuilder.append("a");
            }
            list.add(stringBuilder.toString());
        }
    }

}