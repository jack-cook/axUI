package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaijie on 16/8/20.
 */
public class FlowLayout extends ViewGroup {
    @IntDef({HORIZONTAL,VERTICAL})
    public @interface OrientationMode{}

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    private static final int BAND_WIDTH_NOT_FIXED = 0;

    private int mOrientation = HORIZONTAL;
    private int mGravity = Gravity.START | Gravity.TOP;

    private int mBandWidth = BAND_WIDTH_NOT_FIXED;

    private Drawable mDivider;
    private Drawable mGap;//space between views

    private int mDividerWidth;
    private int mDividerHeight;
    private int mGapWidth;
    private int mGapHeight;

    private List<Band> mBands = new ArrayList<>();

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
        mBands.clear();



        int width = 0;
        int height = 0;

        int flowLength = 0;//flow orientation dimension
        int flowWidth = 0;


        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int flowLengthMode;
        final int flowLengthSize;
        final int flowWidthMode;
        final int flowWidthSize;

        if(mOrientation == HORIZONTAL){
            flowLengthMode = widthMode;
            flowLengthSize = widthSize;
            flowWidthMode = heightMode;
            flowWidthSize = heightSize;
        }else {
            flowLengthMode = heightMode;
            flowLengthSize = heightSize;
            flowWidthMode = widthMode;
            flowWidthSize = widthSize;
        }

        switch (flowLengthMode){
            case MeasureSpec.EXACTLY:
                flowLength = flowLengthSize;
                break;
        }

        switch (flowWidthMode){
            case MeasureSpec.EXACTLY:
                flowWidth = flowLengthSize;
                break;
        }

        Band band;
        int bandWidth = 0;
        int bandLength = 0;
        boolean newBand = true;
        int childCount = getChildCount();
        for(int i = 0; i < childCount; ++i){
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if(newBand){
                bandLength = 0;
                band = new Band();
                band.setStartIndex(0);
                if(mBandWidth != BAND_WIDTH_NOT_FIXED)
                    band.setBandWidth(mBandWidth);
                mBands.add(band);
                newBand = false;
            }
            band = mBands.get(mBands.size() - 1);

            if(flowLengthMode == MeasureSpec.EXACTLY){
                int preBandLength = bandLength;
                bandLength +=
            }
        }



        // TODO: 16/8/20
    }

    void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        boolean fixedBandWidth = mBandWidth != BAND_WIDTH_NOT_FIXED;

        Band band;
        int bandWidth = 0;
        int bandLength = 0;
        int preBandLength = bandLength;
        int tempBandLength = 0;
        boolean newBand = true;
        int childCount = getChildCount();
        for(int i = 0; i < childCount - 1; ++i){
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                continue;//当它不存在,应该不会有影响
            }
            
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if(widthMode == MeasureSpec.EXACTLY){
                if(!newBand && widthSize < bandLength + lp.leftMargin + lp.rightMargin){
                    //因为margin而放不下,需要另起一行。
                    
                    newBand = true;
                    preBandLength = bandLength;
                    bandLength = 0;
                }

                tempBandLength += lp.leftMargin + lp.rightMargin;
                int wMeasureSpec;
                if(lp.width == LayoutParams.MATCH_PARENT){
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - tempBandLength,MeasureSpec.EXACTLY);
                }else {
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize - tempBandLength,MeasureSpec.AT_MOST);
                }
                int hMeasureSpec;
                if(fixedBandWidth){
                        /*
                        用带宽去限制child的高度
                         */

                    if(lp.height == LayoutParams.MATCH_PARENT){
                        hMeasureSpec = MeasureSpec.makeMeasureSpec(mBandWidth - lp.topMargin - lp.bottomMargin,MeasureSpec.EXACTLY);
                    }else{
                        hMeasureSpec = MeasureSpec.makeMeasureSpec(mBandWidth - lp.topMargin - lp.bottomMargin,MeasureSpec.AT_MOST);
                    }
                }else {
                        /*
                        用父试图的高度限制
                         */

                    hMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin,heightMode);
                }
                child.measure(wMeasureSpec,hMeasureSpec);
                
                boolean reMeasure = false;
                if(!newBand && bandLength + child.getMeasuredWidth() > widthSize){
                    reMeasure = true;
                    newBand = true;
                    preBandLength = bandLength;
                    bandLength = 0;
                }
                
                if(reMeasure){
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize,MeasureSpec.EXACTLY);
                    child.measure(wMeasureSpec,hMeasureSpec);
                }
            }else if(widthMode == MeasureSpec.UNSPECIFIED){
                // TODO: 16/8/21  
            }else {
                // TODO: 16/8/21  
            }
            
            
            if(newBand){
                bandLength = 0;
                band = new Band();
                band.setStartIndex(i);
                mBands.add(band);
                newBand = false;
            }
        }
        
        
        
        /*
        *计算宽高,并计算带宽
         */
        int contentHeight = 0;
        int contentWidth = 0;
        int start = 0;
        int end = 0;
        band = null;
        for(int i = 0; i < mBands.size() - 1; ++i){
            band = mBands.get(i);
            start = band.getStartIndex();
            if(i < mBands.size() - 1){
                end = mBands.get(i + 1).getStartIndex();
            }else {
                end = getChildCount() - 1;
            }

            int maxChildHeight = 0;
            bandLength = 0;
            for(int childIndex = start; childCount <= end; ++childCount){
                View child = getChildAt(childIndex);
                if(child.getVisibility() == GONE)
                    continue;
                maxChildHeight = Math.max(maxChildHeight,child.getMeasuredHeight());
                bandLength += child.getMeasuredWidth();
            }
            band.setBandWidth(maxChildHeight);
            contentWidth = Math.max(contentWidth,bandLength);
            contentHeight += maxChildHeight;
        }
        
        if(widthMode == )// TODO: 16/8/21  
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

    public void setGravity(int gravity){
        if (mGravity != gravity) {
            if ((gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.START;
            }

            if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                gravity |= Gravity.TOP;
            }

            mGravity = gravity;
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

    private static class Band {
        private int mBandWidth;
        private int mStartIndex;//start child index

        public int getBandWidth() {
            return mBandWidth;
        }

        public void setBandWidth(int bandWidth) {
            this.mBandWidth = bandWidth;
        }

        public int getStartIndex() {
            return mStartIndex;
        }

        public void setStartIndex(int startIndex) {
            this.mStartIndex = startIndex;
        }
    }
}
