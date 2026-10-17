package com.mohitkumar.docscanner.di

import android.content.Context
import androidx.room.Room
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mohitkumar.docscanner.BuildConfig
import com.mohitkumar.docscanner.data.local.AppDatabase
import com.mohitkumar.docscanner.data.local.ScanDao
import com.mohitkumar.docscanner.data.remote.SummaryApi
import com.mohitkumar.docscanner.data.repository.ScanRepositoryImpl
import com.mohitkumar.docscanner.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "scans.db").build()

    @Provides
    fun scanDao(db: AppDatabase): ScanDao = db.scanDao()

    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides @Singleton
    fun summaryApi(client: OkHttpClient): SummaryApi = Retrofit.Builder()
        .baseUrl(BuildConfig.SUMMARY_API_BASE_URL)
        .client(client)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SummaryApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun scanRepository(impl: ScanRepositoryImpl): ScanRepository
}
