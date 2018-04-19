package com.meigsmart.huaapp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.application.MyApplication;
import com.meigsmart.huaapp.gps.Gps;
import com.meigsmart.huaapp.gps.Point;
import com.meigsmart.huaapp.gps.PositionUtil;
import com.meigsmart.huaapp.gps.SmoothTrack;
import com.meigsmart.huaapp.http.rxjava.observable.DialogTransformer;
import com.meigsmart.huaapp.http.rxjava.observer.BaseObserver;
import com.meigsmart.huaapp.http.service.HttpManager;
import com.meigsmart.huaapp.log.LogUtil;
import com.meigsmart.huaapp.model.DeviceTrackListModel;
import com.meigsmart.huaapp.util.DateUtil;
import com.meigsmart.huaapp.util.MapUtil;
import com.meigsmart.huaapp.util.ToastUtil;
import com.meigsmart.huaapp.view.CustomCalendar;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Created by chenMeng on 2018/4/18.
 */
public class BaiDuFragment extends BaseFragment implements View.OnClickListener{
    private Context mContext;
    @BindView(R.id.bmapView)
    public MapView mapView;//mapview
    private MapUtil mapUtil;//地图工具类
    private String deviceUUid = "";//设备uuid
    private String mStartTime = "";//开始时间
    private String mEndTime = "";//终止时间
    @BindView(R.id.time)
    public TextView mTime;//time
    @BindView(R.id.layout)
    public RelativeLayout mLayout;
    @BindView(R.id.left)
    public LinearLayout mLeft;
    @BindView(R.id.right)
    public LinearLayout mRight;
    private final int SEND_TRACK = 0x001;

    @Override
    protected int setContentView() {
        return R.layout.fragment_baidu_maps;
    }

    @Override
    protected void startLoad() {
        assert getArguments() != null;
        deviceUUid = getArguments().getString("uuid");
        mHandler.sendEmptyMessage(SEND_TRACK);
    }

