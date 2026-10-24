package com.tayseer.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.tayseer.app.ui.navigation.TayseerNavGraph
import com.tayseer.app.ui.theme.TayseerTheme
import com.tayseer.app.ui.viewmodel.AuthViewModel
import com.tayseer.app.ui.viewmodel.AuthViewModelFactory
import com.tayseer.app.ui.viewmodel.TayseerViewModel
import com.tayseer.app.ui.viewmodel.TayseerViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TayseerApplication
        val repository = app.repository

        val authViewModel: AuthViewModel by viewModels {
            AuthViewModelFactory(repository)
        }
        val tayseerViewModel: TayseerViewModel by viewModels {
            TayseerViewModelFactory(repository)
        }

        setContent {
            TayseerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val navController = rememberNavController()
                    val toastMsg by tayseerViewModel.toastMessage.collectAsState()

                    LaunchedEffect(toastMsg) {
                        toastMsg?.let { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            tayseerViewModel.clearToast()
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TayseerNavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            tayseerViewModel = tayseerViewModel
                        )
                    }
                }
            }
        }
    }
}
