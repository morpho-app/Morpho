package com.morpho.app.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.morpho.app.screens.skyline.FeedTab
import kotlin.math.min

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SkylineTopBar(
    tabList: List<FeedTab>,
    modifier: Modifier = Modifier,
    tabIndex: Int = 0,
    onChanged: (Int) -> Unit = {},
    mainButton: @Composable() ((()->Unit) -> Unit)? = null,
    onButtonClicked: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(tabIndex) }
    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {

            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.offset(y = (-5).dp),
                shape = MaterialTheme.shapes.small.copy(
                    bottomStart = CornerSize(0.dp),
                    //topStart = CornerSize(0.dp),
                    topEnd = CornerSize(0.dp),
                )
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
                                .padding(horizontal = 10.dp)
                                .size(30.dp)
                        )
                    }
                }
            }

            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .padding(0.dp)
                    .offset(y = (-8).dp),
                edgePadding = 10.dp,
                //divider = {}
            ) {
                Tab(selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        onChanged(selectedTab)
                    },
                    text = {
                        Text("Home")
                    }
                )
                tabList.forEachIndexed { index, tab ->
                    Tab(selected = selectedTab == (1 + index),
                        onClick = {
                            selectedTab = min((1 + index), tabList.size)
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
@Preview(showBackground = true)
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