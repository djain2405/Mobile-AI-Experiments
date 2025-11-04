package com.example.smartlabelmagic

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.BufferedReader
import java.io.InputStreamReader

class Classifier(private val context: Context) {
    private val classifier = ImageClassifier.createFromFileAndOptions(
        context, "mobilenet_v1_1.0_224_quant.tflite",
        ImageClassifier.ImageClassifierOptions.builder().setMaxResults(1).build()
    )

    private val labels: List<String> by lazy {
        context.assets.open("labels.txt").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readLines()
        }
    }

    data class Result(val label: String, val confidence: Float)

    fun classify(bitmap: Bitmap): Result {
        val tensor = TensorImage.fromBitmap(bitmap)
        val res = classifier.classify(tensor)
        val top = res.firstOrNull()?.categories?.firstOrNull()

        // Get the label - if it's a number (class index), map it to the actual label
        val rawLabel = top?.label ?: "Unknown"
        val actualLabel = try {
            val index = rawLabel.toIntOrNull()
            if (index != null && index in labels.indices) {
                labels[index]
            } else {
                rawLabel
            }
        } catch (e: Exception) {
            rawLabel
        }

        return Result(actualLabel, top?.score ?: 0f)
    }
}
