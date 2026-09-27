package com.divya.fitnesscoach.domain

/**
 * Fitness Coach's product-level view of whether on-device coaching can run right now.
 *
 * This is deliberately not a copy of the SDK's status values. The runtime tells us what
 * is happening underneath; this state tells the app what that means for the person
 * using it. `Unavailable` in particular means "not right now", not "never": AICore can
 * report it on a supported device that hasn't fetched its latest configuration yet.
 */
sealed interface AiReadinessState {

    data object Checking : AiReadinessState

    data object DownloadRequired : AiReadinessState

    /**
     * [bytesToDownload] is null when the total isn't known, for example when a download
     * was already in progress before this app started observing it.
     */
    data class Downloading(
        val bytesDownloaded: Long,
        val bytesToDownload: Long? = null
    ) : AiReadinessState

    data object WarmingUp : AiReadinessState

    data object Ready : AiReadinessState

    data object Unavailable : AiReadinessState

    data class Failed(val reason: ReadinessFailure) : AiReadinessState
}
