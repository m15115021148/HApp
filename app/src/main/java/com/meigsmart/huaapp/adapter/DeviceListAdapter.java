package com.meigsmart.huaapp.adapter;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.db.BluetoothBean;

import butterknife.BindView;

/**
 * Created by chenMeng on 2018/4/18.
 */
public class DeviceListAdapter extends BaseAdapter<BluetoothBean> {
    private OnDeviceListCallBack mCallBack;

    public DeviceListAdapter(OnDeviceListCallBack callBack){
        this.mCallBack = callBack;
    }

    public interface OnDeviceListCallBack{
        void onClickItem(int position);
        void onLongClickItem(int position);
    }

    @Override
    protected int setView() {
        return R.layout.device_list_item;
    }

    @Override
    protected ViewHolder onBindHolder(View view) {
        return new Holder(view);
    }

    @Override
    protected void initData(final int position, ViewHolder viewHolder) {
        BluetoothBean bean = this.list.get(position);
        Holder holder = (Holder) viewHolder;
        holder.name.setText(bean.getSerialNum());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallBack!=null)mCallBack.onClickItem(position);
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mCallBack!=null)mCallBack.onLongClickItem(position);
                return false;
            }
        });
    }

    class Holder extends ViewHolder{
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.layout)RelativeLayout layout;

        Holder(View view) {
            super(view);
        }
    }

}
