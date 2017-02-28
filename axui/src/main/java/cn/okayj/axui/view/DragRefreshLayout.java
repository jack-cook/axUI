package cn.okayj.axui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;

import cn.okayj.axui.R;

/**
 * Created by jack on 2017/2/6.
 * 需要使用{@link #setMainView(View)},{@link #setTopView(View)},{@link #setBottomView(View)}设置视图。
 * 请不要直接调用{@link #addView(View)}!!!!!
 */

public class DragRefreshLayout extends ViewGroup {
    private static final String TAG = "DragRefreshLayout";

    private static final float DEFAULT_DRAG_RATE = 0.5f;

    private static final int MAX_OFFSET_ANIMATION_DURATION = 700;


    private static final int VIEW_STATE_OFFSET_TOP = 1;

    private static final int VIEW_STATE_HOME = 0;//no offset

    private static final int VIEW_STATE_OFFSET_BOTTOM = -1;


    private MoveRecorder mMoveRecorder = new MoveRecorder();

    private float mDragRate = DEFAULT_DRAG_RATE;

    /**
     * 当有触摸事件时，控件可能并不处于初始状态，可能已经有偏移，需要累计距离作为总偏移
     */
    private int mPreTotalOffset = 0;

    /**
     * 该offset是最终的offset，为 {@link #mPreTotalOffset} 加上此次触摸产生的偏移量
     */
    private int mTotalOffset = 0;

    /**
     * {@link #mTotalOffset} 为确定的偏移，但是视图绘制未必即时反应该偏移，所以需要该变量表示视图实际上的偏移，以计算到目的偏移的距离，方便{@link #move()} 操作
     */
    private int mLayoutOffset;

    private int mTouchSlop;

    private int mStopOffsetTop;//顶部刷新时停留的点距

    private int mStopOffsetBottom;//底部刷新时停留的点距

    private boolean mRefreshTopEnabled;

    private boolean mRefreshBottomEnabled;

    private boolean mRefreshingTop;

    private boolean mRefreshingBottom;

    private boolean mDragging;

    private int mViewState = VIEW_STATE_HOME;

    private Interpolator mDecelerateInterpolator = new DecelerateInterpolator(2f);

    private AnimatorMoveTargetObject mAnimatorMoveTargetObject = new AnimatorMoveTargetObject();

    private Animator mCurrentAnimator;

    private View mMainView;

    private View mTopView;

    private View mBottomView;

    private int mTopViewId;//find top view after inflated

    private int mBottomViewId;//find bottom view after inflated

    private List<RefreshListener> mTopRefreshListeners = new ArrayList<>();

    private List<RefreshListener> mBottomRefreshListeners = new ArrayList<>();

    private RefreshListener mTopViewRefreshListener;

    private RefreshListener mBottomViewRefreshListener;

    private OnChildScrollCallback mOnChildScrollCallback;

