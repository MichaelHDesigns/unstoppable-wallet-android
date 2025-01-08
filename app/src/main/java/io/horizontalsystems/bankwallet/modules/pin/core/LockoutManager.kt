package io.horizontalsystems.bankwallet.modules.pin.core

import io.horizontalsystems.core.ILockoutStorage

class LockoutManager(
    private val localStorage: ILockoutStorage,
    private val uptimeProvider: UptimeProvider,
    private val lockoutUntilDateFactory: ILockoutUntilDateFactory
) : ILockoutManager {

    private val lockoutThreshold = 5

    override val currentState: LockoutState
        get() {
            val failedAttempts = localStorage.failedAttempts ?: 0 // Default to 0 if null
            val attemptsLeft = if (failedAttempts >= lockoutThreshold) {
                val lockoutUptime = localStorage.lockoutUptime ?: uptimeProvider.uptime
                lockoutUntilDateFactory.lockoutUntilDate(
                    failedAttempts,
                    lockoutUptime,
                    uptimeProvider.uptime
                )?.let { untilDate ->
                    return LockoutState.Locked(untilDate)
                }
                0 // No attempts left if locked
            } else {
                lockoutThreshold - failedAttempts // Calculate attempts left
            }

            return LockoutState.Unlocked(attemptsLeft)
        }

    override fun didFailUnlock() {
        val attempts = (localStorage.failedAttempts ?: 0) + 1
        if (attempts >= lockoutThreshold) {
            localStorage.lockoutUptime = uptimeProvider.uptime
        }
        localStorage.failedAttempts = attempts
    }

    override fun dropFailedAttempts() {
        localStorage.failedAttempts = null
    }
}