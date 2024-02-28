package com.trava.user.webservices

import com.trava.utilities.SharedPrefs
import com.trava.utilities.constants.LANGUAGE_CHANGED
import com.trava.utilities.webservices.BaseRestClient

object RestClient : BaseRestClient() {
    private lateinit var restApi: API

    init {
        create()
    }

    private fun create() {
        val retrofit = createClient()
        restApi = retrofit.create(API::class.java)
    }

    private fun createWithouttoken() {
        val retrofit = createWithoutUserToken()
        restApi = retrofit.create(API::class.java)
    }

    fun recreate(): RestClient {
        create()
        return this
    }

    fun creaetWithoutHeader(): RestClient {
        createNew()
        return this
    }

    fun createWitjoutToken(): RestClient {
        createWithouttoken()
        return this
    }

    private fun createNew() {
        val retrofit = createWithoutHeaderClient()
        restApi = retrofit.create(API::class.java)
    }

    fun get(): API {
        if (SharedPrefs.get().getBoolean(LANGUAGE_CHANGED, true)) {
            recreate()
            SharedPrefs.get().save(LANGUAGE_CHANGED, false)
        }
        return restApi
    }
}