package ubb.thesis.david.data.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.CountDownLatch

abstract class BaseWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    private lateinit var delayedResult: Result
    private lateinit var progressBarrier: CountDownLatch

    abstract fun executeTask()

    override fun doWork(): Result {
        progressBarrier = CountDownLatch(1)
        executeTask()
        progressBarrier.await()
        return delayedResult
    }

    protected fun onWorkDone(result: Result) {
        delayedResult = result
        progressBarrier.countDown()
    }

    protected fun displayProgress(message: String) =
        applicationContext.runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }

}