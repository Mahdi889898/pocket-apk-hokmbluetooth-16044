package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.GameTableScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HostLobbyScreen
import com.example.ui.screens.JoinLobbyScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.theme.HokmTheme
import com.example.ui.theme.TableTheme
import com.example.viewmodel.HokmViewModel

const val ROUTE_HOME = "home"
const val ROUTE_HOST_LOBBY = "host_lobby"
const val ROUTE_JOIN_LOBBY = "join_lobby"
const val ROUTE_GAME_TABLE = "game_table"
const val ROUTE_RULES = "rules"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HokmTheme {
                val navController = rememberNavController()
                val viewModel: HokmViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.navigateToGameSignal) {
                    if (state.navigateToGameSignal > 0L &&
                        navController.currentDestination?.route != ROUTE_GAME_TABLE
                    ) {
                        navController.navigate(ROUTE_GAME_TABLE) {
                            launchSingleTop = true
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_HOME
                ) {
                    composable(ROUTE_HOME) {
                        HomeScreen(
                            state = state,
                            onHostBluetoothClick = {
                                viewModel.enterHostLobby()
                                navController.navigate(ROUTE_HOST_LOBBY)
                            },
                            onJoinBluetoothClick = {
                                viewModel.enterJoinLobby()
                                navController.navigate(ROUTE_JOIN_LOBBY)
                            },
                            onSoloPracticeClick = {
                                viewModel.startSoloMatch()
                            },
                            onRulesClick = {
                                navController.navigate(ROUTE_RULES)
                            },
                            onUpdateName = viewModel::updatePlayerName,
                            onSelectTableTheme = viewModel::updateTableTheme,
                            onToggleHaptics = viewModel::setHapticsEnabled
                        )
                    }

                    composable(ROUTE_HOST_LOBBY) {
                        HostLobbyScreen(
                            state = state,
                            requiredPermissions = viewModel.requiredBluetoothPermissions(),
                            onRefreshBluetooth = viewModel::refreshBluetoothStatus,
                            onRestartServer = viewModel::restartBluetoothHostServer,
                            onSeatClicked = viewModel::onLobbySeatClicked,
                            onSelectTargetHands = viewModel::updatePreferredTargetHands,
                            onStartMatch = viewModel::startHostedMatch,
                            onPlaySoloFallback = {
                                viewModel.startSoloMatch()
                            },
                            onDismissError = viewModel::clearError,
                            onBack = {
                                viewModel.leaveSession()
                                navController.popBackStack(ROUTE_HOME, inclusive = false)
                            }
                        )
                    }

                    composable(ROUTE_JOIN_LOBBY) {
                        JoinLobbyScreen(
                            state = state,
                            requiredPermissions = viewModel.requiredBluetoothPermissions(),
                            onRefreshBluetooth = viewModel::refreshBluetoothStatus,
                            onStartScan = viewModel::startBluetoothScan,
                            onStopScan = viewModel::stopBluetoothScan,
                            onConnectDevice = viewModel::connectToHost,
                            onPlaySoloFallback = {
                                viewModel.startSoloMatch()
                            },
                            onDismissError = viewModel::clearError,
                            onBack = {
                                viewModel.leaveSession()
                                navController.popBackStack(ROUTE_HOME, inclusive = false)
                            }
                        )
                    }

                    composable(ROUTE_GAME_TABLE) {
                        val snap = state.snapshot
                        val legalCards = if (snap != null) {
                            viewModel.legalCardsFromSnapshot(snap)
                        } else {
                            emptySet()
                        }
                        GameTableScreen(
                            state = state,
                            legalCards = legalCards,
                            onChooseTrump = viewModel::chooseTrump,
                            onCardTapped = viewModel::onCardTapped,
                            onPlaySelectedCard = viewModel::playSelectedCardNow,
                            onNextHand = viewModel::startNextHand,
                            onRematch = viewModel::rematch,
                            onSendQuickChat = viewModel::sendQuickChat,
                            onSelectTableTheme = viewModel::updateTableTheme,
                            onDismissNotice = viewModel::clearNotice,
                            onExitMatch = {
                                viewModel.leaveSession()
                                navController.popBackStack(ROUTE_HOME, inclusive = false)
                            }
                        )
                    }

                    composable(ROUTE_RULES) {
                        RulesScreen(
                            tableTheme = TableTheme.fromId(state.profile.tableThemeId),
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
