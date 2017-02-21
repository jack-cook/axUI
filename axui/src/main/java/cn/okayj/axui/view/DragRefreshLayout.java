package cn.okayj.axui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;

/**
 * Created by jack on 2017/2/6.
 */

public abstract class DragRefreshLayout extends ViewGroup {
    private static final float DEFAULT_DRAG_RATE = 0.5f;

    /*private static final int STATE_IDLE = 0;
    private static final int STATE_DRAGING_DOWN = 1;
    private static final int STATE_REFRESHING_TOP = 2;
    private static final int STATE_DRAGING_UP = -1;
    private static final int STATE_REFRESHING_BOTTOM = -2;*/

    private MoveRecorder mMoveRecorder = new MoveRecorder();

    private float mDragRate = DEFAULT_DRAG_RATE;

    private float mCurrentTouchOffset = 0f;//点差距

    /**
     * 当有触摸事件时，控件可能并不处于初始状态，需要累计距离作为总偏移
     */
    private int mPreTotalOffset = 0;

    private int mTotalOffset = 0;

    private int mTouchSlop;

    private int mStopOffsetTop;//顶部刷新时停留的点距

    private int mStopOffsetBottom;//底部刷新时停留的点距

//    private int mState = STATE_IDLE;

    private boolean mRefreshingTop;

    private boolean mRefreshingBottom;

    private boolean mRefreshTopEnabled;

    private boolean mRefreshBottomEnabled;

    private boolean mDraging;

    private Animation mTopRefreshAnimation;

    private Animation mBottomRefreshAnimation;

    private Animation mToTopAnimation;

    private Animation mToBottomAnimation;

    public DragRefreshLayout(Context context) {
        super(context);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public DragRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mMoveRecorder.record(ev);
        final int deltaY = (int) mMoveRecorder.getDeltaY();

        if (canChildScroll(deltaY)) {
            mMoveRecorder.reset();
            return false;
        }

        //down touch slop
        if (deltaY > 0 && deltaY > mTouchSlop) {
            mMoveRecorder.reset();
            return true;
        }

        //up touch slop
        if (deltaY < 0 && -deltaY > mTouchSlop) {
            mMoveRecorder.reset();
            return true;
        }

        return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        mMoveRecorder.record(event);
        final int deltaY = (int) mMoveRecorder.getDeltaY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPreTotalOffset = mTotalOffset;
                mDraging = true;
                stopAnimation();

                break;
            case MotionEvent.ACTION_UP:
                if (mTotalOffset > 0) {
                    if(mRefreshTopEnabled && mTotalOffset >= mStopOffsetTop){
                        refreshTop(true);
                    }else {
                        startToTopAnimation();
                    }
                } else if (mTotalOffset < 0 ) {
                    if(mRefreshBottomEnabled && -mTotalOffset >= mStopOffsetBottom){
                        refreshBottom(true);
                    }else {
                        startToBottomAnimation();
                    }
                }

                mMoveRecorder.reset();
                mDraging = false;

                break;
            case MotionEvent.ACTION_CANCEL:
                if (mTotalOffset > 0)
                    startToTopAnimation();
                else if (mTotalOffset < 0)
                    startToBottomAnimation();

                mMoveRecorder.reset();
                mDraging = false;

                break;
            case MotionEvent.ACTION_MOVE:
                mCurrentTouchOffset = (int) (deltaY * mDragRate);
                mTotalOffset = mPreTotalOffset + (int) mCurrentTouchOffset;
                move();
                break;
        }

        return true;
    }

    protected boolean canChildScroll(int upOrDown) {
        // TODO: 2017/2/21
        return false;
    }

    private void startRefreshTopAnimation() {
        // TODO: 2017/2/21
    }

    private void startRefreshBottomAnimation() {
        // TODO: 2017/2/21
    }

    private void startToTopAnimation() {
        // TODO: 2017/2/21
    }

    private void startToBottomAnimation() {
        // TODO: 2017/2/21
    }

    private void stopAnimation() {
        // TODO: 2017/2/21
    }

    private void move() {
        // TODO: 2017/2/21
    }

    public void refreshTop(boolean refresh){
        if(!mRefreshTopEnabled || mRefreshingTop == refresh)
            return;

        mRefreshingTop = refresh;
        startRefreshTopAnimation();
    }

    public void refreshBottom(boolean refresh){
        if(!mRefreshBottomEnabled || mRefreshingBottom == refresh)
            return;

        mRefreshingBottom = refresh;
        startRefreshBottomAnimation();
    }
}
