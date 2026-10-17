package com.mohitkumar.docscanner.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mohitkumar.docscanner.presentation.camera.CameraScreen
import com.mohitkumar.docscanner.presentation.scans.ScansScreen
import com.mohitkumar.docscanner.presentation.scans.ScansViewModel

object Routes {
    const val SCANS = "scans"
    const val CAMERA = "camera"
}

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.SCANS) {
        composable(Routes.SCANS) {
            val vm: ScansViewModel = hiltViewModel()
            ScansScreen(viewModel = vm, onScanClick = { nav.navigate(Routes.CAMERA) })
        }
        composable(Routes.CAMERA) {
            // Share the same ViewModel instance as the list screen so the saved scan shows up on return.
            val parentEntry = nav.getBackStackEntry(Routes.SCANS)
            val vm: ScansViewModel = hiltViewModel(parentEntry)
            CameraScreen(
                onTextRecognized = { text ->
                    vm.onTextRecognized(text)
                    nav.popBackStack()
                },
                onClose = { nav.popBackStack() },
            )
        }
    }
}
