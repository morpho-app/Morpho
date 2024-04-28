package com.morpho.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.morpho.app.model.uidata.ContentCardMapEntry
import com.morpho.app.ui.theme.roundedBotR
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max
import kotlin.math.min

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SkylineTopBar(
    tabList: ImmutableList<ContentCardMapEntry>,
    modifier: Modifier = Modifier,
    tabIndex: Int = 0,
    onChanged: (Int) -> Unit = {},
    mainButton: @Composable ((()->Unit) -> Unit)? = null,
    onButtonClicked: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(max(0, min(tabIndex, tabList.lastIndex))) }
    TopAppBar(
        title = {},
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.offset(y = (-5).dp, x= (-5).dp),
                shape = roundedBotR.small
            ) {
                if (mainButton != null) {
                    mainButton(onButtonClicked)
                } else {
                    IconButton(
                        onClick = onButtonClicked,
                        modifier = Modifier
                            .padding(bottom = 5.dp, top = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .size(55.dp)
                        )
                    }
                }
            }
        },
        actions = {
            val padding = if (mainButton != null) {
                Modifier.padding(start = 50.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
            } else {
                Modifier.padding(start = 20.dp, top = 0.dp, bottom = 0.dp, end = 0.dp)
            }

            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = modifier.offset(y = (-8).dp, x = 4.dp ),
                edgePadding = 10.dp,
                indicator = { tabPositions ->
                    if(tabPositions.isNotEmpty()) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[max(0, min(selectedTab, tabList.lastIndex))])
                        )
                    }
                },
            ) {
                tabList.forEachIndexed { index, tab ->
                    Tab(selected = selectedTab == index,
                        onClick = {
                            selectedTab = max(0, min(index, tabList.lastIndex))
                            onChanged(selectedTab)
                        },
                        text = {
                            Text(tab.title)
                        }
                    )
                }
            }
        },
        //scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TopAppBarPreview(){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    TopAppBar(
        title = {},
        actions = {
            Column {
                IconButton(
                    onClick = { /* doSomething() */ },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 5.dp, top = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(30.dp)
                    )

                }
                HorizontalDivider(
                    Modifier
                        .offset(y = (-9.25).dp)
                        .width(60.dp),

                    )
            }
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(0.dp)
                //divider = {}
            ) {
                Tab(selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text("Home")
                    }
                )
                Tab(selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("Feed 1")
                    }
                )
                Tab(selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text("Feed 2")
                    }
                )
                Tab(selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text("Feed 3")
                    }
                )
            }
        },
    )
}