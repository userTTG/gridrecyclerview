package com.zhh.gridrecyclerview

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@ClassName ImageAdapter
 *@Description TODO
 *@Author zhangh-be
 *@Date 2021/12/30 17:11
 *@Version 1.0
 */
class ImageAdapter(data: MutableList<String>) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId = R.layout.item_image, data) {
    override fun convert(holder: BaseViewHolder, item: String) {
        Glide.with(holder.itemView).load(item)
            .into(holder.getView(R.id.iv_picture))
    }
}