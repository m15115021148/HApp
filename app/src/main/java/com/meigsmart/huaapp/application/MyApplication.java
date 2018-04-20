package com.meigsmart.huaapp.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.meigsmart.huaapp.db.BluetoothDao;
import com.meigsmart.huaapp.log.CrashHandler;
import com.meigsmart.huaapp.log.LogUtil;
import com.meigsmart.huaapp.model.ClientsModel;
import com.meigsmart.huaapp.util.MapLocationUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by chenMeng on 2018/4/18.
 */
public class MyApplication extends Application {
    private static MyApplication instance;// application对象
    public static ClientsModel clientsModel;//实体类
    public static double lat = 0;// 纬度
    public static double lng = 0;// 经度
    public BluetoothDao mBlueDb;//数据库
    public static int screenWidth = 0;//屏幕宽
    public static int screenHeight = 0;//屏幕高
    public MapLocationUtil mapLocationUtil;//百度定位

    @Override
    public void onCreate() {
        super.onCreate();
        mBlueDb = new BluetoothDao(this);
        SDKInitializer.initialize(getApplicationContext());
        mapLocationUtil = new MapLocationUtil(getApplicationContext());
        CrashHandler.getInstance().init(this);
    }

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    /**
     * 获取屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        LogUtil.w("result","height:"+screenHeight+" width:"+screenWidth);
    }

    /**
     * 描述：MD5加密.
     *(全大写字母)32
     * @param string
     *            要加密的字符串
     * @return String 加密的字符串
     */
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().toUpperCase();
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersionName() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get App versionCode
     * @return
     */
    public String getVersionCode(){
        PackageManager packageManager=getApplicationContext().getPackageManager();
        PackageInfo packageInfo;
        String versionCode="";
        try {
            packageInfo=packageManager.getPackageInfo(getApplicationContext().getPackageName(),0);
            versionCode=packageInfo.versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * listview没有数据显示 的控件
     * @param context 本类
     * @param T AbsListView
     * @param txt 内容
     */
    public static View setEmptyShowText(Context context, AbsListView T, String txt){
        TextView emptyView = new TextView(context);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setText(txt);
        emptyView.setTextSize(18);
        emptyView.setTextColor(Color.parseColor("#808080"));
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL);
        emptyView.setVisibility(View.GONE);
        ((ViewGroup)T.getParent()).addView(emptyView);
        T.setEmptyView(emptyView);
        return emptyView;
    }

    /**
     * 获取手机设备id
     */
    @SuppressLint("MissingPermission")
    public String getDeviceID(Activity activity){
        TelephonyManager tm = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

}
