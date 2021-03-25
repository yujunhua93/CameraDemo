package com.example.camerademo1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {

    private final static float[] MATRIX = new float[] {
            0.5f, 0, 0, 0, 0,
            0, 0.5f, 0, 0, 0,
            0, 0, 0.5f, 0, 0,
            0, 0, 0, 1, 0 };

    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.about_us_logo);

    Paint paint;
    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(MATRIX);

    public MyImageView(@NonNull Context context) {
        this(context,null);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
//        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
