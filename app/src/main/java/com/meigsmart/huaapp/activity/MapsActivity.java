package com.meigsmart.huaapp.activity;


import android.view.View;

import com.meigsmart.huaapp.R;


public class MapsActivity extends BaseActivity implements View.OnClickListener{
    private MapsActivity mContext;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_maps;
    }

    @Override
    protected void initData() {
        mContext = this;

    }

    @Override
    public void onClick(View v) {

    }



}
