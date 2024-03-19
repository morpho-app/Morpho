package com.morpho.butterfly.auth

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath


class LoginRepository(
    val dir: String,
    val key: String = ""
) {
    private val authStore: KStore<AuthInfo> = storeOf(
        file = "$dir/jwt_$key.json".toPath(),
        default = null,
        enableCache = true
    )

    var auth: AuthInfo?
        get() {
            return runBlocking { authStore.get() }
        }
        set(value) { runBlocking { authStore.set(value) } }

    fun auth(): Flow<AuthInfo?> = authStore.updates

    private val credentialsStore: KStore<Credentials> = storeOf(
        file = "$dir/login_$key.json".toPath(),
        default = null,
        enableCache = true
    )

    var credentials: Credentials?
        get() {
            return runBlocking { credentialsStore.get() }
        }
        set(value) { runBlocking { credentialsStore.set(value) } }

    fun credentials(): Flow<Credentials?> = credentialsStore.updates

    constructor(
        dir: String,
        key: String = "",
        auth: AuthInfo,
        credentials: Credentials) : this(dir, key) {
            this.auth = auth
            this.credentials = credentials
        }
}