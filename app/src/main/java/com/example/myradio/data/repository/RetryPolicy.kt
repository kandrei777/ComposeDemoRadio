package com.example.myradio.data.repository

import kotlinx.coroutines.flow.Flow

interface RetryPolicy {
    val attempts: Flow<Int>
    fun reportError()
    fun reportOk()
    fun cancel()
}