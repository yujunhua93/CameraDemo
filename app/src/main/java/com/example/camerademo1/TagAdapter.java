package com.example.camerademo1;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class TagAdapter<T> {

    public abstract View onCreateView(ViewGroup parent,int position); //返回视图

    public abstract void onBindView(View view,int position); //绑定视图

    public abstract int getItemCount(); //数据数量

    protected void setDatas(List<T> datas){

    }

    protected void notifyDataSetChanged(){
        if (mOnDataChangeListener != null){
            mOnDataChangeListener.onDataChange();
        }
    }

    private OnDataChangeListener mOnDataChangeListener;

    public interface OnDataChangeListener{
        void onDataChange();
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        this.mOnDataChangeListener = onDataChangeListener;
    }

}
