package com.chaitanya.todoweathermvvmdi.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaitanya.todoweathermvvmdi.data.database.entity.NoteEntity
import com.chaitanya.todoweathermvvmdi.data.NoteRepository
import com.chaitanya.todoweathermvvmdi.model.WeatherResponse
import com.chaitanya.todoweathermvvmdi.utils.Constants.API_KEY
import com.chaitanya.todoweathermvvmdi.utils.DataHandler
import com.chaitanya.todoweathermvvmdi.utils.DetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository) : ViewModel() {

    //Local
    fun insertNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }
    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepository.update(note)
        }
    }
    fun updateNoteToPriority(noteId: Long){
        viewModelScope.launch {
            noteRepository.updateNoteToPriority(noteId)
        }
    }
    var allNotes = noteRepository.getAllNotes()

    var priorityNotes = noteRepository.getPriorityNotes()

    private val _detailState = MutableLiveData<DetailState>()
    val detailState : LiveData<DetailState> = _detailState

    fun setDetailState(state: DetailState){
        _detailState.value = state
    }

    private val _detailNote = MutableLiveData<NoteEntity>()
    val detailNote : LiveData<NoteEntity> = _detailNote

    fun setDetail(note: NoteEntity){
        _detailNote.value = note
    }

    //Network

    // Control whether to fetch location data.
    private val _fetchLocation = MutableLiveData<Boolean>().apply { value = true }
    val fetchLocation: LiveData<Boolean> = _fetchLocation

    fun shouldFetch(shouldFetch :Boolean){
        _fetchLocation.value = shouldFetch
    }

    // Set weather data received from the network.
    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData : LiveData<WeatherResponse> = _weatherData

    fun setWeatherData(weatherResponse: WeatherResponse){
         _weatherData.value = weatherResponse
    }


    //Fetch weather details and use DataHandler to represent various States
    private val _weatherDetails = MutableLiveData<DataHandler<WeatherResponse>>()
    val weatherDetails : LiveData<DataHandler<WeatherResponse>> = _weatherDetails

    fun getWeather(latitude : String,longitude :String) {
        _weatherDetails.postValue(DataHandler.LOADING())
        viewModelScope.launch {
            try {
                val response = noteRepository.getWeather(latitude,longitude, API_KEY)
                _weatherDetails.postValue(handleResponse(response))
            }catch (e:Exception){
                _weatherDetails.postValue(DataHandler.ERROR(message = e.message.toString()))
            }

        }
    }

    private fun handleResponse(response: Response<WeatherResponse>): DataHandler<WeatherResponse> {
        if (response.isSuccessful) {
            response.body()?.let { it ->
                return DataHandler.SUCCESS(it)
            }
        }
        return DataHandler.ERROR(message = response.errorBody().toString())
    }


}