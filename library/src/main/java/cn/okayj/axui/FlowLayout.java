package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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

    /**
     * Don't show any dividers.
     */
    public static final int SHOW_DIVIDER_NONE = 0;
    /**
     * Show a divider at the beginning of the group.
     */
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    /**
     * Show dividers between each item in the group.
     */
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    /**
     * Show a divider at the end of the group.
     */
    public static final int SHOW_DIVIDER_END = 4;

    /**
     * Don't show any gaps.
     */
    public static final int SHOW_GAP_NONE = 0;
    /**
     * Show a gap at the beginning of the group.
     */
    public static final int SHOW_GAP_BEGINNING = 1;
    /**
     * Show gaps between each item in the group.
     */
    public static final int SHOW_GAP_MIDDLE = 2;
    /**
     * Show a gap at the end of the group.
     */
    public static final int SHOW_GAP_END = 4;

    private int mOrientation = HORIZONTAL;
    private int mGravity = Gravity.LEFT | Gravity.CENTER;

    private int mBandWidth = BAND_WIDTH_NOT_FIXED;

    private Drawable mDivider;
    private Drawable mGap;//space between views

    private int mShowDividers = SHOW_DIVIDER_NONE;
    private int mShowGaps = SHOW_GAP_NONE;

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
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.FlowLayout,defStyleAttr,defStyleRes);

        mGap = a.getDrawable(R.styleable.FlowLayout_gap);
        mShowGaps = a.getInt(R.styleable.FlowLayout_showGaps,SHOW_GAP_NONE);
//        mDivider = a.getDrawable(R.styleable.FlowLayout_android_divider);
//        mShowDividers = a.getIndex(R.styleable.FlowLayout_android_showDividers);
        mBandWidth = a.getInt(R.styleable.FlowLayout_bandWidth,BAND_WIDTH_NOT_FIXED);
