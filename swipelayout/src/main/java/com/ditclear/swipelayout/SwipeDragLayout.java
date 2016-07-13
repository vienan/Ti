package com.ditclear.swipelayout;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by ditclear on 16/7/12.
 * 可滑动的layout extends FrameLayout
 */
public class SwipeDragLayout extends FrameLayout {

    private View contentView;
    private View menuView;
    private ViewDragHelper mDragHelper;
    private Point originPos = new Point();

    private boolean isOpen;
    private float offset;
    private float needOffset = 0.1f;

    private SwipeListener mListener;

    public SwipeDragLayout(Context context) {
        this(context, null);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化dragHelper，对拖动的view进行操作
    private void init() {

        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {


            @Override
            public boolean tryCaptureView(View child, int pointerId) {

                return child == contentView;
            }


            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                Log.d("releasedChild", "xvel:" + xvel + " yvel:" + yvel);
                if (releasedChild == contentView) {
                    if (xvel < 0) {
                        open();
                    } else if (xvel >= 0) {
                        close();
                    }


                    invalidate();
                }
            }


            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                Log.d("DragLayout", "clampViewPositionHorizontal " + left + "," + dx);
                final int leftBound = getPaddingLeft() - menuView.getWidth();
                final int rightBound = getWidth() - child.getWidth();
                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
                return newLeft;
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return contentView == child ? menuView.getWidth() : 0;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                final int childWidth = menuView.getWidth();
                offset = -(float) left / childWidth;
                //offset can callback here
                Log.d("offset", "" + offset);
                dispatchSwipeEvent(offset);
                Log.d("isOpen", "" + isOpen);
            }


        });

    }


    private void dispatchSwipeEvent(float offset) {
        if (mListener == null) return;

        if (offset == 0) {
            isOpen = false;
            mListener.onClosed(this);
        } else if (offset == 1) {
            isOpen = true;
            mListener.onOpened(this);
        }

        if (offset < 1 && offset > 0)
            if (isOpen) {

                mListener.onStartClose(this);
            } else {
                mListener.onStartOpen(this);
            }
        mListener.onUpdate(this, offset);

    }

    public boolean isOpen() {
        return isOpen;
    }

    public void toggle(View v) {
        if (isOpen)
            smoothClose(v);
        else
            smoothOpen(v);

        invalidate();
    }

    private void toggle() {
        if (isOpen())
            close();
        else
            open();
    }

    private void open() {
        mDragHelper.settleCapturedViewAt(originPos.x - menuView.getWidth(), originPos.y);

    }

    public void smoothOpen(View v) {
        mDragHelper.smoothSlideViewTo(v, originPos.x - menuView.getWidth(), originPos.y);
    }

    public void smoothClose(View v) {
        mDragHelper.smoothSlideViewTo(v, originPos.x, originPos.y);
    }

    private void close() {
        mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isOpen()) {
            return mDragHelper.shouldInterceptTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        originPos.x = contentView.getLeft();
        originPos.y = contentView.getTop();
    }


    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(1);
        menuView = getChildAt(0);
        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        menuView.setLayoutParams(params);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }

    public void addListener(SwipeListener listener) {
        mListener = listener;
    }

    public interface SwipeListener {

        void onStartOpen(SwipeDragLayout layout);

        void onStartClose(SwipeDragLayout layout);

        void onUpdate(SwipeDragLayout layout, float offset);

        void onOpened(SwipeDragLayout layout);

        void onClosed(SwipeDragLayout layout);
    }
}
