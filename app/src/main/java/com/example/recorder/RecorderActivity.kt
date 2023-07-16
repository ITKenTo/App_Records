package com.example.recorder

import android.animation.TimeAnimator
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ResourceCursorAdapter
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recorder.Adapter.AudioRecordAdapter
import com.example.recorder.Database.AppDatabase
import com.example.recorder.Model.AudioRecord
import com.example.recorder.databinding.ActivityRecorderBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecorderActivity : AppCompatActivity(), IclickListener {
    private lateinit var binding: ActivityRecorderBinding
    private lateinit var madapter: AudioRecordAdapter
    private lateinit var db: AppDatabase
    private lateinit var list: ArrayList<AudioRecord>
    private var allCheck= false
    private lateinit var bottomSheet:LinearLayout

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_recorder)
        binding = ActivityRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list= ArrayList();
        db = AppDatabase.getDatabase(this)

       // Log.d("TAG", list.size.toString())
        madapter= AudioRecordAdapter(list,this)
        binding.recyRecord.apply {
            adapter=madapter
            layoutManager=LinearLayoutManager(context)
        }
        fetchData()

        setSupportActionBar(binding.toobar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toobar.setNavigationOnClickListener {
            onBackPressed()
        }

        bottomSheet=findViewById(R.id.bottomSheet)
        bottomSheetBehavior= BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state= BottomSheetBehavior.STATE_HIDDEN

        binding.edSearch.addTextChangedListener (object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var query =s.toString()
                Log.d("TEXT", s.toString())

                searchData(query)
            }

            override fun afterTextChanged(s: Editable?) {
            }

            })

        binding.btnClose.setOnClickListener {
           leveEditMode()
        }

        binding.btnSelect.setOnClickListener {
            allCheck=!allCheck
            list.map {
                it.isChecked=allCheck
            }
            madapter.notifyDataSetChanged()

            if (allCheck){
                disableRename()
                enableDelete()
            }else{
                disableRename()
                disableRename()
            }
        }

        binding.btnDelete.setOnClickListener {
            val  builder= AlertDialog.Builder(this)
            builder.setTitle("Xóa bản ghi ?")
            val nbrecords= list.count { it.isChecked}
            builder.setMessage("Bạn có chắc chắn muốn xóa $nbrecords bản ghi không ?")
            builder.setPositiveButton("Xóa"){_, _->
                val toDelete= list.filter { it.isChecked }.toTypedArray()
                GlobalScope.launch {
                    db.audiorecord().delete(toDelete)
                    runOnUiThread {
                        list.removeAll(toDelete)
                        madapter.notifyDataSetChanged()
                        leveEditMode()
                    }
                }
            }
            builder.setNegativeButton("Hủy"){_,_ ->

            }
            val  dialog= builder.create()
            dialog.show()
        }

        binding.btnedit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = this.layoutInflater.inflate(R.layout.rename_layout,null)
            builder.setView(dialogView)
            val dialog =builder.create()
            var record= list.filter { it.isChecked }.get(0)
            val textinput = dialogView.findViewById<EditText>(R.id.ed_rename)
            textinput.setText(record.filename)
            dialogView.findViewById<Button>(R.id.btn_rename).setOnClickListener {
                val input = textinput.text.toString()
                if (input.isEmpty()) {
                    Toast.makeText(this,"Tên file không được trống",Toast.LENGTH_SHORT).show()
                }else{
                    record.filename=input
                    GlobalScope.launch {
                        db.audiorecord().update(record)
                        runOnUiThread {
                            madapter.notifyItemChanged(list.indexOf(record))
                            leveEditMode()
                            dialog.dismiss()
                        }
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.btn_cancle).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun leveEditMode(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.editBar.visibility= View.GONE
        bottomSheetBehavior.state= BottomSheetBehavior.STATE_HIDDEN
//        bottomSheetBehavior.state= BottomSheetBehavior.STATE_
//        binding.bottomSheet.visibility=View.GONE

        list.map {
            it.isChecked=false
        }
        madapter.setEditMode(false)
    }
    private fun disableRename(){
        binding.apply {
            btnedit.isClickable=false
            btnedit.backgroundTintList= ResourcesCompat.getColorStateList(resources,R.color.grayDarkDisable,theme)
            tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources,R.color.grayDarkDisable,theme))
        }
    }
    private fun disableDelete(){
        binding.apply {
            btnDelete.isClickable=false
            btnDelete.backgroundTintList= ResourcesCompat.getColorStateList(resources,R.color.grayDarkDisable,theme)
            tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources,R.color.grayDarkDisable,theme))
        }
    }

    private fun enableRename(){
        binding.apply {
            btnedit.isClickable=true
            btnedit.backgroundTintList= ResourcesCompat.getColorStateList(resources,R.color.grayDark,theme)
            tvEdit.setTextColor(ResourcesCompat.getColorStateList(resources,R.color.grayDark,theme))
        }
    }
    private fun enableDelete(){
        binding.apply {
            btnDelete.isClickable=true
            btnDelete.backgroundTintList= ResourcesCompat.getColorStateList(resources,R.color.grayDark,theme)
            tvDelete.setTextColor(ResourcesCompat.getColorStateList(resources,R.color.grayDark,theme))
        }
    }

    private fun searchData(query: String) {
        GlobalScope.launch {
            list.clear()
            var listtt = db.audiorecord().getAllSearch("%$query%")
            list.addAll(listtt);

            runOnUiThread{
                madapter.notifyDataSetChanged()
            }

        }

    }

    private fun fetchData() {
        GlobalScope.launch {
            list.clear()
           var listtt = db.audiorecord().getAll()
            list.addAll(listtt);
            madapter.notifyDataSetChanged()

            Log.d("TAG", list.size.toString())

        }
    }

    override fun onclick(pos: Int) {

        if (madapter.isEditMode()) {
            list[pos].isChecked= !list[pos].isChecked
            madapter.notifyItemChanged(pos)
            var nbSelected= list.count { it.isChecked }

            when (nbSelected) {
                0->{
                    disableRename()
                    disableDelete()
                }
                1->{
                    enableDelete()
                    enableRename()
                }
                else->{
                    disableRename()
                    enableDelete()
                }
            }
        }else{
            var audioRecord= list[pos]
            var intent= Intent(this,AudioPlayerActivity::class.java)
            intent.putExtra("filepath",audioRecord.filePath)
            intent.putExtra("filename",audioRecord.filename)
            startActivity(intent)
        }

    }

    override fun onlongclick(pos: Int) {
        madapter.setEditMode(true)
        list[pos].isChecked= !list[pos].isChecked
        madapter.notifyItemChanged(pos)
        bottomSheetBehavior.state= BottomSheetBehavior.STATE_EXPANDED

        if (madapter.isEditMode() && binding.editBar.visibility==View.GONE){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
            binding.editBar.visibility= View.VISIBLE
            enableDelete()
            enableRename()
        }

    }
}