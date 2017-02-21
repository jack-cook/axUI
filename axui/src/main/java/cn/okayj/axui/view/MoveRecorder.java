package cn.okayj.axui.view;

import android.view.MotionEvent;

/**
 * Created by jack on 2017/2/21.
 */

public class MoveRecorder {
    private static final int INVALID_POINTER = -1;

    private float mPointerDownX;

    private float mPointerDownY;

    private float mDeltaX;

    private float mDeltaY;

    private float mPreDeltaX;

    private float mPreDeltaY;

    private int mActivePointerId = INVALID_POINTER;

    public void record(MotionEvent motionEvent) {
        final int action = motionEvent.getActionMasked();
        int pointerIndex = -1;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = motionEvent.getPointerId(0);
                mPointerDownX = motionEvent.getX();
                mPointerDownY = motionEvent.getY();
                mDeltaX = 0;
                mDeltaY = 0;
                mPreDeltaX = 0;
                mPreDeltaY = 0;
                break;
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
                reset();
        }
    }

    public float getDeltaX(){
        return mDeltaX + mPreDeltaX;
    }

    public float getDeltaY(){
        return mDeltaY + mPreDeltaY;
    }

    public void reset() {
        mActivePointerId = INVALID_POINTER;
        mPointerDownX = 0;
        mPointerDownY = 0;
        mDeltaX = 0;
        mDeltaY = 0;
        mPreDeltaX = 0;
        mPreDeltaY = 0;
    }

}
