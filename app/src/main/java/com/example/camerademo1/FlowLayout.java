package com.example.camerademo1;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class FlowLayout extends ViewGroup implements TagAdapter.OnDataChangeListener{

    private TagAdapter mTagAdapter;

    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("onMeasure","onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

//        // wrap_content
//        int width = 0;
//        int height = 0;

        int lineWidth = 0;
        int lineHeight = 0;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int eachWidth = childWidth + lp.leftMargin + lp.rightMargin; //每个子view的宽度
            int eachHeight = childHeight + lp.topMargin + lp.bottomMargin; //每个子view的高度

            if (lineWidth + eachWidth > sizeWidth){
                lineWidth = 0; //重置宽
                lineHeight = lineHeight + eachHeight;
            }else {
                lineWidth = lineWidth + eachWidth;
                lineHeight = Math.max(eachHeight,lineHeight);
            }

        }

        Log.e("height",":"+lineHeight);
        if (modeHeight == MeasureSpec.EXACTLY){
            setMeasuredDimension(sizeWidth,sizeHeight);
        }else {
            setMeasuredDimension(sizeWidth,lineHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("onLayout","onLayout");
        int lineWidth = 0;
        int lineHeight = 0;
        int width = getWidth();
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int eachWidth = childWidth + lp.leftMargin + lp.rightMargin; //每个子view的宽度
            int eachHeight = childHeight + lp.topMargin + lp.bottomMargin; //每个子view的高度

            if (lineWidth + eachWidth < width - getPaddingLeft() - getPaddingRight()){
                lineWidth = lineWidth + eachWidth;
            }else {
                lineWidth = eachWidth;
                lineHeight = lineHeight + eachHeight;
            }

            lineHeight = Math.max(eachHeight,lineHeight);

            child.layout(lineWidth - eachWidth + lp.leftMargin,lineHeight - eachHeight + lp.topMargin ,lineWidth - lp.rightMargin,lineHeight - lp.bottomMargin);
        }
    }


    public void setTagAdapter(TagAdapter tagAdapter){
        mTagAdapter = tagAdapter;
        if (mTagAdapter != null){
            mTagAdapter.setOnDataChangeListener(this);
        }
        refreshLayout();
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) { //一定要用
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public void onDataChange() {
        refreshLayout();
    }

    private void refreshLayout() { //更新布局
        removeAllViews();
        if (mTagAdapter == null){
            throw new IllegalArgumentException("未设置适配器");
        }
        int viewCount = mTagAdapter.getItemCount();
        for (int i = 0; i < viewCount; i++) {
            View view =  mTagAdapter.onCreateView(this,i);
            mTagAdapter.onBindView(view,i);
            addView(view);
        }
    }
}
