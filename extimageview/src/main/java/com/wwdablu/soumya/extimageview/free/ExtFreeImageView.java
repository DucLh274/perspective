package com.wwdablu.soumya.extimageview.free;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wwdablu.soumya.extimageview.BaseExtImageView;
import com.wwdablu.soumya.extimageview.ResultCallBack;
import com.wwdablu.soumya.extimageview.free.pointcollector.CPoint;
import com.wwdablu.soumya.extimageview.free.pointcollector.PointCollector;

import java.util.List;

public class ExtFreeImageView extends BaseExtImageView {

    private PointCollector<CPoint> mPointCollector;

    private Canvas mPointerCanvas;

    private Paint mBitmapPainter;
    private Paint mPointPainer;

    private Runnable mInvalidator;

    public ExtFreeImageView(Context context) {
        this(context, null);
    }

    public ExtFreeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtFreeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPointCollector = new PointCollector<>();

        mBitmapPainter = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPointPainer = new Paint();
        mPointPainer.setColor(Color.RED);
    }

    @Override
    public void crop(@Nullable ResultCallBack<Void> resultCallBack) {

        getOriginalBitmap(new ResultCallBack<Bitmap>() {
            @Override
            public void onComplete(Bitmap originalBitmap) {
                mExecutorService.execute(new OriginalBitmapCropper(originalBitmap, mDisplayedBitmap,
                        mPointCollector.asList(),
                    new ResultCallBack<Bitmap>() {
                        @Override
                        public void onComplete(Bitmap data) {

                            //Save the bitmap
                            saveOriginalBitmap(data, new ResultCallBack<Void>() {
                                @Override
                                public void onComplete(Void vooid) {
                                    cropDisplayBitmap(data, resultCallBack);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    if (resultCallBack != null) {
                                        resultCallBack.onError(throwable);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            if (resultCallBack != null) {
                                resultCallBack.onError(throwable);
                            }
                        }
                    }));
            }

            @Override
            public void onError(Throwable throwable) {
                if (resultCallBack != null) {
                    resultCallBack.onError(throwable);
                }
            }
        });
    }

    /**
     * Reset all the points that have been marked
     * @param resultCallBack
     */
    public void reset(@Nullable ResultCallBack<Void> resultCallBack) {

        if(mPointCollector == null || mPointCollector.isEmpty()) {
            if (resultCallBack != null) {
                resultCallBack.onComplete(null);
            }
            return;
        }

        mPointCollector.clear();
        getOriginalBitmap(new ResultCallBack<Bitmap>() {
            @Override
            public void onComplete(Bitmap data) {
                setImageBitmap(scaleToFit(data, getMeasuredWidth(), getMeasuredHeight()));
                data.recycle();
                if (resultCallBack != null) {
                    resultCallBack.onComplete(null);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (resultCallBack != null) {
                    resultCallBack.onError(throwable);
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawColor(Color.BLACK);

        if(mDisplayedBitmap == null || mDisplayedBitmap.isRecycled()) {
            return;
        }

        PointF coor = getImageContentStartCoordinate();
        canvas.drawBitmap(mDisplayedBitmap, coor.x, coor.y, mBitmapPainter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d("APP", event.getX() + " / " + event.getY());

        PointF coor = getImageContentStartCoordinate();
        CPoint point = new CPoint(event.getX() - coor.x, event.getY() - coor.y);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPointCollector.newEpoch();
                mPointCollector.addPoint(point);
                drawPointersOnBitmap(point);
                return true;

            case MotionEvent.ACTION_MOVE:
                mPointCollector.addPoint(point);
                drawPointersOnBitmap(point);
                return true;

            case MotionEvent.ACTION_UP:
                List<CPoint> points = mPointCollector.asList();
                for(CPoint p : points) {
                    drawPointersOnBitmap(p);
                }
                invalidate();
                return true;

        }

        return super.onTouchEvent(event);
    }

    private void drawPointersOnBitmap(CPoint point) {

        if(mDisplayedBitmap == null || mDisplayedBitmap.isRecycled()) {
            return;
        }

        if(point.isDrawn()) {
            return;
        }

        if(mPointerCanvas == null) {
            mPointerCanvas = new Canvas(mDisplayedBitmap);
        }

        mPointerCanvas.drawCircle(point.x, point.y, 10, mPointPainer);
        point.setDrawn();
        if(mInvalidator != null) {
            return;
        }

        mInvalidator = () -> {
            invalidate();
            mInvalidator = null;
        };
        postDelayed(mInvalidator, 100);
    }

    private void cropDisplayBitmap(@NonNull Bitmap originalBitmap, @Nullable ResultCallBack<Void> resultCallBack) {

        mExecutorService.execute(() -> {
            Bitmap bitmap = scaleToFit(originalBitmap, getMeasuredWidth(), getMaxHeight());
            originalBitmap.recycle();
            runOnUiThread(() -> {
                setImageBitmap(bitmap);
                if (resultCallBack != null) {
                    resultCallBack.onComplete(null);
                }
            });
        });
    }
}
