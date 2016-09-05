package com.fourmob.colorpicker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ColorPickerDialog extends DialogFragment implements ColorPickerSwatch.OnColorSelectedListener,
        ColorPickerButton.OnButton_Cancel_ClickListener,
        ColorPickerButton.OnButtonClickListener {

    private AlertDialog mAlertDialog;
    private int[] mColors = null;
    private int mColumns;
    private ColorPickerSwatch.OnColorSelectedListener mListener;
    private ColorPickerButton.OnButtonClickListener mListenerButton;
    private ColorPickerButton.OnButton_Cancel_ClickListener mListenerCancel;
    private String mText;
    private String mName;
    private int mSelectedColor;
    private int mSize;
    private int mTitleResId = R.string.color_picker_default_title;
    private ColorPickerPaletteB mButtonPalette;
    private ColorPickerPalette mPalette;
    private EditText mEditText;

    private void refreshPalette() {
        if ((this.mPalette != null) && (this.mColors != null)) {
            this.mPalette.drawPalette(this.mColors, this.mSelectedColor);
            this.mButtonPalette.drawPalette();
        }
    }

    public void initialize(int titleId, int[] colors, String Name) {
        setArguments(titleId, 4, 4, Name);
        setColors(colors, 0);
    }

    public void onColorSelected(int selectedColor) {
        if (this.mListener != null)
            this.mListener.onColorSelected(selectedColor);
        if ((getTargetFragment() instanceof ColorPickerSwatch.OnColorSelectedListener))
            ((ColorPickerSwatch.OnColorSelectedListener) getTargetFragment()).onColorSelected(selectedColor);
        if (selectedColor != this.mSelectedColor) {
            this.mSelectedColor = selectedColor;
            this.mPalette.drawPalette(this.mColors, this.mSelectedColor);
        }

    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mTitleResId = getArguments().getInt("title_id");
            this.mColumns = getArguments().getInt("columns");
            this.mSize = getArguments().getInt("size");
            this.mName = getArguments().getString("name");
        }
        if (bundle != null) {
            dismiss();/*
            this.mColors = bundle.getIntArray("colors");
            assert (bundle.getSerializable("selected_color")) != null;
            this.mSelectedColor = (Integer) bundle.getSerializable("selected_color");
            this.mText = bundle.getString("name");*/
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle bundle) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null);
        this.mPalette = ((ColorPickerPalette) view.findViewById(R.id.color_picker));
        TextView mTitle = ((TextView) view.findViewById(R.id.title));
        this.mButtonPalette = ((ColorPickerPaletteB) view.findViewById(R.id.botones));
        this.mEditText = ((EditText) view.findViewById(R.id.bookmark_name));
        this.mPalette.init(this.mSize, this.mColumns, this);

        mTitle.setText(this.mTitleResId);
        this.mButtonPalette.init(this, this);

        if (this.mColors != null) {
            showPaletteView();
        }
        mEditText.setText(mName);
        AlertDialog.Builder dialogo = new AlertDialog.Builder(getActivity()).setView(view);
        mAlertDialog = dialogo.create();

        return this.mAlertDialog;
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putIntArray("colors", this.mColors);
        bundle.putSerializable("selected_color", this.mSelectedColor);
        bundle.putString("name", mEditText.getText().toString());
    }

    private void setArguments(int titleId, int columns, int size, String name) {
        Bundle bundle = new Bundle();
        bundle.putInt("title_id", titleId);
        bundle.putInt("columns", columns);
        bundle.putInt("size", size);
        bundle.putString("name", name);
        setArguments(bundle);
    }

    private void setColors(int[] colors, int selected) {
        if ((this.mColors != colors) || (this.mSelectedColor != selected)) {
            this.mColors = colors;
            this.mSelectedColor = selected;
            refreshPalette();
        }
    }

    public void setOnColorSelectedListener(ColorPickerSwatch.OnColorSelectedListener onColorSelectedListener) {
        this.mListener = onColorSelectedListener;
    }

    private void showPaletteView() {
        if (this.mPalette != null) {
            refreshPalette();
            this.mPalette.setVisibility(View.VISIBLE);
        }
    }

    public void setOnButtonClickListener(ColorPickerButton.OnButtonClickListener onButtonClickListener) {
        this.mListenerButton = onButtonClickListener;
    }

    public void setOnCancelButtonClick(ColorPickerButton.OnButton_Cancel_ClickListener onButton_Cancel_Click) {
        this.mListenerCancel = onButton_Cancel_Click;
    }

    @Override
    public void onButtonClick(String text) {
        if (this.mListenerButton != null) {
            mText = mEditText.getText().toString();
            this.mListenerButton.onButtonClick(mText);
        }
        if ((getTargetFragment() instanceof ColorPickerButton.OnButtonClickListener)) {
            mText = mEditText.getText().toString();
            ((ColorPickerButton.OnButtonClickListener) getTargetFragment()).onButtonClick(mText);
        }
    }

    @Override
    public void onButton_Cancel_Click(boolean save) {
        if (this.mListenerCancel != null)
            this.mListenerCancel.onButton_Cancel_Click(save);
        if ((getTargetFragment() instanceof ColorPickerButton.OnButtonClickListener))
            ((ColorPickerButton.OnButton_Cancel_ClickListener) getTargetFragment()).onButton_Cancel_Click(save);
    }

}