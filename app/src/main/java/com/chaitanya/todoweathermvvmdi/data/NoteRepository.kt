package com.chaitanya.todoweathermvvmdi.data

import androidx.lifecycle.LiveData
import com.chaitanya.todoweathermvvmdi.data.api.WeatherDetailsApi
import com.chaitanya.todoweathermvvmdi.data.database.NoteDatabase
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.model.WeatherResponse
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(val appDatabase: NoteDatabase, val weatherDetailsApi: WeatherDetailsApi) {

    //Local
    suspend fun insertNote(note: NoteEntity): Long {
        return appDatabase.noteDao()
            .insert(note)
    }
    suspend fun delete(note: NoteEntity) {
        appDatabase.noteDao().delete(note)
    }
    suspend fun update(note: NoteEntity) {
        appDatabase.noteDao().update(note)
    }
    suspend fun updateNoteToPriority(noteId: Long){
        appDatabase.noteDao().updateNoteToPriority(noteId)
    }

    fun getAllNotes(): LiveData<List<NoteEntity>> {
        return appDatabase.noteDao().getAllNotes()
    }

    fun getPriorityNotes(): LiveData<List<NoteEntity>>{
        return appDatabase.noteDao().getPriorityNotes()
    }




    //Network
    suspend fun getWeather(latitude : String,longitude :String,apiKey : String) : Response<WeatherResponse>{
        val location = "$latitude,$longitude"
        return weatherDetailsApi.getWeather(apiKey,location)
    }
}
