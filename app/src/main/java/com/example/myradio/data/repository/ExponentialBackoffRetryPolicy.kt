package com.example.myradio.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ExponentialBackoffRetryPolicy @Inject constructor(
    private val scope: CoroutineScope
) : RetryPolicy {

    private val _attempts = MutableStateFlow(0)
    override val attempts: Flow<Int> = _attempts.asStateFlow()

    private var lastSuccess = 0
    private var retryJob: Job? = null

    override fun reportError() {
        // Если таймер ретрая уже запущен и тикает, игнорируем повторные вызовы,
        // чтобы не плодить параллельные корутины-таймеры на одну ошибку
        if (retryJob?.isActive == true) return

        retryJob = scope.launch {
            val currentFailureCount = _attempts.value - lastSuccess + 1
            if (currentFailureCount > MAX_RETRY_LIMIT) return@launch
            // Попытка 1: (2^1) * 1000 = 2 секунды
            // Попытка 2: (2^2) * 1000 = 4 секунды
            // ....
            // Попытка 7: (2^7) * 1000 = 128 секунд
            val delayTime = (2 shl minOf(currentFailureCount, EXPONENTIAL_CAP_STEPS)) * 1000L

            Timber.w("RetryPolicy: Fail, retry in ${delayTime / 1000} sec...")
            delay(delayTime)
            // Inc and push
            _attempts.value += 1
        }
    }

    override fun reportOk() {
        // Если в момент успешного старта еще тикал какой-то старый таймер — отменяем его
        retryJob?.cancel()

        if (lastSuccess != _attempts.value) {
            lastSuccess = _attempts.value
            Timber.d("RetryPolicy: Succes on $lastSuccess attempt.")
        }
    }

    override fun cancel() {
        // Если тикал какой-то старый таймер — отменяем его
        retryJob?.cancel()

        if (lastSuccess != _attempts.value) {
            lastSuccess = _attempts.value
            Timber.d("RetryPolicy: Canceled on $lastSuccess attempt.")
        }
    }

    companion object {
        const val EXPONENTIAL_CAP_STEPS = 7
        const val MAX_RETRY_LIMIT = 31
    }
}