    public DragRefreshLayout(Context context) {
        this(context, null);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setWillNotDraw(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragRefreshLayout);

        mTopViewId = a.getResourceId(R.styleable.DragRefreshLayout_topViewId, 0);

        mBottomViewId = a.getResourceId(R.styleable.DragRefreshLayout_bottomViewId, 0);

        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childToFind = getChildCount();
        if (childToFind > 3) {
            throw new IllegalStateException(TAG + "'s child count should not more than 3");
        }

        View child;
        if (mTopViewId != 0) {
            childToFind--;
            child = findViewById(mTopViewId);
            if (child == null)
                throw new IllegalStateException("Could not find top view by id");

            mTopView = child;
            mRefreshTopEnabled = true;
            if(mTopView instanceof RefreshListener){
                mTopViewRefreshListener = (RefreshListener) mTopView;
            }
        }

        if (mBottomViewId != 0) {
            childToFind--;
            child = findViewById(mBottomViewId);
            if (child == null)
                throw new IllegalStateException("Could not find bottom view by id");

            mBottomView = child;
            mRefreshBottomEnabled = true;
            if(mBottomView instanceof RefreshListener) {
                mBottomViewRefreshListener = (RefreshListener) mBottomView;
            }
        }

        if (childToFind == 1) {
            for (int index = 0; index < getChildCount(); ++index) {
                child = getChildAt(index);
                if (child == mTopView || child == mBottomView)
                    continue;
                else {
                    mMainView = child;
                    break;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        if (mMainView != null) {
            mMainView.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
        }

        int childWidth, childHeight, childWidthMode, childheightMode;
        for (int index = 0; index < getChildCount(); ++index) {
            View child = getChildAt(index);
            if (child == mMainView)
                continue;//measured;

            LayoutParams layoutParams = child.getLayoutParams();
            switch (layoutParams.width) {
                case LayoutParams.MATCH_PARENT:
                    childWidth = measuredWidth;
                    childWidthMode = MeasureSpec.EXACTLY;
                    break;
                case LayoutParams.WRAP_CONTENT:
                    childWidth = measuredWidth;
                    childWidthMode = MeasureSpec.AT_MOST;
                    break;
                default:
                    childWidth = layoutParams.width;
                    childWidthMode = MeasureSpec.EXACTLY;
            }
            switch (layoutParams.height) {
                case LayoutParams.MATCH_PARENT:
                    childHeight = measuredHeight;
                    childheightMode = MeasureSpec.EXACTLY;
                    break;
                case LayoutParams.WRAP_CONTENT:
                    childHeight = measuredHeight;
                    childheightMode = MeasureSpec.AT_MOST;
                    break;
                default:
                    childHeight = layoutParams.height;
                    childheightMode = MeasureSpec.EXACTLY;
            }

            child.measure(MeasureSpec.makeMeasureSpec(childWidth, childWidthMode),
                    MeasureSpec.makeMeasureSpec(childHeight, childheightMode));

            //计算刷新动画的停留高度
            if (child == mTopView) {
                mStopOffsetTop = mTopView.getMeasuredHeight();
            } else if (child == mBottomView) {
                mStopOffsetBottom = -mBottomView.getHeight();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int height = b - t;

        mLayoutOffset = mTotalOffset;

        if (mMainView != null) {
            mMainView.layout(0, mLayoutOffset, mMainView.getMeasuredWidth(), mLayoutOffset + mMainView.getMeasuredHeight());
        }

        if (mTopView != null) {
            mTopView.layout(0, -mTopView.getMeasuredHeight() + mLayoutOffset, mTopView.getMeasuredWidth(), mLayoutOffset);
        }

        if (mBottomView != null) {
            mBottomView.layout(0, height + mLayoutOffset, mBottomView.getMeasuredWidth(), height + mLayoutOffset + mBottomView.getMeasuredHeight());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mRefreshTopEnabled && !mRefreshBottomEnabled) {
            return false;
        }

        mMoveRecorder.record(ev);
        final int deltaY = (int) mMoveRecorder.getDeltaY();

        //子视图能处理
        if (canChildScroll(deltaY)) {
            mMoveRecorder.reset();
            return false;
        }

        //正处在偏移状态，可拖动
        if (mViewState != VIEW_STATE_HOME) {
            mMoveRecorder.reset();
            return true;
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
        //此方法不需要考虑touch slop

        if (!mRefreshTopEnabled && !mRefreshBottomEnabled) {
            return false;
        }


        if (!mDragging) {
            mPreTotalOffset = mTotalOffset;
            //start dragging and stop any animation
            stopAnimation();
            mMoveRecorder.newStartPoint(event);
            mDragging = true;
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
                mMoveRecorder.reset();
                mDragging = false;
                onDragEnd(true);
                break;
            //--------------------------
            //事件被截取，回归状态
            //--------------------------
            case MotionEvent.ACTION_CANCEL:
                mMoveRecorder.reset();
                mDragging = false;
                onDragEnd(false);
                break;
            //-------------------------
            //移动事件，移动视图
            //-------------------------
            case MotionEvent.ACTION_MOVE:
                int durrentTouchOffset = (int) (deltaY * mDragRate);
                mTotalOffset = mPreTotalOffset + durrentTouchOffset;

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
        if(mMainView == null){
            return false;
        }

        if(mOnChildScrollCallback != null){
            return mOnChildScrollCallback.canChildScroll(this,mMainView,upOrDown);
        }

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mMainView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mMainView;

                if (upOrDown > 0) {
                    return absListView.getChildCount() > 0
                            && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                            .getTop() < absListView.getPaddingTop());
                } else {
                    final int lastVisiblePosition, lastItemPosition, childCount;
                    childCount = absListView.getChildCount();
                    if (childCount == 0)
                        return false;

                    lastVisiblePosition = absListView.getLastVisiblePosition();
                    lastItemPosition = absListView.getCount() - 1;
                    if (lastVisiblePosition < lastItemPosition)
                        return true;
                    else {
                        return absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > (absListView.getHeight() - absListView.getPaddingBottom());
                    }
                }
            } else {
                return mMainView.canScrollVertically(upOrDown);
            }
        } else {
            return mMainView.canScrollVertically(upOrDown);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    private void startRefreshTopAnimation() {
        stopAnimation();

        mCurrentAnimator = ObjectAnimator.ofInt(mAnimatorMoveTargetObject, "totalOffset", mStopOffsetTop);
        mCurrentAnimator.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mCurrentAnimator.start();
    }

    private void startRefreshBottomAnimation() {
        stopAnimation();

        mCurrentAnimator = ObjectAnimator.ofInt(mAnimatorMoveTargetObject, "totalOffset", mStopOffsetBottom);
        mCurrentAnimator.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mCurrentAnimator.start();
    }

    private void startToTopAnimation() {
        stopAnimation();

        mCurrentAnimator = ObjectAnimator.ofInt(mAnimatorMoveTargetObject, "totalOffset", 0);
        mCurrentAnimator.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mCurrentAnimator.start();
    }

    private void startToBottomAnimation() {
        stopAnimation();

        mCurrentAnimator = ObjectAnimator.ofInt(mAnimatorMoveTargetObject, "totalOffset", 0);
        mCurrentAnimator.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mCurrentAnimator.start();
    }

    private void stopAnimation() {
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
            mCurrentAnimator = null;
        }
    }

    /**
     * 拖拽事件结束，运行动画，根据视图状态判断是否触发刷新。
     *
     * @param tryTriggerRefresh
     */
    private void onDragEnd(boolean tryTriggerRefresh) {
        if (mTotalOffset > 0) {
            if (mTotalOffset >= mStopOffsetTop) {
                if (mRefreshingTop) {
                    startRefreshTopAnimation();
                } else {
                    if (mRefreshTopEnabled && tryTriggerRefresh) {
                        refreshTop(true);
                    } else {
                        startToTopAnimation();
                    }
                }
            } else {
                startToTopAnimation();
            }
        } else if (mTotalOffset < 0) {
            if (mTotalOffset <= mStopOffsetBottom) {
                if (mRefreshingBottom) {
                    startRefreshBottomAnimation();
                } else {
                    if (mRefreshBottomEnabled && tryTriggerRefresh) {
                        refreshBottom(true);
                    } else {
                        startToBottomAnimation();
                    }
                }
            } else {
                startToBottomAnimation();
            }
        }
    }

    /**
     * 根据偏移移动视图，该方法维护视图状态并通知listener视图的移动
     */
    private void move() {
        int distance = mTotalOffset - mLayoutOffset;

        if(mMainView != null) {
            mMainView.offsetTopAndBottom(distance);
        }

        if (mTopView != null) {
            mTopView.offsetTopAndBottom(distance);
        }

        if (mBottomView != null) {
            mBottomView.offsetTopAndBottom(distance);
        }

        mLayoutOffset = mTotalOffset;


        //----------------------
        //change state and notify listener
        //----------------------
        float movePercent;
        switch (mViewState) {
            case VIEW_STATE_HOME:
                if (mTotalOffset == 0) {
                    break;// state not change, do nothing
                }

                movePercent = ((float) mTotalOffset) / mStopOffsetTop;
                if (mTotalOffset > 0) {
                    mViewState = VIEW_STATE_OFFSET_TOP;
                    mInternalTopRefreshListener.onMoved(mTotalOffset, movePercent);
                } else {
                    mViewState = VIEW_STATE_OFFSET_BOTTOM;
                    mInternalBottomRefreshListener.onMoved(mTotalOffset, movePercent);
                }
                break;
            case VIEW_STATE_OFFSET_TOP:
                if (mTotalOffset == 0) {
                    mViewState = VIEW_STATE_HOME;
                    mInternalTopRefreshListener.onMoved(0, 0);
                    break;
                }

                movePercent = ((float) mTotalOffset) / mStopOffsetTop;
                mInternalTopRefreshListener.onMoved(mTotalOffset, movePercent);
                break;
            case VIEW_STATE_OFFSET_BOTTOM:
                if (mTotalOffset == 0) {
                    mViewState = VIEW_STATE_HOME;
                    mInternalBottomRefreshListener.onMoved(0, 0);
                    break;
                }

                movePercent = ((float) mTotalOffset) / mStopOffsetBottom;
                mInternalBottomRefreshListener.onMoved(mTotalOffset, movePercent);
                break;
        }

    }

    public void refreshTop(boolean refresh) {
        if (!mRefreshTopEnabled || mRefreshingTop == refresh)
            return;

        mRefreshingTop = refresh;

        if (refresh) {
            startRefreshTopAnimation();
            mInternalTopRefreshListener.onRefresh();
        } else {
            if (mDragging) {
                return;
            } else {
                mInternalBottomRefreshListener.onEndRefresh();

                if (mTotalOffset > 0) {
                    startToTopAnimation();
                } else if (mTotalOffset < 0) {
                    startToBottomAnimation();
                }
            }
        }
    }

    /**
     * this will auto enable refresh top feature
     * @param view
     */
    @CallSuper
    public void setTopView(View view) {
        if (mTopView == view)
            return;

        if (mTopView != null)
            removeView(mTopView);

        mTopView = view;
        mTopViewRefreshListener = null;

        if (mTopView != null) {
            addView(mTopView);
            mRefreshTopEnabled = true;

            if (mTopView instanceof RefreshListener) {
                mTopViewRefreshListener = (RefreshListener) mTopView;
            }
        }
    }

    /**
     * this will auto enable refresh bottom feature
     * @param view
     */
    @CallSuper
    public void setBottomView(View view) {
        if (mBottomView == view)
            return;

        if (mBottomView != null)
            removeView(mBottomView);

        mBottomView = view;
        mBottomViewRefreshListener = null;

        if (mBottomView != null) {
            addView(mBottomView);
            mRefreshBottomEnabled = true;

            if (mBottomView instanceof RefreshListener) {
                mBottomViewRefreshListener = (RefreshListener) mBottomView;
            }
        }
    }

    public void setMainView(View view) {
        if (mMainView == view)
            return;

        removeView(mMainView);
        mMainView = view;
        addView(mMainView);
    }

    public void setTopRefreshEnable(boolean enabled) {
        mRefreshTopEnabled = enabled;
    }

    public void setBottomRefreshEnable(boolean enable) {
        mRefreshBottomEnabled = enable;
    }

    public void refreshBottom(boolean refresh) {
        if (!mRefreshBottomEnabled || mRefreshingBottom == refresh)
            return;

        mRefreshingBottom = refresh;

        if (refresh) {
            startRefreshBottomAnimation();
            mInternalBottomRefreshListener.onRefresh();
        } else {
            mInternalBottomRefreshListener.onEndRefresh();

            if (mDragging) {
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

    public void setOnChildScrollCallback(OnChildScrollCallback onChildScrollCallback) {
        this.mOnChildScrollCallback = onChildScrollCallback;
    }

    public void reset() {
        mPreTotalOffset = 0;
        mTotalOffset = 0;
        mRefreshingTop = false;
        mRefreshingBottom = false;
        stopAnimation();
    }

    private RefreshListener mInternalTopRefreshListener = new RefreshListener() {
        @Override
        public void onRefresh() {
            if(mTopViewRefreshListener != null){
                mTopViewRefreshListener.onRefresh();
            }

            for (RefreshListener listener : mTopRefreshListeners) {
                listener.onRefresh();
            }
        }

        @Override
        public void onEndRefresh() {
            if(mTopViewRefreshListener != null){
                mTopViewRefreshListener.onEndRefresh();
            }

            for (RefreshListener listener : mTopRefreshListeners) {
                listener.onEndRefresh();
            }
        }

        @Override
        public void onMoved(int currentOffset, float percent) {
            if(mTopViewRefreshListener != null){
                mTopViewRefreshListener.onMoved(currentOffset, percent);
            }

            for (RefreshListener listener : mTopRefreshListeners) {
                listener.onMoved(currentOffset, percent);
            }
        }
    };

    private RefreshListener mInternalBottomRefreshListener = new RefreshListener() {
        @Override
        public void onRefresh() {
            if(mBottomViewRefreshListener != null){
                mBottomViewRefreshListener.onRefresh();
            }

            for (RefreshListener listener : mBottomRefreshListeners) {
                listener.onRefresh();
            }
        }

        @Override
        public void onEndRefresh() {
            if(mBottomViewRefreshListener != null){
                mBottomViewRefreshListener.onEndRefresh();
            }

            for (RefreshListener listener : mBottomRefreshListeners) {
                listener.onEndRefresh();
            }
        }

        @Override
        public void onMoved(int currentOffset, float percent) {
            if(mBottomViewRefreshListener != null){
                mBottomViewRefreshListener.onMoved(currentOffset, percent);
            }

            for (RefreshListener listener : mBottomRefreshListeners) {
                listener.onMoved(currentOffset, percent);
            }
        }
    };

    public interface RefreshListener {
        void onRefresh();

        void onEndRefresh();

        void onMoved(int currentOffset, float percent);
    }

    public interface OnChildScrollCallback {
        boolean canChildScroll(DragRefreshLayout parent, View mainView, int direction);
    }

    private class AnimatorMoveTargetObject {
        public int getTotalOffset() {
            return mTotalOffset;
        }

        public void setTotalOffset(int totalOffset) {
            mTotalOffset = totalOffset;
            move();
        }
    }
}
