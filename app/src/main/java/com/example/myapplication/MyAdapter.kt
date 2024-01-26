package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class MyAdapter(private val context: Context, private val dataList: ArrayList<DataClass>) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(i: Int): Any {
        return dataList[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
        if (layoutInflater == null) {
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        var convertView = view
        if (convertView == null) {
            convertView = layoutInflater?.inflate(R.layout.grid_item, null)
        }

        val gridImage: ImageView = convertView!!.findViewById(R.id.gridImage)
        val gridCaption: TextView = convertView.findViewById(R.id.gridCaption)

        Glide.with(context).load(dataList[i].imageURL).into(gridImage)
        gridCaption.text = dataList[i].caption

        return convertView
    }
}