//        mGravity = a.getInt(R.styleable.FlowLayout_android_gravity,mGravity);

        setGapDrawable(mGap);
        setDividerDrawable(mDivider);

        a.recycle();
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
        boolean newBand = true;//是否另起一行
        int childCount = getChildCount();
        for(int i = 0; i < childCount; ++i){
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                continue;//当它不存在,应该不会有影响
            }
            
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if(lp.mCarriageReturn)
                newBand = true;
            int wMeasureSpec;
            int hMeasureSpec;
            boolean reMeasure = false;

            /*
            *计算高度限制。todo 用measureChild measureChildWidthMargin
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
//                    preBandLength = bandLength;//todo 这里和后面可能有错误!!!!
//                    bandLength = 0;
//                    tempBandLength = bandLength + lp.leftMargin + lp.rightMargin + mGapWidth;
                }

                if(lp.width == LayoutParams.MATCH_PARENT){
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(getChildMeasureSize(CONTENT_WIDTH - tempBandLength),MeasureSpec.EXACTLY);
                }else {
                    wMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
                }

                child.measure(wMeasureSpec,hMeasureSpec);



            }else{// widthMode == MeasureSpec.UNSPECIFIED
                wMeasureSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
                child.measure(wMeasureSpec,hMeasureSpec);
            }/*else {// AT_MOST

            }*/

            if(!newBand && bandLength + child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin > CONTENT_WIDTH){//需要另起一行,重新测量
                reMeasure = true;
                newBand = true;
            }

            if(newBand){
                preBandLength = bandLength;
                preBandWidth = bandWidth;
                bandLength = 0;
                bandWidth = 0;
            }

            if(reMeasure){//另起一行,需要重新测量
                wMeasureSpec = MeasureSpec.makeMeasureSpec(CONTENT_WIDTH,widthMode);
                measureChild(child,wMeasureSpec,heightMeasureSpec);
            }

            bandLength += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            bandWidth = Math.max(bandWidth,child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

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
        width = maxBandLength + CONTENT_OCCUPIED_HORIZONTAL;
        height = contentHeight + CONTENT_OCCUPIED_VERTICAL;
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

    private int getChildGravity(View child){
        int childGravity = ((LayoutParams)child.getLayoutParams()).mGravity;
        if(childGravity < 0)
            childGravity = mGravity;
        return childGravity;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutHorizontal(l, t, r, b);
        // TODO: 16/8/20 layout vertical
    }

    /**
     * 水平排列,(暂未考虑从左排列还是从右排列,以后实现)
     * @param l
     * @param t
     * @param r
     * @param b
     */
    private void layoutHorizontal(int l, int t, int r, int b){
        int bandLeft = l + getPaddingLeft();
        int bandTop = t + getPaddingTop();

        if((mShowGaps & SHOW_GAP_BEGINNING) > 0){
            bandLeft += mGapWidth;
        }

        if((mShowDividers & SHOW_DIVIDER_BEGINNING) > 0){
            bandTop += mDividerWidth;
        }

        int childLeft = bandLeft;
        int childTop = bandTop;


        int childStart = 0;
        int childEnd = 0;
        int bandCount = mBands.size();
        for(int bandIndex = 0; bandIndex < bandCount ; ++bandIndex){
            Band band = mBands.get(bandIndex);
            band.setBandTop(bandTop);
            int bandWidth = band.getBandWidth();
            if(isBandWidthFixed()){
                bandWidth = mBandWidth;
            }

            childStart = childEnd;
            if(bandIndex < bandCount - 1){
                childEnd = mBands.get(bandIndex + 1).getStartIndex();
            }else {
                childEnd = getChildCount();
            }

            childLeft = bandLeft;

            for(int childIndex = childStart; childIndex < childEnd; ++childIndex){
                View child = getChildAt(childIndex);
                if(child.getVisibility() == GONE){
                    continue;
                }

                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                childLeft += lp.leftMargin;

                int childGravity = getChildGravity(child);
                switch (childGravity & Gravity.VERTICAL_GRAVITY_MASK){
                    case Gravity.CENTER_VERTICAL:
                        childTop = bandTop + (bandWidth - childHeight) / 2 ;
                        break;
                    case Gravity.TOP :
                        childTop = bandTop + lp.topMargin;
                        break;
                    default:
                        childTop = bandTop + bandWidth - childHeight - lp.bottomMargin;
                }

                child.layout(childLeft,childTop,childLeft + childWidth,childTop + childHeight);
                childLeft += child.getMeasuredWidth() + lp.rightMargin;
            }

            bandTop += bandWidth;
            if((mShowDividers & SHOW_DIVIDER_MIDDLE) > 0){
                bandTop += mDividerWidth;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mDivider == null && mGap == null)
            return;

        drawHorizontal(canvas);
        // TODO: 16/8/30 draw vertical
    }

    private void drawHorizontal(Canvas canvas){
        int dividerWidth = mDividerWidth;
        int dividerLeft = getPaddingLeft();
        int dividerRight = getWidth() - getPaddingRight();
        int dividerTop = 0;
        int dividerBottom = 0;

        int gapWidth = mGapWidth;
        int gapLeft = 0;
        int gapRight = 0;
        int gapTop = 0;
        int gapBottom = 0;

        int childStart = 0;
        int childEnd = 0;
        boolean showDividerBegin = (mShowDividers & SHOW_DIVIDER_BEGINNING) > 0;
        boolean showDividersMiddle = (mShowDividers & SHOW_DIVIDER_MIDDLE) > 0;
        boolean showDividerEnd = (mShowDividers & SHOW_DIVIDER_END) > 0;
        boolean showGapBegin = (mShowGaps & SHOW_GAP_BEGINNING) > 0;
        boolean showGapsMiddle = (mShowGaps & SHOW_DIVIDER_MIDDLE) > 0;
        boolean showGapsEnd = (mShowGaps & SHOW_DIVIDER_END) > 0;
        int bandCount = mBands.size();
        for(int bandIndex = 0; bandIndex < bandCount; ++bandCount){
            Band band = mBands.get(bandIndex);
            dividerBottom = band.getBandTop();
            dividerTop = dividerBottom + dividerWidth;

            if(0 == bandIndex){
                if(showDividerBegin) {
                    mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                    mDivider.draw(canvas);
                }
            }else {
                if(showDividersMiddle){
                    mDivider.setBounds(dividerLeft,dividerTop,dividerRight,dividerBottom);
                    mDivider.draw(canvas);
                }
            }

            //draw gaps
            gapLeft = getPaddingLeft();
            gapRight = gapLeft + gapWidth;
            gapTop = band.getBandTop();
            gapBottom = gapTop + (mBandWidth > 0 ? mBandWidth : band.getBandWidth());

            childStart = childEnd;
            if(bandIndex < bandCount - 1){
                childEnd = band.getStartIndex();
            }else {
                childEnd = getChildCount();
            }
            boolean firstVisibleInBand = true;
            for (int childIndex = childStart; childIndex < childEnd; ++childStart){
                View child = getChildAt(childIndex);
                if(child.getVisibility() == GONE)
                    continue;

                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                if(firstVisibleInBand){
                    firstVisibleInBand = false;
                    if(showGapBegin){
                        mGap.setBounds(gapLeft,gapTop,gapRight,gapBottom);
                        mGap.draw(canvas);
                    }
                }else {
                    if(showGapsMiddle) {
                        mGap.setBounds(gapLeft, gapTop, gapRight, gapBottom);
                        mGap.draw(canvas);
                    }
                }

                gapLeft = child.getRight() + lp.rightMargin;
            }
            //画band中最后的gap
            if(showGapsEnd){
                mGap.setBounds(gapLeft,gapTop,gapRight,gapBottom);
                mGap.draw(canvas);
            }
        }

        //画最后的divider
        if(bandCount > 0 && showDividerEnd){
            Band band = mBands.get(mBands.size() - 1);
            dividerTop = band.getBandTop() + (mBandWidth > 0 ? mBandWidth : band.getBandWidth());
            dividerBottom = dividerTop + dividerWidth;

            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mDivider.draw(canvas);
        }
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

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(),attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public boolean mCarriageReturn = false;//是否另起一行

        public int mGravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,R.styleable.FlowLayout_Layout);
//            mGravity = a.getInt(R.styleable.FlowLayout_Layout_android_layout_gravity,mGravity);
            mCarriageReturn = a.getBoolean(R.styleable.FlowLayout_Layout_carriageReturn,mCarriageReturn);

            a.recycle();
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

            mCarriageReturn = source.mCarriageReturn;
            mGravity = source.mGravity;
        }
    }

    private static class Band {
        private int mBandContentMaxWidth;//determined on measure
        private int mStartIndex;//start child index
        private int mBandTop;//determined on layout

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

        public int getBandTop() {
            return mBandTop;
        }

        public void setBandTop(int bandTop) {
            this.mBandTop = bandTop;
        }
    }
}
