package com.cp.fishthebreak.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.cp.fishthebreak.di.local.AppDatabase
import com.cp.fishthebreak.di.local.TrollingDao
import com.cp.fishthebreak.utils.Constants.Companion.BASE_URL
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.worker.WorkerStarter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun provideHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(interceptor).readTimeout(15, TimeUnit.MINUTES)
            .connectTimeout(15, TimeUnit.MINUTES).build()
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory
    ): Retrofit {

        return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(gsonConverterFactory).build()
    }

    @Singleton
    @Provides
    fun provideAPIService(retrofit: Retrofit): ApiInterface =
        retrofit.create(ApiInterface::class.java)

    @Singleton
    @Provides
    fun getSharedPreference(context: Application): SharePreferenceHelper =
        SharePreferenceHelper.getInstance(context)

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "TrollingPoints"
        ).build()
    }

    @Singleton
    @Provides
    fun provideTrollingDao(appDatabase: AppDatabase): TrollingDao {
        return appDatabase.trollingDao()
    }

    @Singleton
    @Provides
    fun provideWorkerStarter(@ApplicationContext context: Context) = WorkerStarter(context)
}