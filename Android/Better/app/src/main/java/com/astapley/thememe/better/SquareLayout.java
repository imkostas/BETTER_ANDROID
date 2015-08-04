package com.astapley.thememe.better;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquareLayout extends FrameLayout {
    public SquareLayout(Context context) { super(context); }

    public SquareLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    public SquareLayout(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec));
    }
}
