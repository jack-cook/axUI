package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Jack on 16/3/18.
 */
public class SelfAdaptionScrollView extends ScrollView {
    private int mMaxHeight = 0;

    public SelfAdaptionScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public SelfAdaptionScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SelfAdaptionScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public SelfAdaptionScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SelfAdaptionScrollView);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.SelfAdaptionScrollView_maxHeight, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mMaxHeight > 0) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (height > mMaxHeight) {
                height = mMaxHeight;
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, heightMode));
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
