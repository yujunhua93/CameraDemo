package com.example.camerademo1.livedata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.camerademo1.R;

public class LiveDataMainActivity extends AppCompatActivity {

    private TestViewModel mTestViewModel;

    TextView mTextView;

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data_main);
        mTextView = findViewById(R.id.live_data_tv);
        mTestViewModel = ViewModelProviders.of(this).get(TestViewModel.class);
        MutableLiveData<String> name = mTestViewModel.getNameEvent();
        name.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mTextView.setText(s.toString());
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestViewModel.getNameEvent().setValue("哈哈哈哈哈");
            }
        });
        Glide.with(this).load("123").into(mImageView);
        Toast.makeText(this,"123",Toast.LENGTH_SHORT).show();

    }

}