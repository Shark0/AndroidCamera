package com.shark.androidcamera;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Shark0 on 2016/5/30.
 */
public class BitmapUtil {
    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
}
