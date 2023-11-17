package com.chaitanya.todoweathermvvmdi.di


import android.content.Context
import androidx.room.Room
import com.chaitanya.todoweathermvvmdi.data.api.WeatherDetailsApi
import com.chaitanya.todoweathermvvmdi.data.database.NoteDatabase
import com.chaitanya.todoweathermvvmdi.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoteModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "note_db.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun providesTopHeadlinesApi(retrofit: Retrofit): WeatherDetailsApi {
        return retrofit.create(WeatherDetailsApi::class.java)
    }


}