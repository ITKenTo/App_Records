package com.example.recorder

import android.content.res.Resources
import android.media.MediaPlayer
import android.media.PlaybackParams
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.postDelayed
import com.example.recorder.databinding.ActivityAudioPlayerBinding
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Duration

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay= 1000L
    private var jumpValue= 1000

    private var playbackSpeed = 1.0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_audio_player)

        binding= ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var filePath= intent.getStringExtra("filepath")
        var filename= intent.getStringExtra("filename")

        mediaPlayer= MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        binding.tvTrackDuration.text= dataFormat( mediaPlayer.duration)

        handler= Handler(Looper.getMainLooper())
        runnable= Runnable {
            binding.seekBar.progress= mediaPlayer.currentPosition
            binding.tvTrackProgess.text= dataFormat(mediaPlayer.currentPosition)
            handler.postDelayed(runnable, delay)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.tvFilename.text=filename

        binding.apply {
            btnPlay.setOnClickListener {
                playPausePlayer()
            }
            btnFor.setOnClickListener {
                mediaPlayer.seekTo(mediaPlayer.currentPosition+jumpValue)
                seekBar.progress+=jumpValue
            }
            btnBack.setOnClickListener {
                mediaPlayer.seekTo(mediaPlayer.currentPosition-jumpValue)
                seekBar.progress-=jumpValue
            }
            chip.setOnClickListener{
                if (playbackSpeed!=2f){
                    playbackSpeed+=0.5f
                }else{
                    playbackSpeed=0.5f
                }
                mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
                chip.text= "x $playbackSpeed"
            }

            seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                   if (fromUser){
                       mediaPlayer.seekTo(progress)
                   }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }
        playPausePlayer()
        binding.seekBar.max= mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            binding.btnPlay.background= ResourcesCompat.getDrawable(resources,R.drawable.baseline_play_circle_24,theme)
            handler.removeCallbacks(runnable)
        }
    }

    private fun playPausePlayer() {
       if (!mediaPlayer.isPlaying){
           mediaPlayer.start()
           binding.btnPlay.background= ResourcesCompat.getDrawable(resources,R.drawable.baseline_pause_circle_24,theme)
           handler.postDelayed(runnable,delay)
       }else{
           mediaPlayer.pause()
           binding.btnPlay.background= ResourcesCompat.getDrawable(resources,R.drawable.baseline_play_circle_24,theme)
           handler.removeCallbacks(runnable)
       }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }
    private fun dataFormat (duration: Int) :String{
        var d= duration/1000
        var s=d%60
        var m= (d/60 % 60)
        var h= ((d-m*60)/360).toInt()

        val f: NumberFormat= DecimalFormat("00")
        var str= "$m:${f.format(s)}"

        if (h>0) {
            str="$h:$str"
        }
        return str
    }
}