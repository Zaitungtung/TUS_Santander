package com.fourmob.colorpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class ColorPickerButton extends FrameLayout implements View.OnClickListener {

    private final OnButtonClickListener mOnButtonClickListener;
    private final OnButton_Cancel_ClickListener mOnButtonCancelClickListener;
    private final Button mButton_A;
    private final Button mButton_C;
    private String mText;

    public ColorPickerButton(Context paramContext, OnButtonClickListener onButtonClickListener, OnButton_Cancel_ClickListener onButton_Cancel_Click) {
        super(paramContext);
        this.mOnButtonClickListener = onButtonClickListener;
        this.mOnButtonCancelClickListener = onButton_Cancel_Click;
        LayoutInflater.from(paramContext).inflate(R.layout.color_picker_button, this);
        this.mButton_A = ((Button) findViewById(R.id.button_confirm_d));
        this.mButton_C = ((Button) findViewById(R.id.button_cancel_d));
        mButton_A.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonClickListener != null)
                    mOnButtonClickListener.onButtonClick(mText);
            }
        });

        mButton_C.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnButtonCancelClickListener != null)
                    mOnButtonCancelClickListener.onButton_Cancel_Click(false);
            }
        });
    }

    public void onClick(View view) {

    }

    public interface OnButtonClickListener {
        void onButtonClick(String text);
    }

    public interface OnButton_Cancel_ClickListener {
        void onButton_Cancel_Click(boolean save);
    }

}