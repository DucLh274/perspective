package com.wwdablu.soumya.extimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.locks.ReentrantLock;

final class BitmapStorage {

    private File mOriginalFile;
    private ReentrantLock mGuard;

    BitmapStorage(@NonNull Context context, @NonNull String id) {
        mOriginalFile = new File(context.getCacheDir(), id + "_" + "o.jpg");
        mGuard = new ReentrantLock();
    }

    void saveOriginalBitmap(@NonNull Bitmap bitmap, @Nullable ResultCallBack<Void> resultCallBack) {

        new Thread(() -> {

            mGuard.lock();
            try (FileOutputStream fos = new FileOutputStream(mOriginalFile)) {

                /* Switched from PNG to JPEG as PNG compression takes long time */
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                if(resultCallBack != null) {
                    resultCallBack.onComplete(null);
                }

            } catch (Exception ex) {
                if(resultCallBack != null) {
                    resultCallBack.onError(ex);
                }
            } finally {
                mGuard.unlock();
            }
        }).start();
    }

    void getOriginalBitmap(@NonNull ResultCallBack<Bitmap> resultCallBack) {

        new Thread(() -> {

            mGuard.lock();
            try {
                resultCallBack.onComplete(getOriginalBitmap());
            } catch (Exception ex) {
                resultCallBack.onError(ex);
            } finally {
                mGuard.unlock();
            }
        }).start();
    }

    private Bitmap getOriginalBitmap() throws Exception {

        if(!isOriginalBitmapPresent()) {
            return null;
        }

        Bitmap bitmap;
        try (FileInputStream fis = new FileInputStream(mOriginalFile)) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDensity = 0;
            options.inScaled = false;
            options.inTargetDensity = 0;
            bitmap = BitmapFactory.decodeStream(fis, null, options);

        }

        return bitmap;
    }

    boolean isOriginalBitmapPresent() {

        return mOriginalFile.exists();
    }

    void deleteOriginalBitmap() {

        if(mOriginalFile.exists()) {
            mOriginalFile.delete();
        }
    }
}
