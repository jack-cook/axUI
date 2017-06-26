package cn.okayj.axui;

import android.support.annotation.IntDef;

/**
 * Created by jack on 2017/6/26.
 */

public interface RatioView {
    public static final int ADJUST_TO_HEIGHT = 1;

    public static final int ADJUST_TO_WIDTH = 2;

    public void setRatio(float ratio);

    public float getRatio();

    public void adjustTo(@AdjustTo int adjustTo);

    @IntDef({ADJUST_TO_HEIGHT,ADJUST_TO_WIDTH})
    public @interface AdjustTo {}
}
