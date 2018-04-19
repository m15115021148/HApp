package com.meigsmart.huaapp.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meigsmart.huaapp.R;
import com.meigsmart.huaapp.util.ToastUtil;

/**
 * Created by chenMeng on 2018/4/17.
 */
public class BlueSetPwdDialog extends Dialog implements View.OnClickListener ,DialogInterface.OnCancelListener {
    private TextView mSure;
    private EditText mPwd;
    private boolean isClick;
    private Context mContext;
    private OnSetPwdCallBack mCallBack;

    public void setCallBack(OnSetPwdCallBack callBack){
        this.mCallBack = callBack;
    }

    public interface OnSetPwdCallBack{
        void onSure(String pwd);
        void onCancel();
    }

    public BlueSetPwdDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public BlueSetPwdDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected BlueSetPwdDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.blue_set_pwd_dialog);
        setOnCancelListener(this);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        getWindow().setAttributes(lp);

        mSure = findViewById(R.id.sure);
        mPwd = findViewById(R.id.password);
        mSure.setOnClickListener(this);

        mPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() < 8 ){
                    mSure.setSelected(false);
                    isClick = false;
                } else if (editable.length() >=8 && editable.length()<=16){
                    mSure.setSelected(true);
                    isClick = true;
                } else {
                    mSure.setSelected(false);
                    isClick = false;
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view == mSure){
            if (isClick){
                this.dismiss();
                if (mCallBack!=null)mCallBack.onSure(mPwd.getText().toString().trim());
            }else{
                ToastUtil.showCenterShort(mContext.getResources().getString(R.string.blue_set_pwd_root));
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        this.dismiss();
        if (mCallBack!=null)mCallBack.onCancel();
    }
}
