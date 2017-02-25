package cn.okayj.axui.view;

import android.view.MotionEvent;

/**
 * Created by jack on 2017/2/21.
 * 默认记录的事件为触摸事件,传其他类型的事件会出错
 */

public class MoveRecorder {
    private static final int INVALID_POINTER = -1;

    /**
     * 点差距的起点，根据{@link #mActivePointerId}变化而变化
     */
    private float mPointerDownX;

    /**
     * 点差距的起点，根据{@link #mActivePointerId}变化而变化
     */
    private float mPointerDownY;

    private float mDeltaX;//点差距

    private float mDeltaY;//点差距

    private float mPreDeltaX;//累计的距离

    private float mPreDeltaY;//累计的距离

    private int mActivePointerId = INVALID_POINTER;

    private boolean mInRecording = false;

    public void record(MotionEvent motionEvent) {
        final int action = motionEvent.getActionMasked();
        int pointerIndex;

        /*
        figure out start point and init
         */
        if (!mInRecording || action == MotionEvent.ACTION_DOWN) {
            mActivePointerId = motionEvent.getPointerId(0);
            mPointerDownX = motionEvent.getX(0);
            mPointerDownY = motionEvent.getY(0);
            mDeltaX = 0;
            mDeltaY = 0;
            mPreDeltaX = 0;
            mPreDeltaY = 0;

            mInRecording = true;
            return;
        }

        switch (action) {
            //case MotionEvent.ACTION_DOWN: handled before
            case MotionEvent.ACTION_POINTER_DOWN:
                //如果有新的触摸点，跟踪新的触摸点，放弃跟踪之前的触摸点
                pointerIndex = motionEvent.getActionIndex();
                mActivePointerId = motionEvent.getPointerId(pointerIndex);
                mPreDeltaX += mDeltaX;
                mPreDeltaY += mDeltaY;
                mDeltaX = 0;
                mDeltaY = 0;
                mPointerDownX = motionEvent.getX(pointerIndex);
                mPointerDownY = motionEvent.getY(pointerIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                mDeltaX = motionEvent.getX(pointerIndex) - mPointerDownX;
                mDeltaY = motionEvent.getY(pointerIndex) - mPointerDownY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = motionEvent.getActionIndex();
                int pointerId = motionEvent.findPointerIndex(pointerIndex);
                if (pointerId != mActivePointerId)//忽略非当前跟踪的pointer
                    return;

                int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mActivePointerId = motionEvent.getPointerId(newPointerIndex);
                mPreDeltaX += mDeltaX;
                mPreDeltaY += mDeltaY;
                mDeltaX = 0;
                mDeltaY = 0;
                mPointerDownX = motionEvent.getX(newPointerIndex);
                mPointerDownY = motionEvent.getY(newPointerIndex);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mInRecording = false;
        }
    }

    public void newStartPoint(MotionEvent motionEvent) {
        mActivePointerId = motionEvent.getPointerId(0);
        mPointerDownX = motionEvent.getX(0);
        mPointerDownY = motionEvent.getY(0);
        mDeltaX = 0;
        mDeltaY = 0;
        mPreDeltaX = 0;
        mPreDeltaY = 0;

        mInRecording = true;
    }

    public float getDeltaX() {
        return mDeltaX + mPreDeltaX;
    }

    public float getDeltaY() {
        return mDeltaY + mPreDeltaY;
    }

    /**
     * 是否处在持续记录的中间过程，{@link #reset()} 和 ACTION_UP， ACTION_CANCEL 都会使这个状态为false
     *
     * @return
     */
    public boolean isInMiddleOfRecording() {
        return mInRecording;
    }

    public void reset() {
        mActivePointerId = INVALID_POINTER;
        mPointerDownX = 0;
        mPointerDownY = 0;
        mDeltaX = 0;
        mDeltaY = 0;
        mPreDeltaX = 0;
        mPreDeltaY = 0;

        mInRecording = false;
    }

}
