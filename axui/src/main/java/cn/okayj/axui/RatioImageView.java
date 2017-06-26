/*
 * Copyright 2016 Kaijie Huang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.okayj.axui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Jack on 15/12/30.
 */
public class RatioImageView extends ImageView implements RatioView {
    private static final float RATIO_DEFAULT = 0;

    private static final int ADJUST_DEFAULT = ADJUST_TO_WIDTH;

    private float mRatio = RATIO_DEFAULT;

    private int mAdjustTo = ADJUST_DEFAULT;

    public RatioImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public RatioImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView);
        mRatio = a.getFloat(R.styleable.RatioImageView_ratio, RATIO_DEFAULT);
        if (mRatio < 0) {
            mRatio = RATIO_DEFAULT;
        }
        mAdjustTo = a.getInt(R.styleable.RatioImageView_adjustTo, ADJUST_DEFAULT);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            if (mAdjustTo == ADJUST_TO_HEIGHT) {
                switch (heightMode) {
                    case MeasureSpec.EXACTLY:
                        width = (int) (height * mRatio);
                        widthMode = MeasureSpec.EXACTLY;
                        break;
                    case MeasureSpec.AT_MOST:
                    case MeasureSpec.UNSPECIFIED:
                        width = (int) (height * mRatio);
                        widthMode = heightMode;
                        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), MeasureSpec.makeMeasureSpec(height, heightMode));
                        height = getMeasuredHeight();
                        heightMode = MeasureSpec.EXACTLY;
                        width = (int) (height * mRatio);
                        widthMode = MeasureSpec.EXACTLY;
                        break;
                }
            } else {
                switch (widthMode) {
                    case MeasureSpec.EXACTLY:
                        height = (int) (width / mRatio);
                        heightMode = MeasureSpec.EXACTLY;
                        break;
                    case MeasureSpec.AT_MOST:
                    case MeasureSpec.UNSPECIFIED:
                        height = (int) (width / mRatio);
                        heightMode = widthMode;
                        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), MeasureSpec.makeMeasureSpec(height, heightMode));
                        width = getMeasuredWidth();
                        widthMode = MeasureSpec.EXACTLY;
                        height = (int) (width / mRatio);
                        heightMode = MeasureSpec.EXACTLY;
                        break;
                }
            }

            super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), MeasureSpec.makeMeasureSpec(height, heightMode));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setRatio(float ratio) {
        if (ratio < 0 || mRatio == ratio) {
            return;
        }

        mRatio = ratio;
        requestLayout();
    }

    public float getRatio() {
        return mRatio;
    }

    public void adjustTo(int adjustTo) {
        if ((mAdjustTo == ADJUST_TO_HEIGHT && adjustTo == ADJUST_TO_HEIGHT) || (mAdjustTo == ADJUST_TO_WIDTH && adjustTo == ADJUST_TO_WIDTH)) {
            return;
        }
        mAdjustTo = adjustTo;
        requestLayout();
    }
}
