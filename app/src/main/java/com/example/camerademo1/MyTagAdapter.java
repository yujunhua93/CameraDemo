package com.example.camerademo1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MyTagAdapter extends TagAdapter<String> {

    private List<String> datas;

    private Context mContext;

    public interface OnDeleteListener{
        void onDelete(int position);
    }

    private OnDeleteListener mOnDeleteListener;

    public void setOnDeleteListener(OnDeleteListener onDeleteListener){
        this.mOnDeleteListener = onDeleteListener;
    }


    public MyTagAdapter(Context context,List<String> list) {
        this.mContext = context;
        this.datas = list;
    }

    @Override
    public View onCreateView(ViewGroup parent,int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout,parent,false);
        return view;
    }

    @Override
    public void onBindView(View view, final int position) {
        TextView textView = view.findViewById(R.id.tag);
        textView.setText(datas.get(position));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDeleteListener != null){
                    mOnDeleteListener.onDelete(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }


    @Override
    public void setDatas(List<String> datas) {
        this.datas = datas;
    }


}
