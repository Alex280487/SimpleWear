package com.thewizrd.simplewear.ui.simplewear

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.HierarchicalFocusCoordinator
import androidx.wear.compose.foundation.edgeSwipeToDismiss
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.PagerScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.layout.scrollAway
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.pager.HorizontalPagerDefaults
import com.thewizrd.shared_resources.helpers.WearConnectionStatus
import com.thewizrd.simplewear.R
import com.thewizrd.simplewear.controls.AppItemViewModel
import com.thewizrd.simplewear.ui.components.LoadingContent
import com.thewizrd.simplewear.ui.theme.WearAppTheme
import com.thewizrd.simplewear.ui.theme.activityViewModel
import com.thewizrd.simplewear.ui.theme.findActivity
import com.thewizrd.simplewear.viewmodels.AppLauncherUiState
import com.thewizrd.simplewear.viewmodels.AppLauncherViewModel

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalWearFoundationApi::class,
    ExperimentalHorologistApi::class
)
@Composable
fun AppLauncherScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val appLauncherViewModel = activityViewModel<AppLauncherViewModel>()
    val uiState by appLauncherViewModel.uiState.collectAsState()

    val scrollState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Unspecified,
            last = ScalingLazyColumnDefaults.ItemType.Chip,
        )
    )
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )

    WearAppTheme {
        PagerScaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = {
                if (pagerState.currentPage == 0) {
                    TimeText(modifier = Modifier.scrollAway { scrollState })
                }
            },
            pagerState = if (uiState.isLoading) null else pagerState
        ) {
            SwipeToDismissBox(
                modifier = Modifier.background(MaterialTheme.colors.background),
                onDismissed = {
                    activity.onBackPressed()
                },
                state = swipeToDismissBoxState
            ) { isBackground ->
                if (isBackground) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                    )
                } else {
                    HorizontalPager(
                        modifier = modifier.edgeSwipeToDismiss(swipeToDismissBoxState),
                        state = pagerState,
                        flingBehavior = HorizontalPagerDefaults.flingParams(pagerState)
                    ) { pageIdx ->
                        HierarchicalFocusCoordinator(requiresFocus = { pageIdx == pagerState.currentPage }) {
                            if (pageIdx == 0) {
                                AppLauncherScreen(
                                    appLauncherViewModel = appLauncherViewModel,
                                    scrollState = scrollState
                                )
                            } else {
                                AppLauncherSettings(
                                    appLauncherViewModel = appLauncherViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun AppLauncherScreen(
    appLauncherViewModel: AppLauncherViewModel,
    scrollState: ScalingLazyColumnState
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    val uiState by appLauncherViewModel.uiState.collectAsState()

    AppLauncherScreen(
        uiState = uiState,
        scrollState = scrollState,
        onItemClicked = {
            appLauncherViewModel.openRemoteApp(activity, it)
        },
        onRefresh = {
            appLauncherViewModel.refreshApps()
        }
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun AppLauncherScreen(
    uiState: AppLauncherUiState,
    scrollState: ScalingLazyColumnState = rememberResponsiveColumnState(),
    onItemClicked: (AppItemViewModel) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingContent(
            empty = uiState.appsList.isEmpty(),
            emptyContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            text = stringResource(id = R.string.error_noapps),
                            textAlign = TextAlign.Center
                        )
                        CompactChip(
                            label = {
                                Text(text = stringResource(id = R.string.action_refresh))
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_refresh_24),
                                    contentDescription = null
                                )
                            },
                            onClick = onRefresh
                        )
                    }
                }
            },
            loading = uiState.isLoading
        ) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                columnState = scrollState,
            ) {
                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Text(text = stringResource(id = R.string.action_apps))
                    }
                }

                items(
                    items = uiState.appsList,
                    key = { Pair(it.activityName, it.packageName) }
                ) {
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(text = it.appLabel ?: "")
                        },
                        icon = if (uiState.loadAppIcons) {
                            it.bitmapIcon?.let {
                                {
                                    Icon(
                                        modifier = Modifier.requiredSize(ChipDefaults.IconSize),
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        } else {
                            null
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        onClick = {
                            onItemClicked(it)
                        }
                    )
                }
            }

            PositionIndicator(scalingLazyListState = scrollState.state)
        }
    }
}

@Composable
private fun AppLauncherSettings(
    appLauncherViewModel: AppLauncherViewModel
) {
    val uiState by appLauncherViewModel.uiState.collectAsState()

    AppLauncherSettings(
        uiState = uiState,
        onCheckChanged = {
            appLauncherViewModel.showAppIcons(it)
        }
    )
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
private fun AppLauncherSettings(
    uiState: AppLauncherUiState,
    onCheckChanged: (Boolean) -> Unit = {}
) {
    val scrollState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Unspecified,
            last = ScalingLazyColumnDefaults.ItemType.Chip,
        )
    )

    ScalingLazyColumn(
        columnState = scrollState
    ) {
        item {
            ResponsiveListHeader(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = firstItemPadding()
            ) {
                Text(text = stringResource(id = R.string.title_settings))
            }
        }
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(id = R.string.pref_loadicons_title))
                },
                checked = uiState.loadAppIcons,
                onCheckedChange = onCheckChanged,
                toggleControl = {
                    Switch(checked = uiState.loadAppIcons)
                }
            )
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@WearPreviewDevices
@Composable
private fun PreviewAppLauncherScreen() {
    val context = LocalContext.current

    val uiState = remember(context) {
        AppLauncherUiState(
            connectionStatus = WearConnectionStatus.CONNECTED,
            appsList = List(10) { index ->
                AppItemViewModel().apply {
                    appLabel = "App ${index + 1}"
                    packageName = "com.package.${index}"
                    bitmapIcon = ContextCompat.getDrawable(context, R.drawable.ic_icon)!!.toBitmap()
                }
            },
            isLoading = false,
            loadAppIcons = true
        )
    }

    AppLauncherScreen(uiState = uiState)
}

@OptIn(ExperimentalHorologistApi::class)
@WearPreviewDevices
@Composable
private fun PreviewLoadingAppLauncherScreen() {
    val context = LocalContext.current

    val uiState = remember(context) {
        AppLauncherUiState(
            connectionStatus = WearConnectionStatus.CONNECTED,
            appsList = emptyList(),
            isLoading = true,
            loadAppIcons = true
        )
    }

    AppLauncherScreen(uiState = uiState)
}

@OptIn(ExperimentalHorologistApi::class)
@WearPreviewDevices
@Composable
private fun PreviewNoContentAppLauncherScreen() {
    val context = LocalContext.current

    val uiState = remember(context) {
        AppLauncherUiState(
            connectionStatus = WearConnectionStatus.CONNECTED,
            appsList = emptyList(),
            isLoading = false,
            loadAppIcons = true
        )
    }

    AppLauncherScreen(uiState = uiState)
}

@WearPreviewDevices
@Composable
private fun PreviewAppLauncherSettings() {
    val uiState = remember {
        AppLauncherUiState(
            connectionStatus = WearConnectionStatus.CONNECTED,
            appsList = emptyList(),
            isLoading = true,
            loadAppIcons = true
        )
    }

    AppLauncherSettings(uiState = uiState)
}