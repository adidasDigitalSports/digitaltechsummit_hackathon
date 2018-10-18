package com.adidas.hackathon.smartjacket.ui.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.adidas.hackathon.smartjacket.util.UIUtils;

public class RoundRectangleDrawable extends Drawable {
    private Paint fillPaint;
    private Paint strokePaint;
    private float cornerRadius;
    private float halfAStroke;

    public RoundRectangleDrawable(int fillColor, int strokeColor, float strokeWidth, float cornerRadius) {
        init(fillColor, strokeColor, strokeWidth, cornerRadius);
    }

    private void init(@ColorInt int fillColor, @ColorInt int strokeColor, float strokeWidth, float cornerRadius) {

        this.halfAStroke = strokeWidth * 0.5f;
        this.cornerRadius = cornerRadius;

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeWidth);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {

        Rect originalRect = getBounds();

        if (!UIUtils.isColorTransparent(fillPaint.getColor())) {
            RectF fillRect = new RectF(originalRect);
            canvas.drawRoundRect(fillRect, cornerRadius, cornerRadius, fillPaint);
        }

        if (!UIUtils.isColorTransparent(strokePaint.getColor())) {

            RectF strokeRect = new RectF();
            // Paint.Style.Stroke requires offsets for strokes!
            strokeRect.left = originalRect.left + halfAStroke;
            strokeRect.top = originalRect.top + halfAStroke;
            strokeRect.right = originalRect.right - halfAStroke;
            strokeRect.bottom = originalRect.bottom - halfAStroke;

            canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint);
        }
    }

    public void setFillColor(@ColorInt int color) {
        fillPaint.setColor(color);
        invalidateSelf();
    }

    public void setStrokeColor(@ColorInt int color) {
        strokePaint.setColor(color);
        invalidateSelf();
    }

    public void setStrokeWidth(float width) {
        this.halfAStroke = width * 0.5f;
        strokePaint.setStrokeWidth(width);
    }

    public int getFillColor() {
        return fillPaint.getColor();
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        int fillColor = fillPaint.getColor();
        int ret;
        if (UIUtils.isColorTransparent(fillColor)) {
            int alpha = Color.alpha(strokePaint.getColor());
            ret = alpha == 0 ? PixelFormat.TRANSPARENT : alpha == 0xFF ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        } else {
            int alpha = Color.alpha(fillColor);
            ret = alpha == 0 ? PixelFormat.TRANSPARENT : alpha == 0xFF ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
        }
        return ret;
    }

}
