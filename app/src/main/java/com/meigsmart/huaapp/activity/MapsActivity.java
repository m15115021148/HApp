package com.meigsmart.huaapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.adapter.MyViewPagerAdapter;
import com.meigsmart.huaapp.fragment.BaiDuFragment;
import com.meigsmart.huaapp.fragment.BaseFragment;
import com.meigsmart.huaapp.fragment.GoogleFragment;
import com.meigsmart.huaapp.log.LogUtil;
import com.meigsmart.huaapp.view.NoScrollViewPager;

import java.util.ArrayList;

import butterknife.BindView;

public class MapsActivity extends BaseActivity implements View.OnClickListener{
    private MapsActivity mContext;
    @BindView(R.id.baidu)
    public TextView mBaidu;
    @BindView(R.id.google)
    public TextView mGoogle;
    @BindView(R.id.viewpager)
    public NoScrollViewPager mViewPager;
    @BindView(R.id.back)
    public LinearLayout mBack;
    private BaiDuFragment f1;
    private GoogleFragment f2;
    private ArrayList<BaseFragment> mFragmentList;// 子页面列表
    private String uuid = "";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_maps;
    }

    @Override
    protected void initData() {
        mContext = this;
        mBack.setOnClickListener(this);
        mBaidu.setOnClickListener(this);
        mGoogle.setOnClickListener(this);
        mBaidu.setSelected(true);

        uuid = getIntent().getStringExtra("uuid");
        LogUtil.w("result","uuid:"+uuid);

        f1 = new BaiDuFragment();
        f2 = new GoogleFragment();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(f1);
        mFragmentList.add(f2);
        mViewPager.setAdapter(new MyViewPagerAdapter(getSupportFragmentManager(),mFragmentList));

        mViewPager.setCurrentItem(0);
        setDataFragment(f1);
    }

    @Override
    public void onClick(View v) {
        if (v == mBack)mContext.finish();
        if (v == mBaidu){
            mBaidu.setSelected(true);
            mGoogle.setSelected(false);
            mViewPager.setCurrentItem(0);
            setDataFragment(f1);
        }
        if (v == mGoogle){
            mBaidu.setSelected(false);
            mGoogle.setSelected(true);
            mViewPager.setCurrentItem(1);
            setDataFragment(f2);
        }
    }

    private void setDataFragment(BaseFragment fragment){
        Bundle b = new Bundle();
        b.putString("uuid",uuid);
        fragment.setArguments(b);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
