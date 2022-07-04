package com.example.breweries.retrofit

import com.example.breweries.MainActivity.Companion.context
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    val api: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openbrewerydb.org")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(httpClient)
            .build()
            .create(ApiInterface::class.java)
    }


    //ADDED CACHE SO THE USER CAN NAVIGATE OFFLINE
    private val httpLoggingInterceptor = HttpLoggingInterceptor()
    private val httpCacheDirectory = File(context.cacheDir, "offlineCache")

    private val cache = Cache(httpCacheDirectory, 10 * 1024 * 1024) // 10 MB
    private val httpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(httpLoggingInterceptor)
        .addNetworkInterceptor(provideCacheInterceptor()!!)
        .addInterceptor(provideOfflineCacheInterceptor()!!)
        .build()

    private fun provideCacheInterceptor(): Interceptor? {
        return Interceptor { chain ->
            var request: Request = chain.request()
            val originalResponse: Response = chain.proceed(request)
            val cacheControl: String? = originalResponse.header("Cache-Control")
            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                cacheControl.contains("must-revalidate") || cacheControl.contains("max-stale=0")
            ) {
                val cc = CacheControl.Builder()
                    .maxStale(1, TimeUnit.DAYS)
                    .build()
                request = request.newBuilder()
                    .removeHeader("Pragma")
                    .cacheControl(cc)
                    .build()
                chain.proceed(request)
            } else {
                originalResponse

            }
        }
    }


    private fun provideOfflineCacheInterceptor(): Interceptor? {
        return Interceptor { chain ->
            try {
                return@Interceptor chain.proceed(chain.request())
            } catch (e: Exception) {
                val cacheControl = CacheControl.Builder()
                    .onlyIfCached()
                    .maxStale(1, TimeUnit.DAYS)
                    .build()
                val offlineRequest: Request = chain.request().newBuilder()
                    .cacheControl(cacheControl)
                    .removeHeader("Pragma")
                    .build()
                return@Interceptor chain.proceed(offlineRequest)
            }
        }
    }
}


