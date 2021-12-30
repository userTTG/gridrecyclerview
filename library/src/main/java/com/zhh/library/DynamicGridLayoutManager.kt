package com.zhh.library

import android.animation.Animator
import kotlin.jvm.JvmOverloads
import androidx.recyclerview.widget.RecyclerView
import android.animation.ValueAnimator
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView.Recycler
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.View

class DynamicGridLayoutManager @JvmOverloads constructor(
    column: Int = DEFAULT_COLUMN_SIZE,
    row: Int = DEFAULT_ROW_SIZE,
    @RecyclerView.Orientation orientation: Int = RecyclerView.HORIZONTAL
) : RecyclerView.LayoutManager() {
    private var mColumnSize = 0
    private var mRowSize = 0
    private var mSelectedIndex = 0

    @RecyclerView.Orientation
    var mOrientation: Int
    private var mHorizontalOffset = 0
    private val mVerticalOffset = 0
    private var preWidth = 0
    private val preHeight = 0
    private var selectAnimator: ValueAnimator? = null
    var onSelectedChangeListener: OnSelectedChangeListener? = null
    private var lockLayout = false
    fun setLockLayout(lockLayout: Boolean) {
        this.lockLayout = lockLayout
    }// getWidth() / 2 + childWidth / 2 +

    /**
     * x最大偏移量
     *
     * @return
     */
    private val maxXOffset: Int
        private get() = if (itemCount == 0 || itemCount <= mColumnSize * mRowSize) {
            0
        } else Math.max(0, width * (itemCount / (mColumnSize * mRowSize)))
    // getWidth() / 2 + childWidth / 2 +
// getWidth() / 2 + childWidth / 2 +
    /**
     * y最大偏移量
     *
     * @return
     */
    private val maxYOffset: Int
        private get() = if (itemCount == 0 || itemCount <= mColumnSize * mRowSize) {
            0
        } else width * (itemCount / (mColumnSize * mRowSize))

    // getWidth() / 2 + childWidth / 2 +
    override fun canScrollHorizontally(): Boolean {
        return !lockLayout && mOrientation == LinearLayout.HORIZONTAL
    }

    override fun canScrollVertically(): Boolean {
        return mOrientation == LinearLayout.VERTICAL
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
        return if (mOrientation == LinearLayout.VERTICAL) {
            0
        } else scrollBy(dx, recycler, state)
    }

    override fun scrollVerticallyBy(
        dy: Int, recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        return if (mOrientation == LinearLayout.HORIZONTAL) {
            0
        } else scrollBy(dy, recycler, state)
    }

    private fun scrollBy(offset: Int, recycler: Recycler, state: RecyclerView.State): Int {
        if (childCount == 0 || offset == 0) {
            return 0
        }

//        if (mOrientation == LinearLayout.HORIZONTAL){
//
//        }
        //----------------1、边界检测-----------------
        if (mHorizontalOffset <= 0 && offset < 0) {
            return 0
        }
        if (mHorizontalOffset >= maxXOffset && offset > 0) {
            return 0
        }
        mHorizontalOffset += offset
        if (mHorizontalOffset < 0) {
            mHorizontalOffset = 0
        }
        if (mHorizontalOffset > maxXOffset) {
            mHorizontalOffset = maxXOffset
        }

        //暂时分离和回收全部有效的Item
        detachAndScrapAttachedViews(recycler)

        //开始布局
        onLayout(recycler)
        return offset
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onMeasure(
        recycler: Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        val w = width
        if (preWidth == 0 && w != 0) {
            mHorizontalOffset = width * (mSelectedIndex / (mColumnSize * mRowSize))
        }
        if (preWidth != w) {
            if (preWidth != 0) {
                mHorizontalOffset = mHorizontalOffset * w / preWidth
            }
            preWidth = w
        }
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            //没有Item可布局，就回收全部临时缓存 (参考自带的LinearLayoutManager)
            //这里的没有Item，是指Adapter里面的数据集，
            //可能临时被清空了，但不确定何时还会继续添加回来
            removeAndRecycleAllViews(recycler)
            return
        }
        //暂时分离和回收全部有效的Item
        detachAndScrapAttachedViews(recycler)

        //开始布局
        onLayout(recycler)
    }

    /**
     * 确定Item位置，角度以及尺寸
     *
     */
    private fun onLayout(recycler: Recycler) {
        val start = System.currentTimeMillis()
        var x: Int
        var y: Int
        val itemWidth = this.width / mColumnSize
        val itemHeight = this.height / mRowSize
        val pageIndex = (mHorizontalOffset / width)
        val startXIndex: Int
        val endXIndex: Int
        val startYIndex: Int
        val endYIndex: Int
        if (mHorizontalOffset % itemWidth == 0) {
            startXIndex = mHorizontalOffset / itemWidth
            endXIndex = startXIndex + mColumnSize
            startYIndex = 0
            endYIndex = mRowSize
        } else {
            startXIndex = mHorizontalOffset / itemWidth
            endXIndex = startXIndex + mColumnSize + 1
            startYIndex = 0
            endYIndex = mRowSize
        }
        var item: View? = null
        val offset = mHorizontalOffset % itemWidth
        for (yIndex in startYIndex until endYIndex) {
            for (xIndex in startXIndex until endXIndex) {
                //根据position获取View
                val videoIndex = getVideoIndex(xIndex, yIndex)
                if (videoIndex < 0 || videoIndex >= itemCount) {
                    continue
                }
                item = recycler.getViewForPosition(getVideoIndex(xIndex, yIndex))
                //添加进去，当然里面不一定每次都是调用RecyclerView的addView方法的，
                //如果是从缓存区里面找到的，只需调用attachView方法把它重新连接上就行了。
                addView(item)
                //测量item，当然，也不是每次都会调用measure方法进行测量的，
                //它里面会判断，如果已经测量过，而且当前尺寸又没有收到更新的通知，就不会重新测量。
                measureChild(item, this.width - itemWidth, this.height - itemHeight)
                x = (xIndex - startXIndex) * itemWidth - offset
                y = (yIndex - startYIndex) * itemHeight
                layoutDecorated(item, x, y, x + itemWidth, y + itemHeight)
            }
        }
        recycleChildren(recycler)
    }

    /**
     * 回收需回收的Item。
     */
    private fun recycleChildren(recycler: Recycler) {
        val scrapList = recycler.scrapList
        for (i in scrapList.indices) {
            val holder = scrapList[i]
            removeAndRecycleView(holder.itemView, recycler)
        }
    }

    private fun getVideoIndex(x: Int, y: Int): Int {
        return x / mColumnSize * mColumnSize * mRowSize + x % mColumnSize + y * mColumnSize
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            RecyclerView.SCROLL_STATE_DRAGGING ->                 //当手指按下时，停止当前正在播放的动画
                cancelAnimator()
            RecyclerView.SCROLL_STATE_IDLE -> {}
            else -> {}
        }
    }

    /**
     * 平滑滚动到下一页
     */
    fun smoothScrollToNextPage() {
        val index = mSelectedIndex + mColumnSize * mRowSize
        if (index > -1 && index < itemCount) {
            mSelectedIndex = index / (mColumnSize * mRowSize) * mColumnSize * mRowSize
        }
        smoothScrollToPosition(mSelectedIndex)
    }

    /**
     * 平滑滚动到上一页
     */
    fun smoothScrollToPrePage() {
        val index = mSelectedIndex - mColumnSize * mRowSize
        if (index > -1 && index < itemCount) {
            mSelectedIndex = index / (mColumnSize * mRowSize) * mColumnSize * mRowSize
        }
        smoothScrollToPosition(mSelectedIndex)
    }

    /**
     * 自动平滑滚动到适当页
     */
    fun smoothScrollToAutoPage() {
        val curPageIndex = mHorizontalOffset / width
        val toPageIndex =
            if (mHorizontalOffset % width > width / 2) curPageIndex + 1 else curPageIndex
        if ((mSelectedIndex < toPageIndex * mColumnSize * mRowSize || mSelectedIndex >= (toPageIndex + 1) * mColumnSize * mRowSize) && toPageIndex * mColumnSize * mRowSize < itemCount && toPageIndex * mColumnSize * mRowSize > -1) {
            mSelectedIndex = toPageIndex * mColumnSize * mRowSize
        }
        smoothScrollToPosition(mSelectedIndex)
    }

    /**
     * 平滑滚动到某个位置
     *
     * @param position 目标Item索引
     */
    fun smoothScrollToPosition(position: Int) {
        if (position > -1 && position < itemCount) {
            startValueAnimator(position)
        } else {
            startValueAnimator(mSelectedIndex)
        }
    }

    override fun scrollToPosition(position: Int) {
        mSelectedIndex = position
        mHorizontalOffset = position / (mColumnSize * mRowSize) * width
        requestLayout()
    }

    private fun startValueAnimator(position: Int) {
        cancelAnimator()
        val distance = position / (mColumnSize * mRowSize) * width - mHorizontalOffset
        if (distance == 0) {
            return
        }
        val minDuration: Long = 100
        val maxDuration: Long = 300
        val duration: Long
        val distanceFraction = Math.abs(distance) / width
        duration = (minDuration + (maxDuration - minDuration) * distanceFraction)
        selectAnimator = ValueAnimator.ofInt(0, distance)
        selectAnimator?.setDuration(duration)
        selectAnimator?.setInterpolator(LinearInterpolator())
        val startedOffset = mHorizontalOffset
        selectAnimator?.addUpdateListener(AnimatorUpdateListener { animation ->
            val value = animation.animatedValue as Int
            mHorizontalOffset = startedOffset + value
            requestLayout()
            Log.e(TAG, "onAnimationUpdate:$mHorizontalOffset")
        })
        selectAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (onSelectedChangeListener != null) {
                    onSelectedChangeListener!!.selectedChanged(position)
                }
                Log.e(TAG, "onAnimationEnd:$mHorizontalOffset")
            }
        })
        selectAnimator?.start()
    }

    /**
     * 取消动画
     */
    fun cancelAnimator() {
        if (selectAnimator != null && (selectAnimator!!.isStarted || selectAnimator!!.isRunning)) {
            selectAnimator!!.cancel()
        }
    }

    fun setColumnRow(column: Int, row: Int, position: Int) {
        if (lockLayout) {
            return
        }
        val toPageNum = position / (column * row)
        mSelectedIndex = position
        mHorizontalOffset = toPageNum * width
        mColumnSize = column
        mRowSize = row
        requestLayout()
    }

    fun getmSelectedIndex(): Int {
        return mSelectedIndex
    }

    fun setmSelectedIndex(mSelectedIndex: Int) {
        this.mSelectedIndex = mSelectedIndex
        if (width != 0) {
            mHorizontalOffset = width * (mSelectedIndex / (mColumnSize * mRowSize))
        }
    }

    interface OnSelectedChangeListener {
        fun selectedChanged(cur: Int)
    }

    companion object {
        private const val TAG = "DynamicGridLayoutManage"
        private const val DEFAULT_COLUMN_SIZE = 1
        private const val DEFAULT_ROW_SIZE = 1
    }

    init {
        mColumnSize = if (column < 1) {
            1
        } else {
            column
        }
        mRowSize = if (row < 1) {
            1
        } else {
            row
        }
        mOrientation = orientation
    }
}