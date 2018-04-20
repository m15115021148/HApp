package com.meigsmart.huaapp.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.adapter.DeviceListAdapter;
import com.meigsmart.huaapp.application.MyApplication;
import com.meigsmart.huaapp.db.BluetoothBean;
import com.meigsmart.huaapp.scan.QRActivity;
import com.meigsmart.huaapp.util.MapLocationUtil;
import com.meigsmart.huaapp.util.ToastUtil;

import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements View.OnClickListener,DeviceListAdapter.OnDeviceListCallBack{
    private MainActivity mContext;
    @BindView(R.id.msgLayout)
    public LinearLayout mMsgLayout;//message
    @BindView(R.id.addDevices)
    public RelativeLayout mAddDevices;
    @BindView(R.id.deviceList)
    public TextView mDeviceList;
    @BindView(R.id.listView)
    public ListView mLv;
    private DeviceListAdapter mAdapter;
    private long exitTime = 0;//退出的时间
    private MapLocationUtil mapLocationUtil;//百度定位

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        mContext = this;
        mMsgLayout.setOnClickListener(this);
        mAddDevices.setOnClickListener(this);

        mAdapter = new DeviceListAdapter(this);
        mLv.setAdapter(mAdapter);
        mDeviceList.setVisibility(View.GONE);
        mHandler.sendEmptyMessage(1001);
        locationPoint();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    getDeviceList();
                    break;
                default:
                    break;
            }
        }
    };

    public void getDeviceList(){
        List<BluetoothBean> list = MyApplication.getInstance().mBlueDb.getAllData();
        if (list!=null && list.size()>0){
            mDeviceList.setVisibility(View.VISIBLE);
            mAdapter.setList(list);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mAddDevices) {
            Intent intent = new Intent(mContext,QRActivity.class);
            intent.putExtra("type",0);
            startActivityForResult(intent,111);
        }
        if (view == mMsgLayout){
            Intent system = new Intent(mContext, SystemMessageActivity.class);
            startActivity(system);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 111){
            try {
                Bundle bundle = data.getExtras();
                String msg = bundle.getString(QRActivity.RESULT);
                String serialNum = msg.substring(2,msg.length());

                if (TextUtils.isEmpty(getDeviceInfo(serialNum))){
                    saveDeviceInfo(serialNum);
                }else{
                    ToastUtil.showBottomShort(getString(R.string.device_info_exit));
                }

                mHandler.sendEmptyMessageDelayed(1001,1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean saveDeviceInfo(String serialNum){
        String blueName = "";
        if (!TextUtils.isEmpty(serialNum)){
            if (serialNum.length()<6){
                return false;
            }
            blueName = serialNum.substring(serialNum.length() - 6);
        }
        BluetoothBean bean = new BluetoothBean();
        bean.setSerialNum(serialNum);
        bean.setPassword("");
        bean.setIsFirstSetPsw("1");
        bean.setBlueName(blueName);
        MyApplication.getInstance().mBlueDb.addData(bean);
        return true;
    }

    private String getDeviceInfo(String serialNum){
        if (TextUtils.isEmpty(serialNum))return "";
        BluetoothBean bean = MyApplication.getInstance().mBlueDb.getData(serialNum);
        if (bean!=null && !bean.equals("{}") && bean.getId()!=null){
            return bean.getSerialNum();
        }
        return "";
    }

    @Override
    public void onClickItem(final int position) {
        Intent intent = new Intent(mContext,UserDeviceInfoActivity.class);
        intent.putExtra("serialNum",mAdapter.getList().get(position).getSerialNum());
        intent.putExtra("serialId",mAdapter.getList().get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onLongClickItem(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Prompt");
        builder.setMessage("Delete the device?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyApplication.getInstance().mBlueDb.deleteCurData(mAdapter.getList().get(position).getSerialNum());
                mAdapter.getList().remove(position);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getList().size()<=0){
                    mDeviceList.setVisibility(View.GONE);
                }
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();
    }

    /**
     * 退出activity
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtil.showBottomShort(getResources().getString(R.string.home_pager_exit_show));
                exitTime = System.currentTimeMillis();
            } else {
                //退出所有的activity
                Intent intent = new Intent();
                intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
                sendBroadcast(intent);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapLocationUtil.unregisterListener(mBDListener); //注销掉监听
        mapLocationUtil.stop(); //停止定位服务
    }

    /**
     * 开启定位
     */
    private void locationPoint() {
        // -----------location config ------------
        mapLocationUtil = ((MyApplication) getApplication()).mapLocationUtil;
        mapLocationUtil.registerListener(mBDListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            mapLocationUtil.setLocationOption(mapLocationUtil.getDefaultLocationClientOption());
        } else if (type == 1) {
            mapLocationUtil.setLocationOption(mapLocationUtil.getOption());
        }
        mapLocationUtil.start();// 定位SDK
    }

    /**
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mBDListener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                MyApplication.lat = location.getLatitude();
                MyApplication.lng = location.getLongitude();
            }
        }
    };

}
