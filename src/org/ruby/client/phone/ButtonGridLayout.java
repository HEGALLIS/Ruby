package org.ruby.client.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ButtonGridLayout extends ViewGroup {

    private final int mColumns = 3;
    private int mPaddingBottom = 0,mPaddingLeft = 0,mPaddingRight = 0,mPaddingTop = 0;
    
    public ButtonGridLayout(Context context) {
        super(context);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int y = mPaddingTop;
        final int rows = getRows();
        final View child0 = getChildAt(0);
        final int yInc = (getHeight() - mPaddingTop - mPaddingBottom) / rows;
        final int xInc = (getWidth() - mPaddingLeft - mPaddingRight) / mColumns;
        final int childWidth = child0.getMeasuredWidth();
        final int childHeight = child0.getMeasuredHeight();
        final int xOffset = (xInc - childWidth) / 2;
        final int yOffset = (yInc - childHeight) / 2;
        
        for (int row = 0; row < rows; row++) {
            int x = mPaddingLeft;
            for (int col = 0; col < mColumns; col++) {
                int cell = row * mColumns + col;
                if (cell >= getChildCount()) {
                    break;
                }
                View child = getChildAt(cell);
                child.layout(x + xOffset, y + yOffset, 
                        x + xOffset + childWidth, 
                        y + yOffset + childHeight);
                x += xInc;
            }
            y += yInc;
        }
    }

    private int getRows() {
        return (getChildCount() + mColumns - 1) / mColumns; 
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mPaddingLeft + mPaddingRight;
        int height = mPaddingTop + mPaddingBottom;
        
        // Measure the first child and get it's size
        View child = getChildAt(0);
        child.measure(MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED);
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        // Make sure the other children are measured as well, to initialize
        for (int i = 1; i < getChildCount(); i++) {
            getChildAt(0).measure(MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED);
        }
        // All cells are going to be the size of the first child
        width += mColumns * childWidth;
        height += getRows() * childHeight;
        
        width = resolveSize(width, widthMeasureSpec);
        height = resolveSize(height, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

}
