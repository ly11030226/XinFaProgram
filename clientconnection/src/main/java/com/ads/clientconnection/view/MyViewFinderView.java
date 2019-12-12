package com.ads.clientconnection.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.ads.clientconnection.R;
import com.ads.clientconnection.utils.BaseUtils;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * 自定义的显示扫描线和矩形框
 */
public class MyViewFinderView extends ViewfinderView {

    public static final int SCAN_VELOCITY = 5;
    public static final int BORDER_LENGTH = 68;
    public static final long DELAY = 10L;
    public static final int DISTANCE_FRAME_Y = 50;
    public static final int DISTANCE_FRAME_X = 30;

    //扫描线top值，用来上下移动
    private int lineTop = 0;
    public MyViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }

        final Rect frame = framingRect;
        final Rect previewFrame = previewFramingRect;

        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

            // Draw a red "laser scanner" line through the middle to show decoding is active

            int lineColor = ContextCompat.getColor(getContext(), R.color.zxing_line_color);
            paint.setColor(lineColor);
//            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            final int middle = frame.height() / 2 + frame.top;
            //通过更改密度值达到一闪一闪的效果
//            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);

            //扫描线仿微信移动上下移动的效果
            if (lineTop == 0) {
                lineTop = frame.top +DISTANCE_FRAME_Y;
            }
            if (lineTop >= frame.bottom - DISTANCE_FRAME_Y) {
                lineTop = frame.top + DISTANCE_FRAME_Y;
            }
            if (lineTop < frame.bottom - DISTANCE_FRAME_Y) {
                lineTop+=SCAN_VELOCITY;
            }
            Rect rect = new Rect(frame.left + DISTANCE_FRAME_X,lineTop,frame.right - DISTANCE_FRAME_X,lineTop+5);
            canvas.drawRect(rect,paint);

            //绘制四角的绿色边框
            paint.setStrokeWidth(10);
            //左上
            canvas.drawLine(frame.left,frame.top,frame.left+BORDER_LENGTH,frame.top,paint);
            canvas.drawLine(frame.left,frame.top,frame.left,frame.top+BORDER_LENGTH,paint);
            //右上
            canvas.drawLine(frame.right - BORDER_LENGTH,frame.top,frame.right,frame.top,paint);
            canvas.drawLine(frame.right,frame.top,frame.right,frame.top+BORDER_LENGTH,paint);
            //左下
            canvas.drawLine(frame.left,frame.bottom - BORDER_LENGTH,frame.left,frame.bottom,paint);
            canvas.drawLine(frame.left,frame.bottom,frame.left+BORDER_LENGTH,frame.bottom,paint);
            //右下
            canvas.drawLine(frame.right - BORDER_LENGTH,frame.bottom,frame.right,frame.bottom,paint);
            canvas.drawLine(frame.right,frame.bottom - BORDER_LENGTH,frame.right,frame.bottom,paint);

            //绘制提示语
            Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textpaint.setTextSize(BaseUtils.spToPx(getContext(),13));
            textpaint.setColor(ContextCompat.getColor(getContext(),R.color.white));
            String content = BaseUtils.getStringByResouceId(R.string.qrcode_text);
            Rect textRect = new Rect();
            textpaint.getTextBounds(content,0,content.length(),textRect);
            float x = (frame.left+((frame.right - frame.left)/2)) - textRect.width()/2;
            float y = frame.bottom + textRect.height() + 50;
            canvas.drawText(content,x,y,textpaint);


            final float scaleX = frame.width() / (float) previewFrame.width();
            final float scaleY = frame.height() / (float) previewFrame.height();

            final int frameLeft = frame.left;
            final int frameTop = frame.top;

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint
                    );
                }
                lastPossibleResultPoints.clear();
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (final ResultPoint point : possibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint
                    );
                }

                // swap and clear buffers
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }
}
