package com.meigsmart.huaapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.model.GroupListModel;

import butterknife.BindView;

/**
 * Created by chenMeng on 2017/12/8.
 */

public class GroupInvitationAdapter extends BaseAdapter<GroupListModel> {
    private OnGroupInvitationListener callBack;

    public GroupInvitationAdapter(OnGroupInvitationListener callBack){
        this.callBack = callBack;
    }

    public interface OnGroupInvitationListener{
        void onItemClick(int position);
    }

    @Override
    protected int setView() {
        return R.layout.group_invitation_item_layout;
    }

    @Override
    protected ViewHolder onBindHolder(View view) {
        return new Holder(view);
    }

    @Override
    protected void initData(final int position, ViewHolder viewHolder) {
        GroupListModel model = this.list.get(position);
        Holder holder = (Holder) viewHolder;

        holder.name.setText(model.getName());

        if (model.getSelect() == 1){
            holder.select.setSelected(true);
        }else{
            holder.select.setSelected(false);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack!=null)callBack.onItemClick(position);
            }
        });

    }

    class Holder extends ViewHolder{
        @BindView(R.id.select)
        ImageView select;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.layout)RelativeLayout layout;

        Holder(View view) {
            super(view);
        }
    }
}
