package com.example.fregmentbestpractice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NewsTitleFragment : Fragment() {

    private var isTwoPane = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.news_title_frag, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        isTwoPane = activity?.findViewById<View>(R.id.newsContentLayout) != null
        val layoutManager = LinearLayoutManager(activity)
        val newsTitleRecyclerView = activity?.findViewById<RecyclerView>(R.id.newsTitleRecyclerView)
        newsTitleRecyclerView?.layoutManager = layoutManager
        val adapter = NewsAdapter(getNews())
        newsTitleRecyclerView?.adapter = adapter
    }

    private fun getNews(): List<News>{
        val newList = ArrayList<News>()
        for (i in 1..50){
            val news = News("This is news title $i", getRandomLengthString("This is news content $i。"))
            newList.add(news)
        }
        return newList
    }

    private fun getRandomLengthString(str: String) = str.repeat((1..20).random())

    //编写适配器（利用内部类实现）
    inner class NewsAdapter(val newsList: List<News>) :
            RecyclerView.Adapter<NewsAdapter.ViewHolder>(){

                inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
                    val newsTitle: TextView = view.findViewById(R.id.newsTitle)
                }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            //获取点击项的实例
            val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
            val holder = ViewHolder(view)
            holder.itemView.setOnClickListener{
                val news = newsList[holder.absoluteAdapterPosition]
                if (isTwoPane){
                    //如果是双页模式，刷新NewsContentFragment
                    if (activity != null) {
                        val mainActivity = activity as MainActivity
                        val fragment =
                            mainActivity.supportFragmentManager.findFragmentById(R.id.newsContentFrag) as NewsContentFragment
                        fragment.refresh(news.title, news.content)
                    }
                }else{//如果单页模式，则直接启动NewsContentActivity
                    NewsContentActivity.actionStart(parent.context, news.title, news.content)
                }
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val news = newsList[position]
            holder.newsTitle.text = news.title
        }

        override fun getItemCount() = newsList.size
    }


}