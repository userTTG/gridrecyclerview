package com.zhh.library


import kotlin.jvm.JvmOverloads
import androidx.recyclerview.widget.RecyclerView
import com.zhh.library.DynamicGridLayoutManager.OnSelectedChangeListener
import androidx.recyclerview.widget.SimpleItemAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class VideoRecyclerLayout<VideoInfo> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle), OnSelectedChangeListener {

    private var mVideoLayoutMode = Mode.SINGLE
    private var mLayoutManager: DynamicGridLayoutManager? = null
    private val mSelectedIndex = 0
    private var onSelectedChangeListener: OnSelectedChangeListener? = null
    private val onSelectVideoTimeChange: OnSelectVideoTimeChange? = null
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val scaleW = 16
        val scaleH = 9
        val w = MeasureSpec.getSize(widthSpec)
        val h = MeasureSpec.getSize(heightSpec)
        val toW: Int
        val toH: Int
        toH = w * scaleH / scaleW
        toW = if (w > h || toH > h) {
//            toH = h;
//            toW = toH *scaleW/scaleH;
            super.onMeasure(widthSpec, heightSpec)
            return
        } else {
            w
        }
        val wMode = MeasureSpec.getMode(widthSpec)
        val hMode = MeasureSpec.getMode(heightSpec)
        val theWidthMeasureSpec = MeasureSpec.makeMeasureSpec(toW, wMode)
        val theHeightMeasureSpec = MeasureSpec.makeMeasureSpec(toH, hMode)
        super.onMeasure(theWidthMeasureSpec, theHeightMeasureSpec)
        setMeasuredDimension(toW, toH)
    }

    private fun init() {
        if (itemAnimator != null && itemAnimator is SimpleItemAnimator) {
            (itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        }
    }

    fun setData(data: List<VideoInfo>?, selectedIndex: Int) {
        if (itemAnimator != null && itemAnimator is SimpleItemAnimator) {
            (itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        }
        mLayoutManager = DynamicGridLayoutManager()
        mLayoutManager!!.onSelectedChangeListener = this
        this.layoutManager = mLayoutManager
    }

    override fun selectedChanged(cur: Int) {
        Log.e(TAG, "selectedChanged:$cur")
        if (mSelectedIndex != cur) {
            //在选中前处理，不需要tag
//            selectItem(cur);
        }
    }

    fun setOnSelectedChangeListener(onSelectedChangeListener: OnSelectedChangeListener?) {
        this.onSelectedChangeListener = onSelectedChangeListener
    }

    val selectView: View?
        get() = if (mLayoutManager == null) {
            null
        } else mLayoutManager!!.findViewByPosition(mSelectedIndex)
    private var flingNotHandled = true
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val result: Boolean
        //当滑动速度为0时，fling不会被触发，此时交由onTouchEvent处理
        if (e.action == MotionEvent.ACTION_UP) {
            flingNotHandled = true
            result = super.onTouchEvent(e)
            if (flingNotHandled) {
                mLayoutManager!!.smoothScrollToAutoPage()
            }
        } else {
            result = super.onTouchEvent(e)
        }
        return result
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val SPEED = 1500
        if (mLayoutManager != null) {
            if (velocityX > SPEED) {
                mLayoutManager!!.smoothScrollToNextPage()
            } else if (velocityX < -SPEED) {
                mLayoutManager!!.smoothScrollToPrePage()
            } else {
                mLayoutManager!!.smoothScrollToAutoPage()
            }
        }
        flingNotHandled = false
        return false
    }

    var videoLayoutMode: Int
        get() = mVideoLayoutMode
        set(mode) {
            if (mLayoutManager == null) {
                return
            }
            when (mode) {
                Mode.SINGLE -> mLayoutManager!!.setColumnRow(1, 1, mSelectedIndex)
                Mode.MULTI_4 -> mLayoutManager!!.setColumnRow(2, 2, mSelectedIndex)
                else -> {}
            }
            mVideoLayoutMode = mode
        }

    object Mode {
        const val NONE = 0
        const val SINGLE = 1
        const val MULTI_4 = 2

        @IntDef(SINGLE, MULTI_4)
        @Retention(RetentionPolicy.SOURCE)
        annotation class VideoLayoutMode
    }

    //避免滑动卡顿，没有使用adapter的notify
    private fun selectItem(index: Int) {
//        if (mAdapter!= null){
//            if (index >-1 && index<mAdapter.getData().size()){
//                if (mSelectedIndex != index){
//                    mAdapter.getData().get(mSelectedIndex).checked = false;
//                    mAdapter.getData().get(index).checked = true;
//                }
//            }
//        }
//        if (mLayoutManager != null){
//            View pre = mLayoutManager.findViewByPosition(mSelectedIndex);
//            Log.e(TAG,"selectItem pre:"+(pre == null) + ",is main:"+ VUtils.isMainThread());
//            if ((pre instanceof VideoPlayer)){
//                VideoPlayer videoPlayerPre = (VideoPlayer)pre;
//                videoPlayerPre.setSelected(false);
//                videoPlayerPre.setOnVideoStatusChange(null);
//            }
//            View cur = mLayoutManager.findViewByPosition(index);
//            Log.e(TAG,"selectItem cur:"+(cur == null));
//            if ((cur instanceof VideoPlayer)){
//                VideoPlayer videoPlayerCur = (VideoPlayer)cur;
//                videoPlayerCur.setSelected(true);
//                addVideoStatusChangeListener(videoPlayerCur);
//            }
//            mSelectedIndex = index;
//        }
    }

    interface OnSelectVideoTimeChange {
        fun changed(time: Long?)
    }

    companion object {
        private const val TAG = "VideoRecyclerLayout"
    }
}