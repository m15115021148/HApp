package com.meigsmart.huaapp.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.application.MyApplication;
import com.meigsmart.huaapp.blue.BluBaseModel;
import com.meigsmart.huaapp.blue.BluSetPswModel;
import com.meigsmart.huaapp.blue.BlueDeviceStatusModel;
import com.meigsmart.huaapp.blue.BluetoothConnListener;
import com.meigsmart.huaapp.blue.BluetoothService;
import com.meigsmart.huaapp.config.RequestCode;
import com.meigsmart.huaapp.db.BluetoothBean;
import com.meigsmart.huaapp.http.rxjava.observable.DialogTransformer;
import com.meigsmart.huaapp.http.rxjava.observer.BaseObserver;
import com.meigsmart.huaapp.http.service.HttpManager;
import com.meigsmart.huaapp.log.LogUtil;
import com.meigsmart.huaapp.model.BindDeviceModel;
import com.meigsmart.huaapp.model.DeviceInfoModel;
import com.meigsmart.huaapp.util.PreferencesUtil;
import com.meigsmart.huaapp.util.QRCodeUtil;
import com.meigsmart.huaapp.util.ToastUtil;
import com.meigsmart.huaapp.view.BluePromptDialog;
import com.meigsmart.huaapp.view.BlueSetPwdDialog;
import com.meigsmart.huaapp.view.SimpleArcDialog;

