package com.example.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Search : Screen("search")
    object Messages : Screen("messages")
    object Chat : Screen("chat/{receiverId}") {
        fun createRoute(receiverId: String) = "chat/$receiverId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object PostJob : Screen("post_job")
    object ManageJobs : Screen("manage_jobs")
    object Followers : Screen("followers/{userId}") {
        fun createRoute(userId: String) = "followers/$userId"
    }
    object Following : Screen("following/{userId}") {
        fun createRoute(userId: String) = "following/$userId"
    }
    object QualifiedCandidate : Screen("qualified_candidate")
    object QualifiedCandidatesPage : Screen("qualified_candidates_page/{jobClassId}/{jobClassTitle}") {
        fun createRoute(id: String, title: String) = "qualified_candidates_page/$id/$title"
    }
    object SendApprovalLetter : Screen("send_approval_letter/{jobClassId}") {
        fun createRoute(id: String) = "send_approval_letter/$id"
    }
}
