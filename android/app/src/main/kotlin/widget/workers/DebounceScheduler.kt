package dev.frammenti.fuckumeter.widget.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

object DebounceScheduler {
    inline fun <reified T : ListenableWorker> schedule(
        context: Context,
        uniqueName: String,
        delayMs: Long = 0L,
        inputData: Data = Data.EMPTY,
    ) {
        val request =
            OneTimeWorkRequestBuilder<T>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10.seconds.inWholeMilliseconds,
                    TimeUnit.MILLISECONDS,
                )
                .setInputData(inputData)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                uniqueName,
                ExistingWorkPolicy.REPLACE,
                request,
            )
    }
}
