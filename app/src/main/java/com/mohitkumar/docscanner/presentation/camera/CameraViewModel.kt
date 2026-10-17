package com.mohitkumar.docscanner.presentation.camera

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.mohitkumar.docscanner.ml.TextRecognizerService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val recognizer: TextRecognizerService,
) : ViewModel() {
    suspend fun recognize(image: ImageProxy): String = recognizer.recognize(image)
}
