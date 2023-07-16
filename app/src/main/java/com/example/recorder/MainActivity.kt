package com.example.recorder

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore.Video.Media
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.recorder.Database.AppDatabase
import com.example.recorder.Model.AudioRecord
import com.example.recorder.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.SimpleTimeZone

const val REQUEST_CODE=200

class MainActivity : AppCompatActivity() , Timer.OnTimerTickListener{
    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var binding: ActivityMainBinding
    private var permission= arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted= false

    private lateinit var recorder:MediaRecorder
    private var dirPath=""
    private var filename=""

    private var isRecording= false
    private var isPaused= false
    private var duration=""

    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var db:AppDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted= ActivityCompat.checkSelfPermission(this,permission[0])==PackageManager.PERMISSION_GRANTED
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this,permission, REQUEST_CODE)
        }

        db= AppDatabase.getDatabase(this)

        timer= Timer(this)
        vibrator= getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.btnRecord.setOnClickListener {
            when {
                isPaused->resumeRecording()
                isRecording->pauseRecorder()
                else ->startRecording()
            }
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        binding.apply {
            btnlist.setOnClickListener {
               startActivity(Intent(this@MainActivity, RecorderActivity::class.java))
            }
            btnDone.setOnClickListener {
                stopRecorder()
                showBottomSheet()
                Toast.makeText(this@MainActivity,"Đã Luu",Toast.LENGTH_SHORT).show()
            }
            btnDelete.setOnClickListener {
                stopRecorder()
                File("$dirPath$filename.mp3")
            }
            btnDelete.isClickable=false
        }
    }

    private fun resumeRecording() {
        recorder.resume()
        isPaused= false
        binding.btnRecord.setImageResource(R.drawable.baseline_pause_24)
        timer.start()

    }

    private fun pauseRecorder() {
        recorder.pause()
        isPaused=true
        binding.btnRecord.setImageResource(R.drawable.boder_1)
        timer.pause()
    }

    private fun stopRecorder(){
        timer.stop()
        recorder.apply {
            stop()
            release()
        }
        isPaused=false
        isRecording=false
     binding.apply {
         btnlist.visibility=View.VISIBLE
         btnDone.visibility=View.GONE

         btnDelete.isClickable=false
         btnDelete.setImageResource(R.drawable.round_clear_24)
         btnRecord.setImageResource(R.drawable.boder_1)
         tvTimer.setText("00:00:00")
         amplitudes= waveformview.clear()

     }



    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== REQUEST_CODE) {
            permissionGranted= grantResults[0]== PackageManager.PERMISSION_GRANTED
        }
    }
    private fun startRecording(){
        if (!permissionGranted){
            ActivityCompat.requestPermissions(this,permission, REQUEST_CODE)
            return
        }
        //start
        recorder= MediaRecorder()
        dirPath= "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat= SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date:String= simpleDateFormat.format(Date())
        filename="audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {

                prepare()
            }catch (e:IOException){

            }
            start()
        }
        binding.btnRecord.setImageResource(R.drawable.baseline_pause_24)
        isRecording= true
        isPaused=false
        timer.start()
        binding.apply {
            btnDelete.isClickable=true
            btnDelete.setImageResource(R.drawable.round_clear_24)

            btnlist.visibility=View.GONE
            btnDone.visibility=View.VISIBLE
        }
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text=duration
        this.duration=duration
        binding.waveformview.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    fun showBottomSheet(){
       val view:View = layoutInflater.inflate(R.layout.bottom_sheet,null)
        val dialog= BottomSheetDialog(this)
        dialog.setContentView(view)
        val ed_save : EditText
        ed_save= view.findViewById<EditText>(R.id.filename)
        ed_save.setText(filename)
        view.findViewById<Button>(R.id.btn_cancle).setOnClickListener {
            File("$dirPath$filename.mp3").delete()

            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            Save(ed_save.text.toString())
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun Save(newfilename:String) {
        if (newfilename != filename) {
            var newFile=   File("$dirPath$newfilename.mp3")
            File("$dirPath$filename.mp3").renameTo(newFile)
        }

        var filePath= "$dirPath$newfilename.mp3"
        var timestamp= Date().time
        var ampsPath= "$dirPath$newfilename"

        try {
            var fos = FileOutputStream(ampsPath)
            var out = ObjectOutputStream(fos)

            out.writeObject(amplitudes)
            fos.close()
            out.close()
        }catch (e:IOException){}

        var recorder= AudioRecord(newfilename,filePath,timestamp,duration,ampsPath)
        GlobalScope.launch {
            db.audiorecord().insert(recorder)
        }
    }
}