package com.example.camerademo1;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.core.view.NestedScrollingParent;

public class StickyLayout extends LinearLayout implements NestedScrollingParent {
    public StickyLayout(Context context) {
        super(context);
    }
}
