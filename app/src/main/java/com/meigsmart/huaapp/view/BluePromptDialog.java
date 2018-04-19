package com.meigsmart.huaapp.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.meigsmart.huaapp.R;

/**
 * Created by chenMeng on 2018/4/17.
 */
public class BluePromptDialog extends Dialog {
    public BluePromptDialog(@NonNull Context context) {
        super(context);
    }

    public BluePromptDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BluePromptDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.blue_prompt_dialog);
        TextView sure = findViewById(R.id.sure);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
