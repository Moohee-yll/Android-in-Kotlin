package com.example.fregmentbestpractice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class NewsContentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.news_content_frag, container, false)
    }

    fun refresh(title: String, content: String){
        if (activity != null){
            val newsContentActivity = activity as NewsContentActivity
            val contentLayout = newsContentActivity.findViewById<LinearLayout>(R.id.contentLayout)

            contentLayout.visibility = View.VISIBLE
            newsContentActivity.findViewById<TextView>(R.id.newsTitle).text = title //刷新新闻标题
            newsContentActivity.findViewById<TextView>(R.id.newsContent).text = content//刷新新闻内容
        }
    }

}