import java.net.ConnectException;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * 设备信息
 *
 * @author chenmeng created by 2017/9/7
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UserDeviceInfoActivity extends BaseActivity implements View.OnClickListener ,BluetoothConnListener ,BlueSetPwdDialog.OnSetPwdCallBack {
    private UserDeviceInfoActivity mContext;//本类
    @BindView(R.id.back)
    public LinearLayout mBack;//返回上一层
    @BindView(R.id.title)
    public TextView mTitle;//标题
    @BindView(R.id.more)
    public LinearLayout mRightLayout;//绑定设备
    @BindView(R.id.rightName)
    public TextView mRightName;//标题右侧名称
    @BindView(R.id.card_qr)
    public ImageView mQr;//二维码
    private String deviceSerial = "";//设备序列号
    private DeviceInfoModel mInfoModel;//信息结果
    @BindView(R.id.deviceNumber)
    public TextView mDeviceNum;
    @BindViews({R.id.eventLayout,R.id.setLayout})
    public List<LinearLayout> mLayoutList;//布局集合
    private static final int REQUEST_SUCCESS = 0x001;
    private static final int REQUEST_FAILURE = 0x002;
    private static final int GET_BLUETOOTH_STATUS = 0x003;
    private static final int BLUETOOTH_STOP = 0x004;
    private BluetoothService mBlueService;
    private String blueName = "";
    private SimpleArcDialog mDialog;//dialog
    private String mPassword = "";
    private int mTypeBind = 3;//0 prompt ; 1 setpwd ; 2 come settings ; 3 default
    private String serialId = "";
    @BindView(R.id.google)
    public LinearLayout mGoogle;
    @BindView(R.id.baidu)
    public LinearLayout mBaidu;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_device_info;
    }

    @Override
    protected void initData() {
        mContext = this;
        mTitle.setText(getResources().getString(R.string.user_info_title));
        mBack.setOnClickListener(this);
        mRightLayout.setOnClickListener(this);
        mLayoutList.get(0).setOnClickListener(this);
        mLayoutList.get(1).setOnClickListener(this);
        mGoogle.setOnClickListener(this);
        mBaidu.setOnClickListener(this);
        mRightName.setText(R.string.user_info_binding);

        mDialog = new SimpleArcDialog(this, R.style.MyDialogStyle);

        String deviceSerial = getIntent().getStringExtra("serialNum");
        serialId = getIntent().getStringExtra("serialId");
        getUserInfoBySerial(deviceSerial);

        mBlueService = new BluetoothService();
        mBlueService.setmListener(this);

        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBlueService.stopScanBlu();
            }
        });

    }

    /**
     * 获取蓝牙状态
     */
    private boolean getStatusData(String serialNum) {
        if (!TextUtils.isEmpty(serialNum)){
            if (serialNum.length()<6){
                return false;
            }
            blueName = serialNum.substring(serialNum.length() - 6);
        }

        BlueDeviceStatusModel model = new BlueDeviceStatusModel();
        model.setPassword(getBluePwd(serialNum));
        model.setSerial(serialNum);
        model.setMethod(RequestCode.BLUE_GET_DEVICE_STATUS);
        String req = JSON.toJSONString(model);

        LogUtil.e("result","req:"+req);

        return mBlueService.startScanBlue(this,req,blueName);
    }

    private String getBluePwd(String serialNum){
        if (TextUtils.isEmpty(serialNum))return "";
        BluetoothBean bean = MyApplication.getInstance().mBlueDb.getData(serialNum);
        if (bean!=null && !bean.equals("{}") && bean.getId()!=null){
            return bean.getPassword();
        }
        return "";
    }

    private boolean setPassword(String serialNum,String psw){
        if (!TextUtils.isEmpty(serialNum)){
            if (serialNum.length()<6){
                return false;
            }
            blueName = serialNum.substring(serialNum.length() - 6);
        }

        BluSetPswModel model = new BluSetPswModel();
        model.setMethod(RequestCode.BLUE_SET_PASSWORD);
        model.setSerial(serialNum);
        model.setPassword("");
        model.setNew_password(psw);

        String test_json_req = JSON.toJSONString(model);
        LogUtil.v("result","req:"+test_json_req);

        return mBlueService.startScanBlue(mContext,test_json_req,blueName);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_SUCCESS:
                    if (isSupportBluetooth()){
                        getStatusData(mInfoModel.getData().getSerialNumber());
                    }else {
                        if (mDialog.isShowing())mDialog.dismiss();
                        ToastUtil.showBottomShort(getResources().getString(R.string.device_set_no_support));
                    }
                    break;
                case REQUEST_FAILURE:
                    if (mDialog.isShowing())mDialog.dismiss();
                    break;
                case GET_BLUETOOTH_STATUS:
                    mBlueService.stopScanBlu();
                    BluBaseModel model = JSON.parseObject(msg.obj.toString(),BluBaseModel.class);
                    if (model.getMethod().equals(RequestCode.BLUE_GET_DEVICE_STATUS)){
                        if (model.getResult()== 200 ){
                            mTypeBind = 2;
                            mDialog.dismiss();

                            Intent intent = new Intent(mContext,SystemSetActivity.class);
                            intent.putExtra("serial_number",mInfoModel.getData().getSerialNumber());
                            startActivity(intent);

                        } else if (model.getResult()== 400 ){//no set password
                            mTypeBind = 1;
                            BlueSetPwdDialog dialog = new BlueSetPwdDialog(mContext,R.style.MyDialogStyle);
                            dialog.setCallBack(mContext);
                            dialog.show();
                        }else {
                            mTypeBind = 0;
                            mDialog.dismiss();
                            BluePromptDialog d = new BluePromptDialog(mContext,R.style.MyDialogStyle);
                            d.show();
                        }
                    } else if (model.getMethod().equals(RequestCode.BLUE_SET_PASSWORD)){
                        if (mDialog!=null && mDialog.isShowing()) mDialog.dismiss();
                        if (model.getResult() == 200) {
                            mTypeBind = 2;
                            ToastUtil.showBottomShort(mContext.getResources().getString(R.string.blue_set_psw_success));
                            if (TextUtils.isEmpty(serialId)){
                                BluetoothBean bean = new BluetoothBean();
                                bean.setSerialNum(mInfoModel.getData().getSerialNumber());
                                bean.setPassword(mPassword);
                                bean.setIsFirstSetPsw("1");
                                bean.setBlueName(blueName);
                                MyApplication.getInstance().mBlueDb.addData(bean);
                            }else{
                                MyApplication.getInstance().mBlueDb.updatePsw(serialId,mPassword);
                            }
                        } else if (model.getResult() == 401) {
                            ToastUtil.showBottomShort(mContext.getResources().getString(R.string.blue_set_psw_wrong_password));
                        } else {
                            ToastUtil.showBottomShort(mContext.getResources().getString(R.string.blue_set_psw_wrong));
                        }
                    } else {
                        mDialog.dismiss();
                        ToastUtil.showBottomShort(mContext.getResources().getString(R.string.blue_connection_exception));
                    }
                    break;
                case BLUETOOTH_STOP:
                    if (mDialog.isShowing())mDialog.dismiss();
                    mBlueService.stopScanBlu();
                    break;
                default:
                    break;
            }
        }
    };

    private boolean isSupportBluetooth(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return true;
        }
        return false;
    }

    /**
     * 获取设备信息 serial_number
     */
    private void getUserInfoBySerial(String serial_number) {
        HttpManager.getApiService().getDeviceInfoBySerial(serial_number)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(new DialogTransformer(this, getResources().getString(R.string.loading_title)).<DeviceInfoModel>transformer())
                .subscribe(new BaseObserver<DeviceInfoModel>() {

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_error));
                        } else if (e instanceof ConnectException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_noNet));
                        } else {//其他或者没网会走这里
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_exception));
                        }
                        mHandler.sendEmptyMessage(REQUEST_FAILURE);
                    }

                    @Override
                    protected void onSuccess(DeviceInfoModel model) {
                        Bitmap logo = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                        String msg = "";
                        if (model.getResult().equals("200")) {
                            mInfoModel = model;
                            msg = RequestCode.QR_DEVICE_HEADER + (model.getData() != null ? model.getData().getSerialNumber() : getResources().getString(R.string.user_info_no_result));
                            mDeviceNum.setText("SN: "+model.getData().getSerialNumber());

//                            mHandler.sendEmptyMessage(REQUEST_SUCCESS);
                        } else {
                            msg = model.getReason();
                            ToastUtil.showBottomShort(model.getReason());
                            mHandler.sendEmptyMessage(REQUEST_FAILURE);
                        }
                        mQr.setImageBitmap(QRCodeUtil.createQRImage(msg, null));
                    }
                });
    }

    /**
     * 绑定设备
     *
     * @param serial_number
     */
    private void bindDevice(String serial_number) {
        HttpManager.getApiService().bindDevice(serial_number)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(new DialogTransformer(this, getResources().getString(R.string.loading_title)).<BindDeviceModel>transformer())
                .subscribe(new BaseObserver<BindDeviceModel>() {

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_error));
                        } else if (e instanceof ConnectException) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_noNet));
                        } else {//其他或者没网会走这里
                            ToastUtil.showBottomShort(getResources().getString(R.string.http_exception));
                        }
                    }

                    @Override
                    protected void onSuccess(BindDeviceModel model) {
                        if (model.getResult().equals("200")) {
                            ToastUtil.showBottomShort(getResources().getString(R.string.user_info_binding_success));
                        } else {
                            ToastUtil.showBottomShort(model.getReason());
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == mBack) {
            mContext.finish();
        }
        if (view == mRightLayout) {// 绑定
            bindDevice(deviceSerial);
        }

        if (view == mLayoutList.get(0)){//事件
            if (mInfoModel==null){
                return;
            }
            Intent intent = new Intent(mContext,DeviceEventActivity.class);
            intent.putExtra("uuid",mInfoModel.getData().getUuid());
            startActivity(intent);
        }
        if (view == mLayoutList.get(1)){//
            if (mInfoModel==null){
                return;
            }
            if (!isSupportBluetooth()){
                ToastUtil.showBottomShort(getResources().getString(R.string.device_set_no_support));
                return;
            }
            if (mTypeBind == 0){
                BluePromptDialog d = new BluePromptDialog(mContext,R.style.MyDialogStyle);
                d.show();
            }else if (mTypeBind == 1){
                mDialog.show();
                BlueSetPwdDialog dialog = new BlueSetPwdDialog(mContext,R.style.MyDialogStyle);
                dialog.setCallBack(mContext);
                dialog.show();
            }else if (mTypeBind == 2){
                Intent intent = new Intent(mContext,SystemSetActivity.class);
                intent.putExtra("serial_number",mInfoModel.getData().getSerialNumber());
                startActivity(intent);
            }else {
                if (!mDialog.isShowing())mDialog.show();
                mHandler.sendEmptyMessage(REQUEST_SUCCESS);
            }
        }
        if (view == mGoogle){
            if (mInfoModel==null){
                return;
            }
            Intent intent = new Intent(mContext,GoogleMapsActivity.class);
            intent.putExtra("uuid",mInfoModel.getData().getUuid());
            startActivity(intent);
        }
        if (view == mBaidu){
            if (mInfoModel==null){
                return;
            }
            Intent intent = new Intent(mContext,BaiduMapActivity.class);
            intent.putExtra("uuid",mInfoModel.getData().getUuid());
            startActivity(intent);
        }
    }

    @Override
    public void onSuccessConnect() {
    }

    @Override
    public void onCancelConnect() {
    }

    @Override
    public void onCommunication() {
    }

    @Override
    public void onReceiveData(String data) {
        Message msg = mHandler.obtainMessage();
        msg.what = GET_BLUETOOTH_STATUS;
        msg.obj = data;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onStopConnect() {
        mHandler.sendEmptyMessage(BLUETOOTH_STOP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 123){
            if (!mDialog.isShowing())mDialog.show();
            getStatusData(mInfoModel.getData().getSerialNumber());
        }else{
            if (mDialog.isShowing())mDialog.dismiss();
        }
    }

    @Override
    public void onSure(String pwd) {
        mPassword = pwd;
        setPassword(mInfoModel.getData().getSerialNumber(),pwd);
    }

    @Override
    public void onCancel() {
        mHandler.sendEmptyMessage(BLUETOOTH_STOP);
    }
}
