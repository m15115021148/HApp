package com.meigsmart.huaapp.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.application.MyApplication;
import com.meigsmart.huaapp.config.RequestCode;
import com.meigsmart.huaapp.http.rxjava.observer.BaseObserver;
import com.meigsmart.huaapp.http.service.HttpManager;
import com.meigsmart.huaapp.log.LogUtil;
import com.meigsmart.huaapp.model.ClientsModel;
import com.meigsmart.huaapp.util.PreferencesUtil;
import com.meigsmart.huaapp.util.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class SplashActivity extends BaseActivity implements Runnable {
    private SplashActivity mContext;
    @BindView(R.id.img)
    public ImageView mImg;
    private Bitmap bt = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    protected void initData() {
        mContext = this;
        if (bt == null)
            bt = BitmapFactory.decodeResource(getResources(),R.drawable.sqlash_bg);
        mImg.setImageBitmap(bt);
        mHandler.postDelayed(this, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this);
        if (bt != null) {
            bt.recycle();
            bt = null;
        }
        System.gc();
    }

    @Override
    public void run() {
        getPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE);
    }

    /**
     * 登录
     */
    private void login(String userName) {
        Map<String,Object> map = new HashMap<>();
        map.put("token",MyApplication.getInstance().getDeviceID(mContext));
        map.put("lang", RequestCode.USER_LANG);
        map.put("bundle_id",RequestCode.USER_BUNDLE_ID);
        map.put("name",userName);
        map.put("versionCode",MyApplication.getInstance().getVersionCode());
        map.put("versionName",MyApplication.getInstance().getVersionName());
        map.put("appKey",RequestCode.APP_KEY);
        map.put("appSecret",RequestCode.APP_SECRET);

        HttpManager.getApiService().registerApp(HttpManager.getParameter(JSON.toJSONString(map)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ClientsModel>() {

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_error));
                        }else if (e instanceof ConnectException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_noNet));
                        } else {//其他或者没网会走这里
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_exception));
                        }
                    }

                    @Override
                    protected void onSuccess(ClientsModel model) {
                        if (model.getResult().equals("200")){
                            PreferencesUtil.setDataModel(mContext,"clientsModel",model);
                            MyApplication.clientsModel = PreferencesUtil.getDataModel(mContext,"clientsModel");
                            Intent intent = new Intent(mContext,MainActivity.class);
                            startActivity(intent);
                            mContext.finish();
                        }else{
                            ToastUtil.showBottomShort(getResources().getString(R.string.login_fail));
                        }
                    }
                });
    }

    /**
     * 获取权限
     * @param context
     * @param str
     */
    @SuppressLint("CheckResult")
    public void getPermission(Activity context, String...str){
        RxPermissions permissions = new RxPermissions(context);
        permissions.request(str)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean){//所以权限都同意 才为true
                            login(MyApplication.getInstance().getDeviceID(mContext));
                        }else{

                        }
                    }
                });
    }

}
