package com.alce.tus.Types;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.alce.tus.Activities.DetailActivity;
import com.alce.tus.Fragments.Home;
import com.alce.tus.R;
import com.fourmob.colorpicker.ColorPickerButton;
import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

/**
 * Color Picker Dialog.
 */
public class FavDialog {

    public static final int[] Colors = new int[]{
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
            0xFFCDDC39, 0xFF8BC34A, 0xFF4CAF50, 0xFF009688,
            0xFF9E9E9E, 0xFFFF9800, 0xFFFFC107, 0xFFFFEB3B,
    };

    public static int Color = 0;
    private static Context mContext;
    private static ColorPickerDialog colorPickerDialog;

    public static void favDialog_init(final Context ctx, FragmentManager fm, String mode,
                                      final Type_Fav element, final View mTintView,
                                      final TextView textView) {

        mContext = ctx;
        colorPickerDialog = new ColorPickerDialog();

        if (mode.equals("edit")) {

            colorPickerDialog.initialize(R.string.editFav, Colors,
                    element.getCustomName());
            colorPickerDialog.setOnButtonClickListener(new ColorPickerButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(String text) {

                    if (Color == 0)
                        Color = element.getCustomColor();

                    Type_Fav fav;
                    if (element.getType() != null) {
                        fav = new Type_Fav(
                                element.getNumber(),
                                element.getName(),
                                0,
                                text,
                                Color,
                                "bike",
                                element.getLat(),
                                element.getLng());
                    } else {
                        fav = new Type_Fav(
                                element.getNumber(),
                                element.getName(),
                                0,
                                text,
                                Color);
                    }

                    if (text.isEmpty()) {
                        text = element.getName();
                    }
                    textView.setText(text);

                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        DetailActivity.editFav(mContext, fav);
                        Home.Task task = new Home.Task();
                        task.execute(mContext);
                    } else {
                        DetailActivity.saveFav(mContext, fav);
                    }

                    colorPickerDialog.dismiss();
                }
            });

            colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                @Override
                public void onColorSelected(int color) {
                    Color = color;
                }
            });

            colorPickerDialog.setOnCancelButtonClick(new ColorPickerButton.OnButton_Cancel_ClickListener() {
                @Override
                public void onButton_Cancel_Click(boolean save) {
                    colorPickerDialog.dismiss();
                }
            });
        } else {
            colorPickerDialog.initialize(R.string.newFav, Colors, "");
            colorPickerDialog.setOnButtonClickListener(new ColorPickerButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(String text) {

                    if (Color == 0) {
                        Random r = new Random();
                        int randomNumber = r.nextInt(Colors.length - 1);
                        Color = Colors[randomNumber];
                    }

                    Type_Fav fav;
                    if (element.getType() != null) {
                        fav = new Type_Fav(
                                element.getNumber(),
                                element.getName(),
                                0,
                                text,
                                Color,
                                "bike",
                                element.getLat(),
                                element.getLng());
                    } else {
                        fav = new Type_Fav(
                                element.getNumber(),
                                element.getName(),
                                0,
                                text,
                                Color);
                    }

                    if (text.isEmpty()) {
                        text = element.getName();
                    }
                    textView.setText(text);
                    DetailActivity.saveFav(mContext, fav);
                    DetailActivity.isFav = true;
                    snackButton(mContext.getString(R.string.addedFav));
                    colorPickerDialog.dismiss();
                }
            });

            colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

                @Override
                public void onColorSelected(int color) {
                    Color = color;
                    mTintView.setBackgroundColor(color);
                }
            });

            colorPickerDialog.setOnCancelButtonClick(new ColorPickerButton.OnButton_Cancel_ClickListener() {
                @Override
                public void onButton_Cancel_Click(boolean save) {
                    colorPickerDialog.dismiss();
                    if (element.getType() != null) {
                        mTintView.setBackgroundColor(mContext.
                                getResources().getColor(R.color.color_tab_3));
                    } else {
                        mTintView.setBackgroundColor(mContext.
                                getResources().getColor(R.color.color_tab_2));
                    }
                    textView.setText(element.getName());
                }
            });
        }
        colorPickerDialog.show(fm, "colorpicker");
    }

    private static void snackButton(String msg) {
        DetailActivity detailActivity = new DetailActivity();
        detailActivity.snackButton(R.string.undo, mContext, msg,
                mContext.getString(R.string.delete), DetailActivity.View);
    }

    public static boolean isOpen() {
        return colorPickerDialog != null && colorPickerDialog.isAdded();
    }
}