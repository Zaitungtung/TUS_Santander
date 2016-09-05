package com.fourmob.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ColorPickerPaletteB extends LinearLayout {

    private ColorPickerButton.OnButtonClickListener mOnButtonClickListener;
    private ColorPickerButton.OnButton_Cancel_ClickListener mOnButtonCancel;

    public ColorPickerPaletteB(Context context) {
        super(context);
    }


    public ColorPickerPaletteB(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void drawPalette() {
        View a = createAcceptButton();
        addView(a);
    }

    private ColorPickerButton createAcceptButton() {
        ColorPickerButton colorPicker = new ColorPickerButton(getContext(), mOnButtonClickListener, mOnButtonCancel);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        colorPicker.setLayoutParams(layoutParams);
        return colorPicker;
    }

    public void init(ColorPickerButton.OnButtonClickListener onButtonClickListener,
                     ColorPickerButton.OnButton_Cancel_ClickListener onButtonCancel) {
        this.mOnButtonCancel = onButtonCancel;
        this.mOnButtonClickListener = onButtonClickListener;

    }
}