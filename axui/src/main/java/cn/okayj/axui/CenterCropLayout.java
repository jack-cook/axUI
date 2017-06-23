package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CenterCropLayout extends ViewGroup {
    public CenterCropLayout(Context context) {
        super(context);
    }

    public CenterCropLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CenterCropLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CenterCropLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);
            LayoutParams lp = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, 0, lp.width), getChildMeasureSpec(heightMeasureSpec, 0, lp.height));

            int childMeasuredWidth = child.getMeasuredWidth();
            int childMeasuredHeight = child.getMeasuredHeight();

            if (measuredHeight <= 0 || measuredWidth <= 0 || childMeasuredWidth <= 0 || childMeasuredHeight <= 0) {
                break;
            } else {
                boolean measureAgain = false;

                if (childMeasuredWidth * measuredHeight < measuredWidth * childMeasuredHeight) {
                    childMeasuredHeight = childMeasuredHeight * measuredWidth / childMeasuredWidth;
                    childMeasuredWidth = measuredWidth;
                    measureAgain = true;
                } else if (childMeasuredWidth * measuredHeight > measuredWidth * childMeasuredHeight) {
                    childMeasuredWidth = childMeasuredWidth * measuredHeight / childMeasuredHeight;
                    childMeasuredHeight = measuredHeight;
                    measureAgain = true;
                }

                if (measureAgain) {
                    child.measure(MeasureSpec.makeMeasureSpec(childMeasuredWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childMeasuredHeight, MeasureSpec.EXACTLY));
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int height = getHeight();
        int width = getWidth();

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);
            int childMeasuredWidth = child.getMeasuredWidth();
            int childMeasuredHeight = child.getMeasuredHeight();

            int childTop = (height - childMeasuredHeight) / 2;
            int childLeft = (width - childMeasuredWidth) / 2;
            child.layout(childLeft, childTop, childLeft + childMeasuredWidth, childTop + childMeasuredHeight);
        }
    }
}
