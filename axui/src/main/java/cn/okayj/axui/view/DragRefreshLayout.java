package cn.okayj.axui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

/**
 * Created by jack on 2017/2/6.
 */

public abstract class DragRefreshLayout extends ViewGroup {
    private static final float DEFAULT_DRAG_RATE = 0.5f;

    private static final int MAX_OFFSET_ANIMATION_DURATION = 700;

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

    private View mMainView;

    private View mTopView;

    private View mBottonView;

    private RefreshListener mTopRefreshlistener;

    private RefreshListener mBottomRefreshListener;

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

        if (canDrag()) {
            mMoveRecorder.reset();
            mDraging = true;
            return true;
        }

        //down touch slop
        if (deltaY > 0 && deltaY > mTouchSlop) {
            mMoveRecorder.reset();
            mDraging = true;
            return true;
        }

        //up touch slop
        if (deltaY < 0 && -deltaY > mTouchSlop) {
            mMoveRecorder.reset();
            mDraging = true;
            return true;
        }

        return false;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //此方法不需要考虑touch slop

        if (!mRefreshTopEnabled && !mRefreshBottomEnabled) {
            return false;
        }

        //不管处于什么状态，都置为dragging状态，从此之后掌管touch事件
        mDraging = true;

        //todo 目前用此方法判断用户是否刚开始drag，后续可能会修改判断方式
        if (!mMoveRecorder.isInMiddleOfRecording()) {
            mPreTotalOffset = mTotalOffset;
            //start dragging and stop any animation
            stopAnimation();
            mMoveRecorder.newStartPoint(event);
            return true;
        }

        int action = event.getActionMasked();
        mMoveRecorder.record(event);
        final int deltaY = (int) mMoveRecorder.getDeltaY();

        /*
        //--------------------------
        //如果处于临界状态，不处理，转交事件
        //---------------------------
        if(mTotalOffset == 0){
            if(deltaY > 0){
                if(!mRefreshTopEnabled)
                    return false;
            }else if(deltaY < 0){
                if(!mRefreshBottomEnabled)
                    return false;
            }
        }
        */

        switch (action) {
            //---------------------
            //拖动完成，刷新或者回归状态
            //---------------------
            case MotionEvent.ACTION_UP:
                if (mTotalOffset > 0) {
                    if (mRefreshTopEnabled && mTotalOffset >= mStopOffsetTop) {
                        refreshTop(true);
                    } else {
                        startToTopAnimation();
                    }
                } else if (mTotalOffset < 0) {
                    if (mRefreshBottomEnabled && -mTotalOffset >= mStopOffsetBottom) {
                        refreshBottom(true);
                    } else {
                        startToBottomAnimation();
                    }
                }

                mMoveRecorder.reset();
                mDraging = false;

                break;
            //--------------------------
            //事件被截取，回归状态
            //--------------------------
            case MotionEvent.ACTION_CANCEL:
                if (mTotalOffset > 0)
                    startToTopAnimation();
                else if (mTotalOffset < 0)
                    startToBottomAnimation();

                mMoveRecorder.reset();
                mDraging = false;

                break;
            //-------------------------
            //移动事件，移动视图
            //-------------------------
            case MotionEvent.ACTION_MOVE:
                mCurrentTouchOffset = (int) (deltaY * mDragRate);
                mTotalOffset = mPreTotalOffset + (int) mCurrentTouchOffset;

                /*
                   视图的移动不能超过临界状态
                 */
                if (mTotalOffset > 0 && !mRefreshTopEnabled) {
                    mPreTotalOffset = mPreTotalOffset - mTotalOffset;//反向偏移，使totalOffset 处于0
                    mTotalOffset = 0;
                } else if (mTotalOffset < 0 && !mRefreshBottomEnabled) {
                    mPreTotalOffset = mPreTotalOffset - mTotalOffset;//反向偏移，使totalOffset 处于0
                    mTotalOffset = 0;
                }

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
        int currentTop = mMainView.getTop();
        int distance = mTotalOffset - currentTop;
        mMainView.offsetTopAndBottom(distance);
        if (mTopView != null) {
            mTopView.offsetTopAndBottom(distance);
        }

        if (mBottonView != null) {
            mBottonView.offsetTopAndBottom(distance);
        }


        //notify
        if (mCurrentTouchOffset > 0) {
            float movePercent = ((float) mTotalOffset) / mStopOffsetTop;
            mInternalTopRefreshListener.onMoved(mTotalOffset, movePercent);
        } else if (mCurrentTouchOffset < 0) {
            float movePercent = ((float) mTotalOffset) / mStopOffsetBottom;
            mInternalBottomRefreshListerner.onMoved(mTotalOffset, movePercent);
        } else {
            if (currentTop > 0) {
                mInternalTopRefreshListener.onMoved(0,0);
            } else if (currentTop < 0) {
                mInternalBottomRefreshListerner.onMoved(0,0);
            } else {
                //throw new IllegalStateException();
            }
        }

    }

    /**
     * 判断是否处于能拖拽的状态
     *
     * @return
     */
    private boolean canDrag() {
        return mDraging || Math.abs(mTotalOffset) > mTouchSlop;
    }

    public void refreshTop(boolean refresh) {
        if (!mRefreshTopEnabled || mRefreshingTop == refresh)
            return;

        mRefreshingTop = refresh;

        if (refresh) {
            startRefreshTopAnimation();
            mInternalTopRefreshListener.onRefresh();
        } else {
            if (mDraging) {
                return;
            } else {
                mInternalBottomRefreshListerner.onEndRefresh();

                if (mTotalOffset > 0) {
                    startToTopAnimation();
                } else if (mTotalOffset < 0) {
                    startToBottomAnimation();
                }
            }
        }
    }

    public void refreshBottom(boolean refresh) {
        if (!mRefreshBottomEnabled || mRefreshingBottom == refresh)
            return;

        mRefreshingBottom = refresh;

        if (refresh) {
            startRefreshBottomAnimation();
            mInternalBottomRefreshListerner.onRefresh();
        } else {
            mInternalBottomRefreshListerner.onEndRefresh();

            if (mDraging) {
                return;
            } else {
                if (mTotalOffset > 0) {
                    startToTopAnimation();
                } else if (mTotalOffset < 0) {
                    startToBottomAnimation();
                }
            }
        }
    }

    private RefreshListener mInternalTopRefreshListener = new RefreshListener() {
        @Override
        public void onRefresh() {
            // TODO: 2017/2/25
        }

        @Override
        public void onEndRefresh() {
            // TODO: 2017/2/25

        }

        @Override
        public void onMoved(int currentOffset, float percent) {
            // TODO: 2017/2/25

        }
    };

    private RefreshListener mInternalBottomRefreshListerner = new RefreshListener() {
        @Override
        public void onRefresh() {
            // TODO: 2017/2/25

        }

        @Override
        public void onEndRefresh() {
            // TODO: 2017/2/25

        }

        @Override
        public void onMoved(int currentOffset, float percent) {
            // TODO: 2017/2/25

        }
    };

    public interface RefreshListener {
        void onRefresh();

        void onEndRefresh();

        void onMoved(int currentOffset, float percent);
    }
}
