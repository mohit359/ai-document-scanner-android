package com.mohitkumar.docscanner.ml

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** On-device OCR (ML Kit). Works offline and keeps the image on the phone. */
@Singleton
class TextRecognizerService @Inject constructor() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    suspend fun recognize(imageProxy: ImageProxy): String = try {
        val mediaImage = imageProxy.image ?: return ""
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(input).await().text
    } finally {
        imageProxy.close()
    }
}
