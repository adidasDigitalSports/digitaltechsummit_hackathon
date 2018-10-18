package com.adidas.hackathon.smartjacket.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

public class DividerDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;

    public DividerDecoration(Context context, @ColorInt int dividerColor) {
        this.paint = new Paint();
        paint.setColor(dividerColor);
        paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics()));
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int xLeft = parent.getPaddingLeft();
        int xRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int y = (int) (top + (paint.getStrokeWidth() / 2));

            c.drawLine(xLeft, y, xRight, y, paint);
        }
    }

}
