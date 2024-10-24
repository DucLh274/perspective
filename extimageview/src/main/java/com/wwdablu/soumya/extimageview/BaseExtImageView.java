package com.wwdablu.soumya.extimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseExtImageView extends AppCompatImageView {

    protected static final String TAG = "ExtImageView";

    public enum Rotate {
        CW_90(90),
        CW_180(180),
        CW_270(270),
        CCW_90(-90),
        CCW_180(-180),
        CCW_270(-270);

        private int value;
        Rotate(final int value) {
            this.value = value;
        }
    }

    private Matrix mMatrix;
    private BitmapStorage mStorage;
    private boolean mIsDisplayBitmapReady;

    protected Bitmap mDisplayedBitmap;

    protected ExecutorService mExecutorService;
    private Handler mUIHandler;

    public abstract void crop(@Nullable ResultCallBack<Void> resultCallBack);

    public BaseExtImageView(Context context) {
        this(context, null, 0);
    }

    public BaseExtImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseExtImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMatrix = new Matrix();
        mExecutorService = Executors.newSingleThreadExecutor();
        mIsDisplayBitmapReady = false;
        mUIHandler = new Handler(Looper.getMainLooper());
        mStorage = new BitmapStorage(context, "uid_" + System.currentTimeMillis());
    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        if(!mIsDisplayBitmapReady) {
            mDisplayedBitmap = bm;
            if(!mStorage.isOriginalBitmapPresent()) {
                mStorage.saveOriginalBitmap(mDisplayedBitmap, null);
            }
            return;
        }

        super.setImageBitmap(bm);

        if(bm.equals(mDisplayedBitmap)) {
            return;
        }

        if(mDisplayedBitmap != null && !mDisplayedBitmap.isRecycled()) {
            mDisplayedBitmap.recycle();
        }

        mDisplayedBitmap = bm;
    }

    /**
     * Returns a mutable copy of the bitmap that is being displayed on screen to the device. Note,
     * that the caller has the responsibility to recycle() the bitmap once it has been used.
     * @param resultCallBack Returns the bitmap or the exception generated
     */
    public final void getCroppedBitmap(@NonNull ResultCallBack<Bitmap> resultCallBack) {

        if(mStorage == null) {
            resultCallBack.onError(new IllegalStateException("Invalid object"));
            return;
        }

        mStorage.getOriginalBitmap(resultCallBack);
    }

    /**
     * Rotate the bitmap by the specified option.
     * @see Rotate
     * @param by Rotation value
     * @param resultCallBack Notified when rotate is completed
     */
    public void rotate(Rotate by, @NonNull ResultCallBack<Void> resultCallBack) {
        saveOriginalBitmapRotation(by, resultCallBack);
    }

    protected final void getOriginalBitmap(@NonNull ResultCallBack<Bitmap> resultCallBack) {
        mStorage.getOriginalBitmap(resultCallBack);
    }

    protected final void saveOriginalBitmap(@NonNull Bitmap bitmap, @NonNull ResultCallBack<Void> resultCallBack) {
        mStorage.saveOriginalBitmap(bitmap, resultCallBack);
    }

    @Override
    @CallSuper
    protected void onFinishInflate() {

        super.onFinishInflate();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if(mDisplayedBitmap == null || mDisplayedBitmap.isRecycled() || getVisibility() == View.GONE) {
                    return;
                }

                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Bitmap scaled = scaleToFit(mDisplayedBitmap, getMeasuredWidth(), getMeasuredHeight());
                mDisplayedBitmap.recycle();
                mDisplayedBitmap = scaled;
                mIsDisplayBitmapReady = true;
                setImageBitmap(mDisplayedBitmap);
            }
        });
    }

    @Override
    @CallSuper
    protected void onDetachedFromWindow() {

        if(!mExecutorService.isShutdown()) {
            mExecutorService.shutdownNow();
        }

        if(mDisplayedBitmap != null && !mDisplayedBitmap.isRecycled()) {
            mDisplayedBitmap.recycle();
            mDisplayedBitmap = null;
        }

        mStorage.deleteOriginalBitmap();
        super.onDetachedFromWindow();
    }

    protected final void runOnUiThread(@NonNull Runnable runnable) {
        mUIHandler.post(runnable);
    }

    public final Bitmap scaleToFit(Bitmap bitmap, int toWidth, int toHeight) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float useFactor = getScaleFactor(bitmap, toWidth, toHeight);

        int scaleWidth = (int) (originalWidth / useFactor);
        int scaleHeight = (int) (originalHeight / useFactor);

        return Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
    }

    protected final float getScaleFactor(Bitmap bitmap, int toWidth, int toHeight) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        float heightFactor = (float) originalWidth / (float) toWidth;
        float widthFactor = (float) originalHeight / (float) toHeight;

        return (widthFactor >= heightFactor) ? widthFactor : heightFactor;
    }

    protected final float getDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.density;
    }

    protected final PointF getImageContentStartCoordinate() {

        int idWidth = mDisplayedBitmap.getWidth();
        int idHeight = mDisplayedBitmap.getHeight();

        float left = 0;
        float top = 0;

        if(idWidth == getMeasuredWidth()) {
            top = (getMeasuredHeight() - idHeight) >> 1;
        } else {
            left = (getMeasuredWidth() - idWidth) >> 1;
        }

        return new PointF(left, top);
    }

    private void saveOriginalBitmapRotation(Rotate by, ResultCallBack<Void> resultCallBack) {

        mStorage.getOriginalBitmap(new ResultCallBack<Bitmap>() {
            @Override
            public void onComplete(Bitmap bitmap) {
                mExecutorService.execute(() -> {
                    try {
                        if(bitmap == null) {
                            return;
                        }

                        mMatrix.reset();
                        mMatrix.preRotate(by.value);

                        Bitmap originalRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                bitmap.getHeight(), mMatrix, true);

                        bitmap.recycle();

                        mStorage.saveOriginalBitmap(originalRotated, new ResultCallBack<Void>() {
                            @Override
                            public void onComplete(Void data) {

                                Bitmap bm = scaleToFit(originalRotated, getMeasuredWidth(), getMeasuredHeight());
                                runOnUiThread(() -> setImageBitmap(bm));
                                resultCallBack.onComplete(null);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG, "Rotation failed internally. Output may be incorrect.");
                                resultCallBack.onError(throwable);
                            }
                        });

                    } catch (Exception ex) {
                        Log.e(TAG, "Rotation failed internally. Output may be incorrect.");
                        resultCallBack.onError(ex);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Could not retrieve the image. " + throwable.getMessage());
                resultCallBack.onError(throwable);
            }
        });
    }
}
