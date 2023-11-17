package com.chaitanya.todoweathermvvmdi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chaitanya.todoweathermvvmdi.data.database.NoteDao
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity

//function to obtain the DAO
@Database(entities = [NoteEntity::class],version = 2)
abstract class NoteDatabase :RoomDatabase() {
    abstract fun noteDao(): NoteDao
}