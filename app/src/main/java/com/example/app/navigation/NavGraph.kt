package com.example.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.app.repository.*
import com.example.app.ui.screens.*
import com.example.app.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController, themeViewModel: ThemeViewModel) {
    val context = LocalContext.current
    
    // Repositories
    val authRepo = remember { AuthRepository() }
    val userRepo = remember { UserRepository() }
    val jobRepo = remember { JobRepository() }
    val chatRepo = remember { ChatRepository() }
    val jobClassRepo = remember { JobClassRepository() }
    val storageRepo = remember { 
        StorageRepository(context).apply {
            initCloudinary("dbvrr11iu", "171412535189688")
        }
    }

    // ViewModels with factories
    val authViewModel: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepo, userRepo) as T
        }
    })
    
    val homeViewModel: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(jobRepo, authRepo, userRepo) as T
        }
    })

    val searchViewModel: SearchViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(userRepo, authRepo) as T
        }
    })

    val profileViewModel: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userRepo, authRepo, storageRepo) as T
        }
    })

    val chatViewModel: ChatViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(chatRepo, authRepo, userRepo) as T
        }
    })

    val savedJobsViewModel: SavedJobsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SavedJobsViewModel(authRepo, userRepo, jobRepo) as T
        }
    })

    val notificationViewModel: NotificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(authRepo, userRepo, jobRepo, chatRepo) as T
        }
    })

    val qualifiedCandidateViewModel: QualifiedCandidateViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return QualifiedCandidateViewModel(jobClassRepo, userRepo, chatRepo, authRepo) as T
        }
    })

    val verificationViewModel: VerificationViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return VerificationViewModel(userRepo, authRepo) as T
        }
    })

    val currentFeedMode by homeViewModel.feedMode.collectAsState()
    var showModeDialog by remember { mutableStateOf(false) }

    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = { Text("Select Feed Mode") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Discover Mode",
                                color = if (currentFeedMode == HomeFeedMode.DISCOVER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        supportingContent = { Text("See all jobs from everyone") },
                        trailingContent = {
                            if (currentFeedMode == HomeFeedMode.DISCOVER) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            homeViewModel.setFeedMode(HomeFeedMode.DISCOVER)
                            showModeDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { 
                            Text(
                                "Pro Mode",
                                color = if (currentFeedMode == HomeFeedMode.PRO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        supportingContent = { Text("Only see jobs from accounts you follow") },
                        trailingContent = {
                            if (currentFeedMode == HomeFeedMode.PRO) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            homeViewModel.setFeedMode(HomeFeedMode.PRO)
                            showModeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    val startDestination = Screen.Splash.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Screen.Home.route, Screen.Search.route, Screen.Messages.route, Screen.Profile.route)) {
                BottomNavigationBar(
                    navController = navController,
                    onHomeLongClick = { showModeDialog = true }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onAnimationFinished = {
                    val nextRoute = if (authViewModel.isLoggedIn()) Screen.Home.route else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onPostJobClick = { navController.navigate(Screen.PostJob.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToSaved = { navController.navigate(Screen.SavedJobs.route) },
                    onAuthorClick = { userId ->
                        navController.navigate(Screen.OtherProfile.createRoute(userId))
                    }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = notificationViewModel,
                    homeViewModel = homeViewModel,
                    onBack = { navController.popBackStack() },
                    onNotificationClick = { userId, type ->
                        when (type) {
                            com.example.app.model.NotificationType.MESSAGE -> 
                                navController.navigate(Screen.Chat.createRoute(userId))
                            com.example.app.model.NotificationType.NEW_FOLLOWER -> 
                                navController.navigate(Screen.Chat.createRoute(userId))
                            else -> 
                                navController.navigate(Screen.Home.route)
                        }
                    },
                    onNavigateToSaved = { navController.navigate(Screen.SavedJobs.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onMessageClick = { receiverId ->
                        navController.navigate(Screen.Chat.createRoute(receiverId))
                    }
                )
            }
            composable(Screen.Messages.route) {
                MessagesScreen(
                    viewModel = chatViewModel,
                    onChatClick = { receiverId ->
                        navController.navigate(Screen.Chat.createRoute(receiverId))
                    }
                )
            }
            composable(Screen.Chat.route) { backStackEntry ->
                val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                ChatScreen(
                    viewModel = chatViewModel,
                    qualViewModel = qualifiedCandidateViewModel,
                    receiverId = receiverId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                    onFollowersClick = { userId ->
                        navController.navigate(Screen.Followers.createRoute(userId))
                    },
                    onFollowingClick = { userId ->
                        navController.navigate(Screen.Following.createRoute(userId))
                    },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.OtherProfile.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                ProfileScreen(
                    userId = userId,
                    profileViewModel = profileViewModel,
                    onEditProfileClick = {}, // Not used
                    onFollowersClick = { uid ->
                        navController.navigate(Screen.Followers.createRoute(uid))
                    },
                    onFollowingClick = { uid ->
                        navController.navigate(Screen.Following.createRoute(uid))
                    },
                    onSettingsClick = {} // Not used
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    themeViewModel = themeViewModel,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                    onPostJobClick = { navController.navigate(Screen.PostJob.route) },
                    onManageJobsClick = { navController.navigate(Screen.ManageJobs.route) },
                    onQualifiedCandidateClick = { navController.navigate(Screen.QualifiedCandidate.route) },
                    onSavedJobsClick = { navController.navigate(Screen.SavedJobs.route) },
                    onVerifyAccountsClick = { navController.navigate(Screen.Verification.route) },
                    onLogoutSuccess = {
                        profileViewModel.clear()
                        chatViewModel.clear()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable(Screen.Verification.route) {
                VerificationScreen(
                    viewModel = verificationViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SavedJobs.route) {
                SavedJobsScreen(
                    viewModel = savedJobsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.QualifiedCandidate.route) {
                QualifiedCandidateScreen(
                    viewModel = qualifiedCandidateViewModel,
                    onBack = { navController.popBackStack() },
                    onJobClassClick = { id, title ->
                        navController.navigate(Screen.QualifiedCandidatesPage.createRoute(id, title))
                    }
                )
            }
            composable(Screen.QualifiedCandidatesPage.route) { backStackEntry ->
                val jobClassId = backStackEntry.arguments?.getString("jobClassId") ?: ""
                val jobClassTitle = backStackEntry.arguments?.getString("jobClassTitle") ?: ""
                QualifiedCandidatesPage(
                    jobClassId = jobClassId,
                    jobClassTitle = jobClassTitle,
                    viewModel = qualifiedCandidateViewModel,
                    onBack = { navController.popBackStack() },
                    onSendApprovalClick = { id ->
                        navController.navigate(Screen.SendApprovalLetter.createRoute(id))
                    }
                )
            }
            composable(Screen.SendApprovalLetter.route) { backStackEntry ->
                val jobClassId = backStackEntry.arguments?.getString("jobClassId") ?: ""
                SendApprovalLetterScreen(
                    jobClassId = jobClassId,
                    viewModel = qualifiedCandidateViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.popBackStack(Screen.QualifiedCandidatesPage.route, inclusive = false)
                    }
                )
            }
            composable(Screen.ManageJobs.route) {
                ManageJobsScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Followers.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                LaunchedEffect(userId) {
                    profileViewModel.loadFollowers(userId)
                }
                UserListScreen(
                    title = "Followers",
                    viewModel = profileViewModel,
                    onUserClick = { partnerId ->
                        navController.navigate(Screen.Chat.createRoute(partnerId))
                    }
                )
            }
            composable(Screen.Following.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                LaunchedEffect(userId) {
                    profileViewModel.loadFollowing(userId)
                }
                UserListScreen(
                    title = "Following",
                    viewModel = profileViewModel,
                    onUserClick = { partnerId ->
                        navController.navigate(Screen.Chat.createRoute(partnerId))
                    }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PostJob.route) {
                PostJobScreen(
                    viewModel = homeViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavigationBar(navController: NavHostController, onHomeLongClick: () -> Unit) {
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Search", Screen.Search.route, Icons.Default.Search),
        BottomNavItem("Messages", Screen.Messages.route, Icons.Default.Message),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    if (item.route == Screen.Home.route) {
                        Box(modifier = Modifier.combinedClickable(
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onLongClick = onHomeLongClick
                        )) {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route != Screen.Home.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: ImageVector)
