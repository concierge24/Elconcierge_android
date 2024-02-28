package com.trava.utilities.webservices

import com.trava.utilities.Constants
import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.ACCESS_TOKEN_KEY
import com.trava.utilities.constants.LANGUAGE_CODE
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


open class BaseRestClient {
    fun createClient(): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        var hostnameVerifier: HostnameVerifier = HostnameVerifier { hostname, session ->
            val hv = HttpsURLConnection.getDefaultHostnameVerifier()
            hv.verify("royorides.io", session)
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val okHttpClient = OkHttpClient
                .Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .connectTimeout(90, TimeUnit.SECONDS)
                .hostnameVerifier { hostname, session -> true }
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain
                            .request()
                            .newBuilder()
                            .addHeader("language_id", SharedPrefs.get().getString(LANGUAGE_CODE, ""))
                            .addHeader("secretdbkey", Constants.SECRET_DB_KEY)
                            .addHeader("access_token", SharedPrefs.get().getString(ACCESS_TOKEN_KEY, ""))
                            .build()
                    chain.proceed(request)
                }.build()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }

    fun createWithoutUserToken(): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        var hostnameVerifier: HostnameVerifier = object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                val hv = HttpsURLConnection.getDefaultHostnameVerifier()
                return hv.verify("royorides.io", session)
            }
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val okHttpClient = OkHttpClient
                .Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .connectTimeout(90, TimeUnit.SECONDS)
                .hostnameVerifier { hostname, session -> true }
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain
                            .request()
                            .newBuilder()
                            .addHeader("secretdbkey", Constants.SECRET_DB_KEY)
                            .build()
                    chain.proceed(request)
                }.build()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }

    fun createWithoutHeaderClient(): Retrofit {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY// Create a trust manager that does not validate certificate chains
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        })

        var hostnameVerifier: HostnameVerifier = object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                val hv = HttpsURLConnection.getDefaultHostnameVerifier()
                return hv.verify("royorides.io", session)
            }
        }

        // Install the all-trusting trust manager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        val okHttpClient = OkHttpClient
                .Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .writeTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .connectTimeout(90, TimeUnit.SECONDS)
                .hostnameVerifier { hostname, session -> true }
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain
                            .request()
                            .newBuilder()
                            .addHeader("secretdbkey", Constants.SECRET_DB_KEY)
                            .build()
                    chain.proceed(request)
                }.build()

        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
    }

    companion object {


        @JvmField
        var DEV_URL: String = ""

        @JvmField
        var BASE_IMAGE_URL: String = ""

        @JvmField
        var LIVE_IMAGE_URL: String = ""

        @JvmField
        var BASE_IMAGE_CATEGORY: String = ""

        @JvmField
        var BASE_AD_URL: String = ""

        @JvmField
        var BASE_POLICIES: String = ""

        @JvmField
        var BASE_URL: String = ""

        @JvmField
        var BASE_CHAT_URL: String = ""


        init {

            when (Constants.SECRET_DB_KEY) {
                "c8181080fcadf2ab9230a1ebb921933048136663d8f5a9f694481bacd3363e08" -> {
                    Constants.SOCKET_URL = "https://ridesapi.marketplace24.ch/"  // Live URL
                    DEV_URL = "https://ridesapi.marketplace24.ch/v1/"  //Live URL
                    BASE_IMAGE_URL = "https://cpshift-assets.s3-us-west-2.amazonaws.com/images/"  // test images url
                    BASE_IMAGE_CATEGORY = "https://cpshift-assets.s3-us-west-2.amazonaws.com/BuraqExpress/public/images/"
                    BASE_AD_URL = "https://cpshift-assets.s3-us-west-2.amazonaws.com/BuraqExpress/public/images/"
                    BASE_POLICIES = "https://gomove.co/en"
                    LIVE_IMAGE_URL = "https://cpshift-assets.s3-us-west-2.amazonaws.com/BuraqExpress/public/images/"
                }
                "5025ff88429c4cb7b796cb3ee7128009" -> {
                    DEV_URL = "${Constants.SOCKET_URL}v1/"  //Live URL

                    BASE_IMAGE_URL = if (Constants.APP_SERVER == "PRODUCTION") {
                        "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/testImages/"
                    } else {
                        "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/testImages/"
                    }
                    BASE_IMAGE_CATEGORY = "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/BuraqExpress/public/images/"  //dev or test or production
                    BASE_AD_URL = "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/BuraqExpress/public/images/"
                    BASE_POLICIES = "https://corsa.netsolutionindia.com"
                    LIVE_IMAGE_URL = "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/BuraqExpress/public/images/"
                }
                "ab4170c712014d7a48edcbc44a919f6c" -> {
//                    DEV_URL = "${Constants.SOCKET_URL}v1/"  //Live URL

                 Constants. SOCKET_URL = "https://api-moov.biptech.ai/"  // Live URL
                  DEV_URL = "https://api-moov.biptech.ai/v1/"  //Live URL

                   BASE_URL = DEV_URL

                    BASE_IMAGE_URL = "https://moov-biplife-assets.s3.amazonaws.com/testImages/"  // test images url
                    BASE_CHAT_URL = DEV_URL

                   BASE_IMAGE_CATEGORY = "https://moov-biplife-assets.s3.amazonaws.com/BuraqExpress/public/images/"

                   BASE_AD_URL = "https://moov-biplife-assets.s3.amazonaws.com/BuraqExpress/public/images/"

                   BASE_POLICIES = "https://gomove.co/en"

                    LIVE_IMAGE_URL = "https://ghaytah-rides-assets.s3.me-south-1.amazonaws.com/BuraqExpress/public/images/"
                }
                else -> {
//                    DEV_URL = "${Constants.SOCKET_URL}v1/"  //Live URL
                    DEV_URL = "${Constants.SOCKET_URL}"  //Live URL
                    BASE_IMAGE_URL = if (Constants.APP_SERVER == "PRODUCTION") {
                        "https://cdn-royorides.imgix.net/prodimages/"
                    } else {
                        "https://cdn-royorides.imgix.net/testImages/"
                    }
                    BASE_IMAGE_CATEGORY = "https://cdn-royorides.imgix.net/BuraqExpress/public/images/"  //dev or test or production
                    BASE_AD_URL = "https://cdn-royorides.imgix.net/BuraqExpress/public/images/"
                    BASE_POLICIES = "https://corsa.netsolutionindia.com"
                    LIVE_IMAGE_URL = "https://cdn-royorides.imgix.net/BuraqExpress/public/images/"
                }
            }

            BASE_CHAT_URL = Constants.SOCKET_URL
            BASE_URL = DEV_URL
        }
    }
}


