package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by huangkaijie on 16/8/20.
 */
public class FlowLayout extends ViewGroup {
    @IntDef({HORIZONTAL,VERTICAL})
    public @interface OrientationMode{}

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    private static final int BAND_WIDTH_NOT_FIXED = -1;

    private int mOrientation = HORIZONTAL;
    private boolean mReverse = false;
    
    private int mBandWidth = BAND_WIDTH_NOT_FIXED;

    private Drawable mDivider;
    private Drawable mGap;//space between views

    private int mDividerWidth;
    private int mDividerHeight;
    private int mGapWidth;
    private int mGapHeight;

    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr,0);
    }

    @TargetApi(21)
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        // TODO: 16/8/20
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: 16/8/20  
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO: 16/8/20
    }
    
    public final boolean isBandWidthFixed(){
        return mBandWidth != BAND_WIDTH_NOT_FIXED;
    }
    
    public void setBandWidth(int bandWidth){
        if(bandWidth <= 0)
            bandWidth = BAND_WIDTH_NOT_FIXED;
        if(bandWidth != mBandWidth){
            mBandWidth = bandWidth;
            requestLayout();
        }
    }

    public void setOrientation(@OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            requestLayout();
        }
    }
    
    public void setReverse(boolean reverse){
        if(mReverse != reverse){
            mReverse = reverse;
            requestLayout();
        }
    }

    public void setDividerDrawable(Drawable divider) {
        if (divider == mDivider) {
            return;
        }
        mDivider = divider;
        if (divider != null) {
            mDividerWidth = divider.getIntrinsicWidth();
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerWidth = 0;
            mDividerHeight = 0;
        }
        setWillNotDraw(mDivider == null && mGap == null);
        requestLayout();
    }
    
    public void setGapDrawable(Drawable gap){
        if (gap == mGap) {
            return;
        }
        mGap = gap;
        if (gap != null) {
            mGapWidth = gap.getIntrinsicWidth();
            mGapHeight = gap.getIntrinsicHeight();
        } else {
            mGapWidth = 0;
            mGapHeight = 0;
        }
        setWillNotDraw(mDivider == null && mGap == null);
        requestLayout();
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private boolean mCR = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            //// TODO: 16/8/20
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source){
            super(source);

            //// TODO: 16/8/20
        }
    }
}
