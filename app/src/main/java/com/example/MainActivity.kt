package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedPost
import com.example.model.UserProfile
import com.example.ui.components.DecisaoHeaderLogo
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerificationBadge
import com.example.ui.screens.*
import com.example.ui.theme.BrandPurpleGradientEnd
import com.example.ui.theme.BrandPurpleGradientStart
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DecisaoViewModel
import com.example.viewmodel.UiEvent

import com.example.viewmodel.MainTab
import com.example.viewmodel.ScreenType
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DecisaoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val composeScope = rememberCoroutineScope()
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                

                    val snackbarHostState = remember { SnackbarHostState() }
                    val context = androidx.compose.ui.platform.LocalContext.current

                    LaunchedEffect(Unit) {
                        viewModel.uiEvents.collect { event ->
                            when (event) {
                                is UiEvent.ShowSnackbar -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                                is UiEvent.Error -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                                is UiEvent.ShowToast -> {
                                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            when (currentScreen) {

                        ScreenType.WELCOME -> {
                            WelcomeScreen(
                                onRegisterComplete = { username, name, password ->
                                    composeScope.launch {
                                        val email = if (username.contains("@") && username.contains(".")) {
                                            username
                                        } else {
                                            "${username.replace("@", "").trim()}@decisoes.com"
                                        }
                                        val result = viewModel.authRepository.register(email, password, name, username)
                                        if (result.isSuccess) {
                                            viewModel.refreshCachedTokenSuspending()
                                            viewModel.updateProfile(name, username, "Novo no Decisões! 💜")
                                            viewModel.selectScreen(ScreenType.MAIN)
                                        } else {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Erro no Firebase: ${result.exceptionOrNull()?.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            val useDevAuth = try {
                                                com.example.BuildConfig.USE_DEV_AUTH.toBoolean()
                                            } catch (e: Exception) {
                                                true
                                            }
                                            if (useDevAuth) {
                                                // Fallback to offline/development flow even on error to make it robust for development
                                                viewModel.updateProfile(name, username, "Novo no Decisões! 💜")
                                                viewModel.selectScreen(ScreenType.MAIN)
                                            }
                                        }
                                    }
                                },
                                onNavigateToMain = { loginUsername, loginPassword ->
                                    composeScope.launch {
                                        val email = if (loginUsername.contains("@") && loginUsername.contains(".")) {
                                            loginUsername
                                        } else {
                                            "${loginUsername.replace("@", "").trim()}@decisoes.com"
                                        }
                                        val result = viewModel.authRepository.login(email, loginPassword)
                                        if (result.isSuccess) {
                                            viewModel.refreshCachedTokenSuspending()
                                            viewModel.loadMeProfile()
                                            viewModel.selectScreen(ScreenType.MAIN)
                                        } else {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Erro no Firebase: ${result.exceptionOrNull()?.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            val useDevAuth = try {
                                                com.example.BuildConfig.USE_DEV_AUTH.toBoolean()
                                            } catch (e: Exception) {
                                                true
                                            }
                                            if (useDevAuth) {
                                                // Fallback to offline/development flow even on error to make it robust for development
                                                viewModel.selectScreen(ScreenType.MAIN)
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        ScreenType.MAIN -> {
                            MainShellScreen(viewModel = viewModel)
                        }
                        ScreenType.CHAT_DETAIL -> {
                            val selectedRoom by viewModel.selectedChatRoom.collectAsState()
                            selectedRoom?.let { room ->
                                ChatDetailScreen(
                                    room = room,
                                    onSendMessage = { text -> viewModel.sendChatMessage(text) },
                                    onSendImage = { imageUrl, isSingleView -> 
                                        viewModel.sendChatImage(room.id, imageUrl, isSingleView)
                                    },
                                    onDeleteMessage = { messageId, forEveryone ->
                                        viewModel.deleteMessage(room.id, messageId, forEveryone)
                                    },
                                    onOpenSingleView = { messageId ->
                                        viewModel.openSingleViewImage(room.id, messageId)
                                    },
                                    onToggleMute = {
                                        viewModel.toggleMuteChatRoom(room.id)
                                    },
                                    onToggleBlock = {
                                        viewModel.toggleBlockChatRoom(room.id)
                                    },
                                    onBack = { viewModel.closeChatRoom() }
                                )
                            }
                        }
                        ScreenType.POST_DETAIL -> {
                            val selectedPost by viewModel.selectedPost.collectAsState()
                            val currentPostComments by viewModel.currentPostComments.collectAsState()
                            selectedPost?.let { post ->
                                PostDetailScreen(
                                    post = post,
                                    comments = currentPostComments,
                                    onLikePost = { viewModel.likePost(post.id) },
                                    onVotePoll = { optId -> viewModel.votePoll(post.id, optId) },
                                    onBackClick = { viewModel.selectScreen(ScreenType.MAIN) },
                                    onUserClick = { username -> viewModel.openUserProfileByUsername(username) },
                                    onAddComment = { text -> viewModel.addComment(post.id, text) },
                                    onLikeComment = { commentId -> viewModel.toggleLikeComment(post.id, commentId) },
                                    onSavePost = { viewModel.toggleSavePost(post.id) },
                                    onReportPost = { postId, reason, details -> viewModel.reportPost(postId, reason, details) },
                                    onReportComment = { commentId, postId, reason, details -> viewModel.reportComment(commentId, postId, reason, details) },
                                    onDeletePost = { id -> viewModel.deletePost(id) }
                                )
                            }
                        }
                        ScreenType.USER_PROFILE -> {
                            val selectedUserProfile by viewModel.selectedUserProfile.collectAsState()
                            val allPosts by viewModel.feedPosts.collectAsState()
                            selectedUserProfile?.let { profile ->
                                UserProfileScreen(
                                    profile = profile,
                                    allPosts = allPosts,
                                    onLikePost = { id -> viewModel.likePost(id) },
                                    onVotePoll = { id, optId -> viewModel.votePoll(id, optId) },
                                    onBackClick = { viewModel.selectScreen(ScreenType.MAIN) },
                                    onToggleFollow = { viewModel.toggleFollowUser(profile.username) },
                                    onSendMessage = { viewModel.startChatWithUser(profile.username) },
                                    onPostClick = { post -> viewModel.openPostDetail(post) },
                                    onSavePost = { id -> viewModel.toggleSavePost(id) },
                                    onReportPost = { id, reason, details -> viewModel.reportPost(id, reason, details) }
                                )
                            }
                        }
                        ScreenType.EDIT_PROFILE -> {
                            val userProfile by viewModel.userProfile.collectAsState()
                            EditProfileScreen(
                                profile = userProfile,
                                onSave = { name, username, bio, avatar, cover ->
                                    viewModel.updateProfile(name, username, bio, avatar, cover)
                                    viewModel.selectScreen(ScreenType.MAIN)
                                },
                                onBack = { viewModel.selectScreen(ScreenType.MAIN) }
                            )
                        }
                        ScreenType.CREATE_SELECTION -> {
                            CreateSelectionScreen(
                                onChoosePoll = { viewModel.selectScreen(ScreenType.CREATE_POLL) },
                                onChooseThought = { viewModel.selectScreen(ScreenType.CREATE_THOUGHT) },
                                onBack = { viewModel.selectScreen(ScreenType.MAIN) }
                            )
                        }
                        ScreenType.CREATE_POLL -> {
                            CreatePollScreen(
                                onCreatePoll = { question, options, category ->
                                    viewModel.createCustomPost(question, true, options, category, null)
                                    viewModel.selectScreen(ScreenType.MAIN)
                                    viewModel.selectTab(MainTab.INICIO)
                                },
                                onBack = { viewModel.selectScreen(ScreenType.CREATE_SELECTION) }
                            )
                        }
                        ScreenType.CREATE_THOUGHT -> {
                            CreateThoughtScreen(
                                onCreateThought = { text, category, imageUrl ->
                                    viewModel.createCustomPost(text, false, emptyList(), category, imageUrl)
                                    viewModel.selectScreen(ScreenType.MAIN)
                                    viewModel.selectTab(MainTab.INICIO)
                                },
                                onBack = { viewModel.selectScreen(ScreenType.CREATE_SELECTION) }
                            )
                        }
                        ScreenType.NOTIFICATIONS -> {
                            val notifications by viewModel.notifications.collectAsState()
                            LaunchedEffect(Unit) {
                                viewModel.markNotificationsAsRead()
                            }
                            NotificationScreen(
                                notifications = notifications,
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) },
                                onToggleFollow = { id -> viewModel.toggleFollowNotification(id) },
                                onExploreNearby = {
                                    viewModel.selectTab(MainTab.EXPLORAR)
                                    viewModel.selectScreen(ScreenType.MAIN)
                                }
                            )
                        }
                        ScreenType.SETTINGS -> {
                            val seeOnline by viewModel.seeOnline.collectAsState()
                            val lastSeen by viewModel.lastSeen.collectAsState()
                            val delivered by viewModel.delivered.collectAsState()
                            val readReceipt by viewModel.readReceipt.collectAsState()
                            val typing by viewModel.typing.collectAsState()
                            val recordingAudio by viewModel.recordingAudio.collectAsState()
                            val blockedContacts by viewModel.blockedContacts.collectAsState()

                            SettingsScreen(
                                seeOnline = seeOnline,
                                lastSeen = lastSeen,
                                delivered = delivered,
                                readReceipt = readReceipt,
                                typing = typing,
                                recordingAudio = recordingAudio,
                                blockedContacts = blockedContacts,
                                onTogglePrivacy = { viewModel.togglePrivacy(it) },
                                onUnblockContact = { viewModel.unblockContact(it) },
                                onDeleteAccount = { viewModel.deleteAccountPermanently() },
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) }
                            )
                        }
                        ScreenType.SAVED_POSTS -> {
                            val savedPosts by viewModel.savedPosts.collectAsState()
                            SavedPostsScreen(
                                savedPosts = savedPosts,
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) },
                                onPostClick = { post -> viewModel.openPostDetail(post) },
                                onRemoveSave = { postId -> viewModel.toggleSavePost(postId) }
                            )
                        }
                        ScreenType.FOLLOWERS_LIST -> {
                            val followingUsers by viewModel.followingUsers.collectAsState()
                            FollowingListScreen(
                                followingUsers = followingUsers,
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) },
                                onUserClick = { user -> viewModel.openUserProfileByUsername(user.username) },
                                onUnfollowUser = { username -> viewModel.toggleFollowUser(username) }
                            )
                        }
                                                ScreenType.HELP_SUPPORT -> {
                            HelpSupportScreen(
                                onBackClick = { viewModel.selectScreen(ScreenType.MAIN) }
                            )
                        }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun MainShellScreen(viewModel: DecisaoViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    val isDark by viewModel.isDarkTheme.collectAsState()
    val currentTab by viewModel.currentMainTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val stories by viewModel.stories.collectAsState()
    val feedPosts by viewModel.feedPosts.collectAsState()
    val chatRooms by viewModel.chatRooms.collectAsState()
    val profilePosts by viewModel.profilePosts.collectAsState()
    val allUserProfiles by viewModel.allUserProfiles.collectAsState()

    val unreadMessagesCount = chatRooms.sumOf { it.unreadCount }
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationsCount = notifications.count { it.isUnread }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                DrawerMenuContent(
                    profileName = userProfile.name,
                    profileUsername = userProfile.username,
                    isDarkTheme = isDark,
                    unreadMessagesCount = unreadMessagesCount,
                    unreadNotificationsCount = unreadNotificationsCount,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onNavigateToTab = { tab ->
                        viewModel.selectTab(tab)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNotificationsClick = {
                        viewModel.selectScreen(ScreenType.NOTIFICATIONS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSavedClick = {
                        viewModel.selectScreen(ScreenType.SAVED_POSTS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onFollowingClick = {
                        viewModel.selectScreen(ScreenType.FOLLOWERS_LIST)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onSettingsClick = {
                        viewModel.selectScreen(ScreenType.SETTINGS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onHelpClick = {
                        viewModel.selectScreen(ScreenType.HELP_SUPPORT)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onLogout = {
                        coroutineScope.launch {
                            viewModel.authRepository.logout()
                        }
                        viewModel.selectScreen(ScreenType.WELCOME)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Top App Bar is shown on tabs, except profile which has a custom banner header
                if (currentTab != MainTab.PERFIL) {
                    val titleText = when (currentTab) {
                        MainTab.INICIO -> stringResource(R.string.app_name)
                        MainTab.EXPLORAR -> "Explorar"
                        MainTab.MENSAGENS -> "Mensagens"
                        else -> stringResource(R.string.app_name)
                    }
                    val showBackButton = currentTab != MainTab.INICIO
                    
                    DecisaoAppBar(
                        titleText = titleText,
                        showBackButton = showBackButton,
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onBackClick = {
                            viewModel.selectTab(MainTab.INICIO)
                        },
                        onNotificationsClick = {
                            viewModel.selectScreen(ScreenType.NOTIFICATIONS)
                        },
                        unreadNotificationsCount = unreadNotificationsCount
                    )
                }
            },
            bottomBar = {
                DecisaoBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == MainTab.CRIAR) {
                            viewModel.selectScreen(ScreenType.CREATE_SELECTION)
                        } else {
                            viewModel.selectTab(tab)
                        }
                    },
                    unreadMessagesCount = unreadMessagesCount
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Render the selected Tab
                when (currentTab) {
                    MainTab.INICIO -> {
                        FeedScreen(
                            stories = stories,
                            posts = feedPosts,
                            onLikePost = { id -> viewModel.likePost(id) },
                            onVotePoll = { id, optId -> viewModel.votePoll(id, optId) },
                            onCreatePoll = { q, o1, o2 -> viewModel.createPoll(q, o1, o2) },
                            onAvatarClick = { viewModel.selectTab(MainTab.PERFIL) },
                            onPostClick = { post -> viewModel.openPostDetail(post) },
                            onUserClick = { username -> viewModel.openUserProfileByUsername(username) },
                            onToggleFollow = { username -> viewModel.toggleFollowUser(username) },
                            onSavePost = { id -> viewModel.toggleSavePost(id) },
                            onReportPost = { id, reason, details -> viewModel.reportPost(id, reason, details) },
                            onDeletePost = { id -> viewModel.deletePost(id) },
                            currentUserUsername = userProfile.username
                        )
                    }
                    MainTab.EXPLORAR -> {
                        ExploreScreen(
                            allUserProfiles = allUserProfiles,
                            onUserClick = { username -> viewModel.openUserProfileByUsername(username) }
                        )
                    }
                    MainTab.MENSAGENS -> {
                        MessagesScreen(
                            chatRooms = chatRooms,
                            onChatRoomClick = { id -> viewModel.openChatRoom(id) },
                            onBackToHome = { viewModel.selectTab(MainTab.INICIO) },
                            onDeleteChatRoom = { id -> viewModel.deleteChatRoom(id) }
                        )
                    }
                    MainTab.PERFIL -> {
                        ProfileScreen(
                            profile = userProfile,
                            profilePosts = profilePosts,
                            onLikePost = { id -> viewModel.likePost(id) },
                            onVotePoll = { id, optId -> viewModel.votePoll(id, optId) },
                            onEditProfileClick = { viewModel.selectScreen(ScreenType.EDIT_PROFILE) },
                            onBackToHome = { viewModel.selectTab(MainTab.INICIO) },
                            onSavePost = { id -> viewModel.toggleSavePost(id) },
                            onReportPost = { id, reason, details -> viewModel.reportPost(id, reason, details) },
                            onDeletePost = { id -> viewModel.deletePost(id) }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisaoAppBar(
    titleText: String,
    showBackButton: Boolean,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNotificationsCount: Int,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("app_bar"),
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                IconButton(onClick = onMenuClick, modifier = Modifier.testTag("menu_hamburger_button")) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu Lateral",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Box {
                    Icon(
                        imageVector = if (unreadNotificationsCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Notificações",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .sizeIn(minWidth = 14.dp, minHeight = 14.dp)
                                .background(Color(0xFFFF1744), CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadNotificationsCount.toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun DecisaoBottomNavigation(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    unreadMessagesCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Início
        NavigationBarItem(
            selected = currentTab == MainTab.INICIO,
            onClick = { onTabSelected(MainTab.INICIO) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.INICIO) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.nav_inicio)
                )
            },
            label = { Text(stringResource(R.string.nav_inicio), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )

        // Tab 2: Explorar
        NavigationBarItem(
            selected = currentTab == MainTab.EXPLORAR,
            onClick = { onTabSelected(MainTab.EXPLORAR) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.EXPLORAR) Icons.Filled.Explore else Icons.Outlined.Explore,
                    contentDescription = stringResource(R.string.nav_explorar)
                )
            },
            label = { Text(stringResource(R.string.nav_explorar), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )

        // Tab 3: Criar (+)
        NavigationBarItem(
            selected = false,
            onClick = { onTabSelected(MainTab.CRIAR) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(BrandPurpleGradientStart, BrandPurpleGradientEnd)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.nav_criar),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_criar), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        )

        // Tab 4: Mensagens
        NavigationBarItem(
            selected = currentTab == MainTab.MENSAGENS,
            onClick = { onTabSelected(MainTab.MENSAGENS) },
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadMessagesCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFF1744),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = unreadMessagesCount.toString(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == MainTab.MENSAGENS) Icons.Filled.Forum else Icons.Outlined.Forum,
                        contentDescription = stringResource(R.string.nav_mensagens)
                    )
                }
            },
            label = { Text(stringResource(R.string.nav_mensagens), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )

        // Tab 5: Perfil
        NavigationBarItem(
            selected = currentTab == MainTab.PERFIL,
            onClick = { onTabSelected(MainTab.PERFIL) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.PERFIL) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                    contentDescription = stringResource(R.string.nav_perfil)
                )
            },
            label = { Text(stringResource(R.string.nav_perfil), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
    }
}

@Composable
fun DrawerMenuContent(
    profileName: String,
    profileUsername: String,
    isDarkTheme: Boolean,
    unreadMessagesCount: Int,
    unreadNotificationsCount: Int,
    onToggleTheme: () -> Unit,
    onNavigateToTab: (MainTab) -> Unit,
    onNotificationsClick: () -> Unit,
    onSavedClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // 1. Branding Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                DecisaoHeaderLogo(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
            }

            // 2. User info card matching Screen 3 Drawer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                UserAvatar(name = profileName, size = 52.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profileName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        VerificationBadge(size = 14.dp)
                    }
                    Text(
                        text = profileUsername,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ver perfil",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onNavigateToTab(MainTab.PERFIL) }
                            .testTag("drawer_view_profile")
                    )
                }
            }

            // 3. Navigation Drawer Menu Links matching Screen 3 precisely
            DrawerMenuItem(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.nav_inicio),
                onClick = { onNavigateToTab(MainTab.INICIO) },
                tag = "drawer_item_inicio"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Explore,
                label = stringResource(R.string.nav_explorar),
                onClick = { onNavigateToTab(MainTab.EXPLORAR) },
                tag = "drawer_item_explorar"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Notifications,
                label = stringResource(R.string.menu_notificacoes),
                badgeText = if (unreadNotificationsCount > 0) unreadNotificationsCount.toString() else null,
                onClick = onNotificationsClick,
                tag = "drawer_item_notif"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Forum,
                label = stringResource(R.string.nav_mensagens),
                badgeText = if (unreadMessagesCount > 0) unreadMessagesCount.toString() else null,
                onClick = { onNavigateToTab(MainTab.MENSAGENS) },
                tag = "drawer_item_mensagens"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Bookmark,
                label = stringResource(R.string.menu_salvos),
                onClick = onSavedClick,
                tag = "drawer_item_salvos"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Group,
                label = stringResource(R.string.menu_seguindo),
                onClick = onFollowingClick,
                tag = "drawer_item_seguindo"
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            DrawerMenuItem(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.menu_configuracoes),
                onClick = onSettingsClick,
                tag = "drawer_item_config"
            )
            DrawerMenuItem(
                icon = Icons.Filled.Help,
                label = stringResource(R.string.menu_ajuda),
                onClick = onHelpClick,
                tag = "drawer_item_ajuda"
            )
            DrawerMenuItem(
                icon = Icons.Filled.ExitToApp,
                label = stringResource(R.string.menu_sair),
                onClick = onLogout,
                tag = "drawer_item_sair"
            )
        }

        // 4. Drawer Footer Theme Switch matching Screen 3 perfectly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.menu_tema),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Clean custom toggler matching Sun/Moon icons in the switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable { onToggleTheme() }
                    .testTag("drawer_theme_toggle_layout")
            ) {
                Icon(
                    imageVector = Icons.Filled.Brightness2, // Moon icon
                    contentDescription = "Tema Escuro",
                    tint = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        uncheckedTrackColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .scale(0.7f)
                        .height(20.dp)
                        .testTag("drawer_theme_switch")
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Filled.WbSunny, // Sun icon
                    contentDescription = "Tema Claro",
                    tint = if (!isDarkTheme) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    badgeText: String? = null,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFF1744)) // Red badge matching Screen 3 "12"
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class ExploreTopic(val title: String, val votesText: String)

@Composable
fun ExploreScreen(
    allUserProfiles: Map<String, UserProfile>,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val topics = listOf(
        ExploreTopic("🗳️ Mobilidade Urbana", "9.4k votos hoje"),
        ExploreTopic("🌱 Meio Ambiente", "4.2k votos hoje"),
        ExploreTopic("🎓 Educação Pública", "12.8k votos hoje"),
        ExploreTopic("🐾 Proteção Animal", "1.5k votos hoje"),
        ExploreTopic("🏥 Saúde e Bem-Estar", "6.1k votos hoje"),
        ExploreTopic("💻 Inclusão Digital", "3.0k votos hoje")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search bar is ALWAYS at the top of the screen now
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar nome ou @usuario...", fontSize = 14.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("explore_search_field"),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        if (searchQuery.isEmpty()) {
            // Original Screen state (No Search Active) but with the search bar already at the top
            Text(
                text = "Participe dos debates e enquetes populares mais relevantes.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 1. Pessoas Próximas (Nearby People Section)
            Text(
                text = "👥 Pessoas Próximas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allUserProfiles.values.toList()) { user ->
                    val distance = when (user.username) {
                        "@anaclara" -> "1.2 km"
                        "@gabrielf" -> "800m"
                        "@julianacosta" -> "2.5 km"
                        "@lucasmartins" -> "3.1 km"
                        "@beatriz" -> "1.8 km"
                        else -> "2.0 km"
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onUserClick(user.username) }
                            .testTag("nearby_user_${user.username}")
                    ) {
                        UserAvatar(name = user.name, size = 62.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = user.name.split(" ").firstOrNull() ?: user.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(66.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = distance,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Temas em Alta (Trending Topics)
            Text(
                text = "🔥 Assuntos Mais Relevantes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topics) { topic ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = topic.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topic.votesText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
            // Search Mode active: original sections are hidden
            val filteredUsers = allUserProfiles.values.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.username.contains(searchQuery, ignoreCase = true)
            }

            Text(
                text = "Resultados para \"$searchQuery\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum usuário encontrado.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserClick(user.username) }
                                .testTag("search_result_${user.username}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = user.name, size = 44.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VerificationBadge(size = 12.dp)
                                        }
                                    }
                                    Text(
                                        text = user.username,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                }
            }
        }
    }
}
}
