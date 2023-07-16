package com.example.recorder.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.recorder.Model.AudioRecord

@Dao
interface AudioRecorDao {

    @Query("Select * from audiorecords")
    fun getAll(): List<AudioRecord>

    @Query("Select * from audiorecords where filename  LIKE :query")
    fun getAllSearch(query: String): List<AudioRecord>

    @Insert
    fun insert(audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecord: AudioRecord)

    @Delete
    fun delete(audioRecord: Array<AudioRecord>)

    @Update
    fun update(audioRecord: AudioRecord)

}