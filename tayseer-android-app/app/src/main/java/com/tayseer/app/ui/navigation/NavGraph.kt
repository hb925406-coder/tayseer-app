package com.tayseer.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tayseer.app.ui.screens.*
import com.tayseer.app.ui.viewmodel.AuthViewModel
import com.tayseer.app.ui.viewmodel.TayseerViewModel

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val ATTENDANCE = "attendance"
    const val EVALUATION = "evaluation"
    const val STUDENTS = "students"
    const val STUDENT_DETAIL = "student_detail/{studentId}"
    const val PARENT_PORTAL = "parent_portal/{studentId}"
    const val SETTINGS = "settings"

    fun studentDetail(studentId: Long) = "student_detail/$studentId"
    fun parentPortal(studentId: Long) = "parent_portal/$studentId"
}

@Composable
fun TayseerNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    tayseerViewModel: TayseerViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onParentLoginSuccess = { studentId ->
                    navController.navigate(Routes.parentPortal(studentId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = tayseerViewModel,
                onNavigateToAttendance = { navController.navigate(Routes.ATTENDANCE) },
                onNavigateToEvaluation = { navController.navigate(Routes.EVALUATION) },
                onNavigateToStudents = { navController.navigate(Routes.STUDENTS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ATTENDANCE) {
            AttendanceScreen(
                viewModel = tayseerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EVALUATION) {
            EvaluationScreen(
                viewModel = tayseerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STUDENTS) {
            StudentsScreen(
                viewModel = tayseerViewModel,
                onStudentClick = { studentId ->
                    navController.navigate(Routes.studentDetail(studentId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.STUDENT_DETAIL,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            StudentDetailScreen(
                studentId = studentId,
                viewModel = tayseerViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PARENT_PORTAL,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            ParentPortalScreen(
                studentId = studentId,
                authViewModel = authViewModel,
                tayseerViewModel = tayseerViewModel,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                tayseerViewModel = tayseerViewModel,
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
