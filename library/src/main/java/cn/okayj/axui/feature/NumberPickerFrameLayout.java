package cn.okayj.axui.feature;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.okayj.axui.R;

/**
 * Created by Jack on 16/5/7.
 */
public class NumberPickerFrameLayout extends FrameLayout {
    public static final int FLAG_NUMBER_TOO_LARGE = 1;
    public static final int FLAG_NUMBER_TOO_SMALL = -1;

    public static final int STRATEGY_RESET_NUMBER_TO_MIN = -1;
    public static final int STRATEGY_RESET_NUMBER_TO_MAX = 1;

    private final int DEFAULT_MIN_NUMBER = 0;// TODO: 16/5/8 init
    private final int DEFAULT_MAX_NUMBER = Integer.MAX_VALUE;// TODO: 16/5/8 init
    private final int DEFAULT_NUMBER = DEFAULT_MIN_NUMBER;// TODO: 16/5/8 init

    private int mNumber;
    private int mMinNumber;
    private int mMaxNumber;

    private boolean mPlusActivated = true;
    private boolean mMinusActivated = true;

    private boolean mAdjustToBound = false;// TODO: 16/5/8 init

    private int mResetStrategy = STRATEGY_RESET_NUMBER_TO_MIN;// TODO: 16/5/8 init

    private int mStep = 1;// TODO: 16/5/8 init

    private int mNumberTextViewId;
    private int mPlusButtonId;
    private int mMinusButtonId;

    private TextView mNumberTextView;
    private View mPlusButton,mMinusButton;

    private NumberPickerListener mNumberPickerListener;
    private PreEventListener mPreEventListener;
    private ErrorListener mErrorListener;


    public NumberPickerFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NumberPickerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public NumberPickerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerFrameLayout);
        mNumberTextViewId = a.getInteger(R.styleable.NumberPickerFrameLayout_pickerTextViewId,0);
        mPlusButtonId = a.getInt(R.styleable.NumberPickerFrameLayout_pickerPlusButtonId,0);
        mMinusButtonId = a.getInt(R.styleable.NumberPickerFrameLayout_pickerMinusButtonId,0);

        if(0 == mNumberTextViewId || 0 == mPlusButtonId || 0 == mMinusButtonId){
            throw new RuntimeException("Ids of parts of number picker are not specified");
        }

        if(mNumberTextViewId == mPlusButtonId || mNumberTextViewId == mMinusButtonId || mPlusButtonId == mMinusButtonId){
            throw new RuntimeException("Ids of parts of number picker should not be same");
        }
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public final int getNumber() {
        return mNumber;
    }

    public final void setNumber(int number) {
        int preNumber = mNumber;
        int aimNumber = number;

        if(aimNumber > mMaxNumber){
            if(mAdjustToBound) {
                mNumber = mMaxNumber;
            }else {
                onInternalOutOfBound(FLAG_NUMBER_TOO_LARGE);
                return;
            }
        }else if(aimNumber < mMinNumber){
            if(mAdjustToBound) {
                mNumber = mMinNumber;
            }else {
                onInternalOutOfBound(FLAG_NUMBER_TOO_SMALL);
                return;
            }
        }else {
            mNumber = aimNumber;
        }
        //change text
        if(preNumber != mNumber){
            mNumberTextView.setText(""+mNumber);
        }


        /*
        * notify event
         */
        if(mNumber == mMaxNumber){
            onInternalReachMax(preNumber,aimNumber,true);
        }else if(preNumber == mMaxNumber){
            onInternalLeaveMax(true);
        }

        if(mNumber == mMinNumber){
            onInternalReachMin(preNumber,aimNumber,true);
        }else if(preNumber == mMaxNumber){
            onInternalLeaveMin(true);
        }
    }

    public final void resetNumber(){
        switch (mResetStrategy){
            case STRATEGY_RESET_NUMBER_TO_MIN :
                mNumber = mMinNumber;
                break;
            case STRATEGY_RESET_NUMBER_TO_MAX :
                mNumber = mMaxNumber;
                break;
            default:
        }
        //change text
        mNumberTextView.setText(""+mNumber);
    }

    public final void plus(){
        onInternalAttemptPlus();
        if(mPlusActivated) {
            int aimNumber = mNumber + mStep;
            if(onInternalPrePlus(aimNumber)) {
                setNumber(aimNumber);
            }
        }else {

        }
    }

    public final void minus(){
        onInternalAttemptMinus();
        if(mMinusActivated) {
            int aimNumber = mNumber - mStep;
            if(onInternalPreMinus(aimNumber)){
                setNumber(aimNumber);
            }
        }else {

        }
    }

    public final int getMinNumber() {
        return mMinNumber;
    }

    public final void setMinNumber(int minNumber) {
        if(minNumber > mMaxNumber){
            throw new RuntimeException("min number is too large");
        }
        mMinNumber = minNumber;
        if(mNumber < mMinNumber){
            resetNumber();
        }
    }

    public final int getMaxNumber() {
        return mMaxNumber;
    }

    public final void setMaxNumber(int maxNumber) {
        if(maxNumber < mMinNumber){
            throw new RuntimeException("max number is too small");
        }
        mMaxNumber = maxNumber;
        if(mNumber > mMaxNumber){
            resetNumber();
        }
    }

    public final void setBounds(int minNumber, int maxNumber){
        if(minNumber > maxNumber){
            throw new RuntimeException("min number should <= max number");
        }

        mMinNumber = minNumber;
        mMaxNumber = maxNumber;
        if(mNumber < mMinNumber || mNumber > mMaxNumber){
            resetNumber();
        }
    }

    private void onInternalOutOfBound(int flag){
        // TODO: 16/5/8
    }

    private void onInternalReachMax(int preNumber, int aimNumber, boolean notify){

    }

    private void onInternalReachMin(int preNumber, int aimNumber, boolean notify){

    }

    private void onInternalLeaveMax(boolean notify){

    }

    private void onInternalLeaveMin(boolean notify){

    }

    private void onInternalAttemptPlus(){
        // TODO: 16/5/8
    }

    private void onInternalAttemptMinus(){
        // TODO: 16/5/8
    }

    private boolean onInternalPrePlus(int aimNumber){
        return false;// TODO: 16/5/8
    }

    private boolean onInternalPreMinus(int aimNumber){
        return false;// TODO: 16/5/8
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mNumberTextView = (TextView) findViewById(mNumberTextViewId);
        mPlusButton = findViewById(mPlusButtonId);
        mMinusButton = findViewById(mMinusButtonId);

        if(null == mNumberTextView){
            throw new RuntimeException("Can not find textView of number");
        }

        if(null == mPlusButton){
            throw new RuntimeException("Can not find plus button");
        }

        if(null == mMinusButton){
            throw new RuntimeException("Can not find minus button");
        }
    }

    public interface NumberPickerListener {
        void onPlus(int preNumber, int currentNumber);
        void onMinus(int preNumber, int currentNumber);

        /**
         * number reach max
         * @param preNumber number before
         * @param aimNumber aim number was to set, may be greater than max number
         */
        void onReachMax(int preNumber, int aimNumber);

        /**
         * number reach min
         * @param preNumber number before
         * @param aimNumber aim number was to set, may be less than min number
         */
        void onReachMin(int preNumber, int aimNumber);
    }

    public interface PreEventListener {
        void onAttemptPlus();
        void onAttemptMinus();
        boolean onPrePlus();
        boolean onPreMinus();
//        boolean onPreSetNumber(int aimNumber);
    }

    public interface ErrorListener {
        void numberOutOfBound(int flag);
    }
}
