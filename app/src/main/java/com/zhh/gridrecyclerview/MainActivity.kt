package com.zhh.gridrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhh.gridrecyclerview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var mBinding:ActivityMainBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        val data:MutableList<String> = mutableListOf("https://lmg.jj20.com/up/allimg/tp09/210611094Q512b-0-lp.jpg");
        mBinding.vrlImages.adapter = ImageAdapter(data);
//        mBinding.vrlImages.layoutManager = LinearLayoutManager(this)
    }
}