package energister.springdatasample

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Flag that might be set only once
 */
class OneOffFlag {
    private val latch = CountDownLatch(1)

    fun set() {
        latch.countDown()
    }

    /**
     * Causes the current thread to wait until the flag is [set]
     * or unless the thread is {@linkplain Thread#interrupt interrupted}.
     *
     * If the flag has already been set at the moment of invocation
     * method returns immediately.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @Throws(InterruptedException::class)
    fun await() = latch.await()

    /**
     * Causes the current thread to wait until the flag is [set]
     * or unless the thread is interrupted, or the specified waiting time elapses.
     *
     * If the flag has already been set at the moment of invocation
     * or the time is less than or equal to zero,
     * method returns immediately.
     *
     * If the specified waiting time elapses then the value `false` is returned.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @Throws(InterruptedException::class)
    fun await(timeout: Long, unit: TimeUnit): Boolean = latch.await(timeout, unit)
}