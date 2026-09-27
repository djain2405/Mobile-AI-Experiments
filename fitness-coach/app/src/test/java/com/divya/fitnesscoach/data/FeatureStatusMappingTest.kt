package com.divya.fitnesscoach.data

import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.ReadinessFailure
import com.google.mlkit.genai.common.FeatureStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureStatusMappingTest {

    @Test
    fun `AVAILABLE maps to Ready`() {
        assertEquals(AiReadinessState.Ready, readinessStateFor(FeatureStatus.AVAILABLE))
    }

    @Test
    fun `DOWNLOADABLE maps to DownloadRequired`() {
        assertEquals(AiReadinessState.DownloadRequired, readinessStateFor(FeatureStatus.DOWNLOADABLE))
    }

    @Test
    fun `DOWNLOADING maps to Downloading with unknown total`() {
        assertEquals(
            AiReadinessState.Downloading(bytesDownloaded = 0L, bytesToDownload = null),
            readinessStateFor(FeatureStatus.DOWNLOADING)
        )
    }

    @Test
    fun `UNAVAILABLE maps to Unavailable, not a permanent unsupported state`() {
        assertEquals(AiReadinessState.Unavailable, readinessStateFor(FeatureStatus.UNAVAILABLE))
    }

    @Test
    fun `an unknown status maps to Failed Unknown`() {
        assertEquals(
            AiReadinessState.Failed(ReadinessFailure.Unknown),
            readinessStateFor(42)
        )
    }
}
