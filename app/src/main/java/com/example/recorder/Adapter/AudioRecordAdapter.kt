package com.example.recorder.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recorder.IclickListener
import com.example.recorder.Model.AudioRecord
import com.example.recorder.R
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date

class AudioRecordAdapter (var listRecord:ArrayList<AudioRecord>, var iclickListener: IclickListener):RecyclerView.Adapter<AudioRecordAdapter.ViewHolder>() {

    private var editMode=false
    fun isEditMode():Boolean {
        return editMode
    }

    fun setEditMode(mode:Boolean){
        if (editMode!=mode) {
            editMode=mode
            notifyDataSetChanged()
        }
    }
    inner class ViewHolder (itemView:View) :RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{

        var tv_filename:TextView= itemView.findViewById(R.id.tv_filename)
        var tv_Meta:TextView= itemView.findViewById(R.id.tv_media)
        var ckb:CheckBox= itemView.findViewById(R.id.checkbok)
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
        override fun onClick(v: View?) {
            val position= adapterPosition
            if (position!=RecyclerView.NO_POSITION){
                iclickListener.onclick(position)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position= adapterPosition
            if (position!=RecyclerView.NO_POSITION){
                iclickListener.onlongclick(position)
            }
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       var view:View = LayoutInflater.from(parent.context).inflate(R.layout.item_record,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listRecord.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var sdf= SimpleDateFormat("dd/MM/yyyy")
        var date= Date(listRecord[position].timestamp)
        var strDate = sdf.format(date)
       holder.tv_filename.text= listRecord[position].filename
        holder.tv_Meta.text= "${listRecord[position].duration}$strDate"


        if (editMode){
            holder.ckb.visibility=View.VISIBLE
            holder.ckb.isChecked= listRecord[position].isChecked
        }else{
            holder.ckb.visibility=View.GONE
            holder.ckb.isChecked= false
        }

    }
}