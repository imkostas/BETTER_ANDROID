package com.astapley.thememe.better;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class CustomCardView extends CardView {
    private int position;
    private InViewListener inViewListener;

    public CustomCardView(Context context) { super(context); }
    public CustomCardView(Context context, AttributeSet attrs) { super(context, attrs); }
    public CustomCardView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public interface InViewListener { void onViewEnter(int position); }
    public void setInViewListener(InViewListener inViewListener) { this.inViewListener = inViewListener; }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();

        inViewListener.onViewEnter(position);
    }

    public void setPosition(int position){ this.position = position; }
    public int getPosition(){ return position; }
}
