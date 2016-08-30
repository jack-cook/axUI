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
        measureHorizontal(widthMeasureSpec,heightMeasureSpec);
        // TODO: 16/8/30 measureVertical
    }

    void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        boolean fixedBandWidth = mBandWidth != BAND_WIDTH_NOT_FIXED;

        final int CONTENT_OCCUPIED_HORIZONTAL = getContentOccupied(HORIZONTAL);
        final int CONTENT_OCCUPIED_VERTICAL = getContentOccupied(VERTICAL);
        final int CONTENT_WIDTH = Math.max(0,widthSize - CONTENT_OCCUPIED_HORIZONTAL);
        final int CONTENT_HEIGHT = Math.max(0,heightSize - CONTENT_OCCUPIED_VERTICAL);

        Band band = null;
        int bandWidth = 0;//带宽,用来计算高度
        int preBandWidth = bandWidth;
        int bandLength = 0;
        int preBandLength = bandLength;
        int tempBandLength = 0;
        int maxBandLength = 0;//最长band,用来计算宽度
        int contentHeight = 0;//用来计算高度
        boolean newBand = true;
        int childCount = getChildCount();
        for(int i = 0; i < childCount - 1; ++i){
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                continue;//当它不存在,应该不会有影响
            }
            
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int wMeasureSpec;
            int hMeasureSpec;
            boolean reMeasure = false;

            /*
            *计算高度限制。
             */
            if(fixedBandWidth){
                        /*
                        用带宽去限制child的高度
                         */

                if(lp.height == LayoutParams.MATCH_PARENT){
                    hMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMeasureSize(mBandWidth - lp.topMargin - lp.bottomMargin),MeasureSpec.EXACTLY);
                }else{
                    hMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMeasureSize(mBandWidth - lp.topMargin - lp.bottomMargin),MeasureSpec.AT_MOST);
                }
            }else {
                        /*
                        用父试图的高度限制
                         */
                if(heightMode == MeasureSpec.UNSPECIFIED){
                    hMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
                }else {
                    hMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMeasureSize(CONTENT_HEIGHT - lp.topMargin - lp.bottomMargin), heightMode);// TODO: 16/8/27 ok?
                }
            }


            /*
            *计算宽度限制并测量,有可能要另起一行
             */
            if(widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST){
                tempBandLength = bandLength + lp.leftMargin + lp.rightMargin + mGapWidth;
                if(!newBand && CONTENT_WIDTH < tempBandLength){
                    //因为margin而放不下,需要另起一行。
                    
                    newBand = true;
                    preBandLength = bandLength;
                    bandLength = 0;
                    tempBandLength = bandLength + lp.leftMargin + lp.rightMargin + mGapWidth;
                }

                if(lp.width == LayoutParams.MATCH_PARENT){
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMeasureSize(CONTENT_WIDTH - tempBandLength),MeasureSpec.EXACTLY);
                }else {
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
                }

                child.measure(wMeasureSpec,hMeasureSpec);

                if(!newBand && bandLength + child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin > CONTENT_WIDTH){//需要另起一行,重新测量
                    reMeasure = true;
                    newBand = true;
                    preBandLength = bandLength;
                    bandLength = 0;
                    preBandWidth = bandWidth;
                    bandWidth = 0;
                }else {
                    bandLength += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    bandWidth = Math.max(bandWidth,child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                }

            }else{// widthMode == MeasureSpec.UNSPECIFIED
                wMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
                child.measure(wMeasureSpec,hMeasureSpec);
            }/*else {// AT_MOST

            }*/


            if(reMeasure){//另起一行,需要重新测量
                wMeasureSpec = MeasureSpec.makeMeasureSpec(CONTENT_WIDTH,widthMode);
                child.measure(wMeasureSpec,hMeasureSpec);
                bandWidth = Math.max(bandWidth,child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }

            if(newBand){
                if(band != null){//set last band max width
                    band.setBandWidth(preBandWidth);
                }

                bandLength = 0;
                band = new Band();
                band.setStartIndex(i);
                mBands.add(band);
                newBand = false;

                maxBandLength = Math.max(preBandLength,maxBandLength);
                contentHeight += preBandWidth;
            }
        }

        //last band
        if(band != null){
            band.setBandWidth(bandWidth);
        }

        int bandCount = mBands.size();
        maxBandLength = Math.max(bandLength,maxBandLength);
        contentHeight += bandWidth;
        if(bandCount > 0){
            contentHeight += (bandCount -1) * mDividerWidth;
        }
        width = maxBandLength + CONTENT_WIDTH;
        height = contentHeight + CONTENT_HEIGHT;
        width = Math.max(width,getSuggestedMinimumWidth());
        height = Math.max(height,getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSize(width,widthMeasureSpec),resolveSize(height,heightMeasureSpec));

        /*
        *计算宽高,并计算带宽
         *//*
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
        }*/
        
    }

    private int getContentOccupied(@OrientationMode int orientation){
        int occupied = 0;
        if(orientation == HORIZONTAL){
            occupied += getPaddingLeft() + getPaddingRight();
            if(mOrientation == HORIZONTAL){
                int edgeGapCount = 0;
                // TODO: 16/8/27 count edge gap
                return occupied += mGapWidth * edgeGapCount;
            }else {
                return occupied;
            }
        }else {
            occupied += getPaddingTop() + getPaddingBottom();
            if(mOrientation == VERTICAL){
                int edgeGapCount = 0;
                // TODO: 16/8/27 count edge gap
                return occupied += mGapWidth * edgeGapCount;
            }else {
                return occupied;
            }
        }

    }

    private int getChildMeasureSize(int sizeLeft){
        return Math.max(0,sizeLeft);
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
        private int mBandContentMaxWidth;
        private int mStartIndex;//start child index

        public int getBandWidth() {
            return mBandContentMaxWidth;
        }

        public void setBandWidth(int bandContentMaxWidth) {
            this.mBandContentMaxWidth = bandContentMaxWidth;
        }

        public int getStartIndex() {
            return mStartIndex;
        }

        public void setStartIndex(int startIndex) {
            this.mStartIndex = startIndex;
        }
    }
}
