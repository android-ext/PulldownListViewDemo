package com.pulldownlistview;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;

public class PullDownListView extends RelativeLayout implements OnScrollListener {
    private static final String TAG = "PullDownListView";
    /** 在onMeasure()中初始化 */
    static int MAX_PULL_TOP_HEIGHT;
    static int MAX_PULL_BOTTOM_HEIGHT;
    static int REFRESHING_TOP_HEIGHT;
    static int REFRESHING_BOTTOM_HEIGHT;

    /** 在onScroll()中初始化 */
    private boolean isTop;
    private boolean isBottom;

    private boolean isRefreshing;
    private boolean isAnimation;

    /** onFinishInflate()中初始化 */
    RelativeLayout layoutHeader;
    RelativeLayout layoutFooter;

    private int mCurrentY = 0;
    boolean pullTag = false;
    OnScrollListener mOnScrollListener;
    OnPullHeightChangeListener mOnPullHeightChangeListener;

    public void setOnPullHeightChangeListener(
            OnPullHeightChangeListener listener) {
        this.mOnPullHeightChangeListener = listener;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isRefreshing() {
        return this.isRefreshing;
    }

    private ListView mListView = new ListView(getContext()) {

        int lastY = 0;

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            Log.i(TAG, "onTouchEvent");
            if (isAnimation || isRefreshing) {
                return super.onTouchEvent(ev);
            }
            RelativeLayout parent = (RelativeLayout) mListView.getParent();
            /**
             * currentY是最新的y, lastY是上次的y
             * 当用户手指向下滑动的时候，因为有了竖直方向的偏移量才导致view进行对应的滑动
             * currentY - lastY >= 0 代表手指向下滑动
             */
            int currentY = (int) ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = currentY;
                    break;
                case MotionEvent.ACTION_MOVE: {
                    // 手指移动的方向
                    boolean isToBottom = currentY - lastY >= 0;
                    int step = Math.abs(currentY - lastY);
                    lastY = currentY;

                    Log.i(TAG, "isTop = " + isTop + ", isButton = " + isBottom);

                    // 视图的left, top, right, bottom 的值是针对其父视图的相对位置
                    if (isTop && mListView.getTop() >= 0) {

                        // 向下滑动 && listView.top < 顶部下拉的最大高度
                        if (isToBottom && mListView.getTop() <= MAX_PULL_TOP_HEIGHT) {
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            pullTag = true;

                            // listView.top > header.height 步长减半
                            if (mListView.getTop() > layoutHeader.getHeight()) {
                                step = step / 2;
                            }
                            // 限高处理
                            if ((mListView.getTop() + step) > MAX_PULL_TOP_HEIGHT) {
                                mCurrentY = MAX_PULL_TOP_HEIGHT;
                            } else {
                                mCurrentY += step;
                            }
                            scrollTopTo(mCurrentY);

                        } else if (!isToBottom && mListView.getTop() > 0) { // 手指向上滑动
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            if ((mListView.getTop() - step) < 0) {
                                mCurrentY = 0;
                            } else {
                                mCurrentY -= step;
                            }
                            scrollTopTo(mCurrentY);
                        } else if (!isToBottom && mListView.getTop() == 0) {
                            if (!pullTag) {
                                return super.onTouchEvent(ev);
                            }

                        }
                        return true;
                    } else if (isBottom && mListView.getBottom() <= parent.getHeight()) { // 底部显示出来了

                        // 向上滑动      listView.bottom 距离父控件的底部的距离 小于或者等于 底部最大可以上拉的高度
                        if (!isToBottom && (parent.getHeight() - mListView.getBottom()) <= MAX_PULL_BOTTOM_HEIGHT) {
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            pullTag = true;
                            // 步长减半
                            if (parent.getHeight() - mListView.getBottom() > layoutFooter.getHeight()) {
                                step = step / 2;
                            }
                            // mListView.getBottom() - step 代表listView将要上移到的底部位置
                            if ((mListView.getBottom() - step) < (parent.getHeight() - MAX_PULL_BOTTOM_HEIGHT)) {
                                mCurrentY = -MAX_PULL_BOTTOM_HEIGHT;
                            } else {
                                mCurrentY -= step;
                            }
                            scrollBottomTo(mCurrentY);
                        } else if (isToBottom && (mListView.getBottom() < parent.getHeight())) {  // 向下滑动 listView.bottom < 父控件的高度

                            if ((mListView.getBottom() + step) > parent.getHeight()) {
                                mCurrentY = 0;
                            } else {
                                mCurrentY += step;
                            }
                            scrollBottomTo(mCurrentY);
                        } else if (isToBottom && mListView.getBottom() == parent.getHeight()) { //向下滑 且 到达底部
                            if (!pullTag) {
                                return super.onTouchEvent(ev);
                            }
                        }
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    pullTag = false;

                    if (mListView.getTop() > 0) { // listView已经下拉过
                        if (mListView.getTop() > REFRESHING_TOP_HEIGHT) { // 达到刷新的偏移量
                            animateTopTo(layoutHeader.getMeasuredHeight()); // 滑动到头部刷新的位置
                            isRefreshing = true;
                            if (null != mOnPullHeightChangeListener) {
                                mOnPullHeightChangeListener.onRefreshing(true);
                            }
                        } else { // 没达到刷新的偏移量,HeaderView隐藏
                            animateTopTo(0);
                        }

                    } else if (mListView.getBottom() < parent.getHeight()) { // listView的底部已经和父控件底部有偏移量了
                        // 是否达到底部刷新的偏移量
                        if ((parent.getHeight() - mListView.getBottom()) > REFRESHING_BOTTOM_HEIGHT) {
                            animateBottomTo(-layoutFooter.getMeasuredHeight()); // 滑动到底部加载的位置
                            isRefreshing = true;
                            if (null != mOnPullHeightChangeListener) {
                                mOnPullHeightChangeListener.onRefreshing(false);
                            }
                        } else {
                            animateBottomTo(0);
                        }
                    }
            }
            return super.onTouchEvent(ev);
        }
    };

    public void scrollBottomTo(int y) {
        mListView.layout(mListView.getLeft(), y, mListView.getRight(), this.getMeasuredHeight() + y);
        if (null != mOnPullHeightChangeListener) {
            mOnPullHeightChangeListener.onBottomHeightChange(layoutHeader.getHeight(), -y);
        }
    }

    public void animateBottomTo(final int y) {

        ValueAnimator animator = ValueAnimator.ofInt(mListView.getBottom() - this.getMeasuredHeight(), y);
        animator.setDuration(300);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int frameValue = (Integer) animation.getAnimatedValue();
                mCurrentY = frameValue;
                scrollBottomTo(frameValue);
                if (frameValue == y) {
                    isAnimation = false;
                }
            }
        });
        isAnimation = true;
        animator.start();
    }

    /**
     * 改变view位置的方法：
     * 1) 直接scrollTo()
     * 2）采用属性动画
     * 3) 采用设置布局参数
     * 4) 设置view.layout()
     * @param y
     */
    public void scrollTopTo(int y) {
        mListView.layout(mListView.getLeft(), y, mListView.getRight(), this.getMeasuredHeight() + y);
        if (null != mOnPullHeightChangeListener) {
            mOnPullHeightChangeListener.onTopHeightChange(layoutHeader.getHeight(), y);
        }
    }

    public void animateTopTo(final int y) {
        ValueAnimator animator = ValueAnimator.ofInt(mListView.getTop(), y);
        animator.setDuration(300);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int frameValue = (Integer) animation.getAnimatedValue();
                mCurrentY = frameValue;
                scrollTopTo(frameValue);
                if (frameValue == y) {
                    isAnimation = false;
                }
            }
        });
        isAnimation = true;
        animator.start();
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        REFRESHING_TOP_HEIGHT = layoutHeader.getMeasuredHeight();
        REFRESHING_BOTTOM_HEIGHT = layoutFooter.getMeasuredHeight();

        MAX_PULL_TOP_HEIGHT = this.getMeasuredHeight();
        MAX_PULL_BOTTOM_HEIGHT = this.getMeasuredHeight();
    }

    @Override
    public void onFinishInflate() {

        mListView.setBackgroundColor(0xffffffff);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mListView.setOnScrollListener(this);
        this.addView(mListView);

        layoutHeader = (RelativeLayout) this.findViewById(R.id.layoutHeader);
        layoutFooter = (RelativeLayout) this.findViewById(R.id.layoutFooter);

        super.onFinishInflate();
    }


    public ListView getListView() {
        return this.mListView;
    }

    public void pullUp() {
        isRefreshing = false;
        if (mListView.getTop() > 0) {
            animateTopTo(0);
        } else if (mListView.getBottom() < this.getHeight()) {
            animateBottomTo(0);
        }

    }


    /**
     * @description: 在程序启动后渲染ListView的时候就会运行该方法，此时ListView的数据源还没有数据
     * @author: Ext
     * @time: 2016/3/16 17:44
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.i(TAG, "onScroll");
        if (null != mOnScrollListener) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        isBottom = false;
        if (mListView.getCount() > 0) {
            if ((firstVisibleItem + visibleItemCount) == totalItemCount) { // 到达底部
                View lastItem = mListView.getChildAt(visibleItemCount - 1);
                if (null != lastItem) {

                    // 最后一个条目的bottom == listView的高度
                    if (lastItem.getBottom() == mListView.getHeight()) {
                        Log.e("my", lastItem.getBottom() + "");
                        isBottom = true;
                    }
                }
            }
        }

        isTop = false;
        if (mListView.getCount() > 0) {
            if (firstVisibleItem == 0) {
                View firstItem = mListView.getChildAt(0);
                if (null != firstItem) {
                    if (firstItem.getTop() == 0) {
                        isTop = true;
                    }
                }
            }
        } else {
            isTop = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.i(TAG, "onScrollStateChanged");
        if (null != mOnScrollListener) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    // listener call back
    public interface OnPullHeightChangeListener {
        public void onTopHeightChange(int headerHeight, int pullHeight);

        public void onBottomHeightChange(int footerHeight, int pullHeight);

        public void onRefreshing(boolean isTop);
    }
}