    @Override
    protected void initData() {
        mContext = getContext();
        mTime.setOnClickListener(this);
        mLeft.setOnClickListener(this);
        mRight.setOnClickListener(this);
        mapUtil = new MapUtil(mContext, mapView);
        // 隐藏缩放控件
        mapUtil.hidezoomView();

        mStartTime = DateUtil.getGMTDate(DateUtil.startOfTodDay(new Date()));
        mEndTime = DateUtil.getGMTDate(DateUtil.endOfTodDay(new Date()));

        mTime.setText(DateUtil.getCurrentTime());

        mLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapUtil.clear();
        if (mHandler!=null){
            mHandler.removeMessages(SEND_TRACK);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mLeft){
            mTime.setText(DateUtil.dateToStr(DateUtil.getBeforeDay(DateUtil.strToDate(mTime.getText().toString()))));

            mStartTime = DateUtil.getGMTDate(DateUtil.startOfTodDay(DateUtil.strToDate(mTime.getText().toString())));
            mEndTime = DateUtil.getGMTDate(DateUtil.endOfTodDay(DateUtil.strToDate(mTime.getText().toString())));

            mHandler.sendEmptyMessage(SEND_TRACK);
        }
        if (view == mRight){
            mTime.setText(DateUtil.dateToStr(DateUtil.getNextDay(DateUtil.strToDate(mTime.getText().toString()))));

            mStartTime = DateUtil.getGMTDate(DateUtil.startOfTodDay(DateUtil.strToDate(mTime.getText().toString())));
            mEndTime = DateUtil.getGMTDate(DateUtil.endOfTodDay(DateUtil.strToDate(mTime.getText().toString())));

            mHandler.sendEmptyMessage(SEND_TRACK);
        }
        if (view == mTime) {
            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View v = getLayoutInflater().inflate(R.layout.date_time_layout,null);
            final CustomCalendar custom = (CustomCalendar) v.findViewById(R.id.cal);
            custom.setListener(new CustomCalendar.OnCalendarClickListener() {
                @Override
                public void onLeftRowClick() {
                    custom.monthChange(-1);
                }

                @Override
                public void onRightRowClick() {
                    custom.monthChange(1);
                }

                @Override
                public void onTitleClick(String monthStr, Date month) {
                }

                @Override
                public void onWeekClick(int weekIndex, String weekStr) {
                }

                @Override
                public void onDayClick(int day, String dayStr, CustomCalendar.DayFinish finish) {
                    mTime.setText(dayStr);

                    mStartTime = DateUtil.getGMTDate(DateUtil.startOfTodDay(DateUtil.strToDate(dayStr)));
                    mEndTime = DateUtil.getGMTDate(DateUtil.endOfTodDay(DateUtil.strToDate(dayStr)));

                    deviceTrack(deviceUUid, mStartTime, mEndTime);
                    dialog.dismiss();
                }
            });
            dialog.setContentView(v);
            dialog.show();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEND_TRACK:
                    deviceTrack(deviceUUid, mStartTime, mEndTime);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 设备轨迹
     */
    private void deviceTrack(String device_uuid, String startTime, String endTime) {
        final Map<String, Object> map = new HashMap<>();
        map.put("begin", startTime);
        map.put("end", endTime);

        HttpManager.getApiService().getDeviceTrackList(device_uuid, HttpManager.getParameter(JSON.toJSONString(map)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(new DialogTransformer(getActivity(), getResources().getString(R.string.loading_title)).<DeviceTrackListModel>transformer())
                .subscribe(new BaseObserver<DeviceTrackListModel>() {

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
                    protected void onSuccess(DeviceTrackListModel model) {
                        if (model.getResult().equals("200")) {
                            List<LatLng> list = new ArrayList<>();
                            List<Point> pointList = new ArrayList<>();//轨迹点纠偏数据;
                            if (model.getData() != null && model.getData().size() > 0) {
                                try {
                                    for (DeviceTrackListModel.DeviceTrackModel m : model.getData()) {
                                        if (!mapUtil.isZeroPoint(Double.parseDouble(m.getLatitude()), Double.parseDouble(m.getLongitude()))) {
                                            Point p = new Point();
                                            p.setLat(Double.parseDouble(m.getLatitude()));
                                            p.setLng(Double.parseDouble(m.getLongitude()));
                                            p.setTime(DateUtil.formatDateTime(m.getCreateTime()));
                                            p.setUnixTime(DateUtil.parseDate(DateUtil.formatDateTime(Long.parseLong(m.getTrackTime()))).getTime() / 1000);
                                            pointList.add(p);
                                        }
                                    }

                                    //按时间排序
                                    Collections.sort(pointList, new Comparator<Point>() {

                                        public int compare(Point p1, Point p2) {
                                            long time1 = DateUtil.parseDate(p1.getTime()).getTime();
                                            long time2 = DateUtil.parseDate(p2.getTime()).getTime();
                                            return (time1 == time2 ? 0 : (time1 > time2 ? 1 : -1));
                                        }
                                    });

                                    List<Point> points = SmoothTrack.doSmooth(pointList, 0.1, 1);
                                    List<Point> points1 = SmoothTrack.correcteZShape(points);
                                    List<Point> points2 = SmoothTrack.doSmooth(points1, 0.1, 1);

                                    for (Point p : points2) {
                                        Gps gps = PositionUtil.gps_to_bd09(p.getLat(), p.getLng());
                                        list.add(new LatLng(gps.getWgLat(),gps.getWgLon()));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (list.size() > 0) {
                                        mapUtil.drawHistoryTrack(list);
                                    } else {
                                        mapUtil.clearHistory();
                                        mapUtil.updateStatus(new LatLng(MyApplication.lat,MyApplication.lng),false);
                                        ToastUtil.showBottomShort(getResources().getString(R.string.main_no_track));
                                    }

                                }
                            } else {
                                mapUtil.clearHistory();
                                mapUtil.updateStatus(new LatLng(MyApplication.lat,MyApplication.lng),false);
                                ToastUtil.showBottomShort(getResources().getString(R.string.main_no_track));
                            }

                        } else {
                            mapUtil.clearHistory();
                            mapUtil.updateStatus(new LatLng(MyApplication.lat,MyApplication.lng),false);
                            ToastUtil.showBottomShort(model.getReason());
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.e("requestCode:"+requestCode+" resultCode:"+resultCode);
        if (requestCode == 1001) {
            if (data != null) {
                mapUtil.clear();
                mStartTime = data.getStringExtra("startTime");
                mEndTime = data.getStringExtra("endTime");

                deviceTrack(deviceUUid, mStartTime, mEndTime);
            }
        }

    }
}
