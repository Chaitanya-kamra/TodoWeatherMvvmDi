package com.chaitanya.todoweathermvvmdi.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity


@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteEntity: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("UPDATE `notes-table` SET isPriority = 1 WHERE id = :noteId")
    suspend fun updateNoteToPriority(noteId: Long)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM `notes-table`")
    fun getAllNotes(): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM `notes-table` WHERE isPriority = 1")
    fun getPriorityNotes(): LiveData<List<NoteEntity>>


}