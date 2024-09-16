package com.morpho.app.ui.post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.morpho.app.ui.elements.AvatarShape
import com.morpho.app.ui.elements.OutlinedAvatar
import com.morpho.app.ui.elements.WrappedColumn
import com.morpho.butterfly.AtUri
import morpho.app.ui.utils.indentLevel

@Composable
fun NotFoundPostFragment(
    modifier: Modifier = Modifier,
    post: AtUri? = null,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.Solo,
 ) {
    WrappedColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Surface(
            shadowElevation = max((indentLevel - 1).dp, 0.dp),
            tonalElevation = indentLevel.dp,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth(indentLevel(indentLevel.toFloat()))

        ) {
            Column {
                SelectionContainer {
                    Text(
                        text = "Post deleted or not found",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraLight),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderSkylineItem(
    modifier: Modifier = Modifier,
    role: PostFragmentRole = PostFragmentRole.Solo,
    indentLevel: Int = 0,
    elevate: Boolean = false,
) {
    val padding = remember { when(role) {
        PostFragmentRole.Solo -> if(indentLevel == 0) Modifier.padding(2.dp) else Modifier
        PostFragmentRole.PrimaryThreadRoot -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadBranchStart -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadBranchMiddle -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadBranchEnd -> Modifier.padding(start = 0.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
        PostFragmentRole.ThreadRootUnfocused -> Modifier.padding(2.dp)
        PostFragmentRole.ThreadEnd -> Modifier.padding(start = 2.dp, top = 0.dp, end = 2.dp, bottom = 2.dp)
    }}
    WrappedColumn(modifier = modifier.then(padding.fillMaxWidth())) {
        val indent = remember { when(role) {
            PostFragmentRole.Solo -> indentLevel.toFloat()
            PostFragmentRole.PrimaryThreadRoot -> indentLevel.toFloat()
            PostFragmentRole.ThreadBranchStart -> 0.0f//indentLevel.toFloat()
            PostFragmentRole.ThreadBranchMiddle -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadBranchEnd -> 0.0f//indentLevel.toFloat()-1
            PostFragmentRole.ThreadRootUnfocused -> indentLevel.toFloat()
            PostFragmentRole.ThreadEnd -> 0.0f
        }}

        val bgColor = if (role == PostFragmentRole.PrimaryThreadRoot) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(if (elevate ) 2.dp else
                                                                  if (indentLevel > 0) (indentLevel*2).dp else 0.dp)
        }

        Surface (
            shadowElevation = if (elevate || indentLevel > 0) 2.dp else 0.dp,
            tonalElevation = if (elevate && role != PostFragmentRole.ThreadEnd) 2.dp
            else if (indentLevel > 0) (indentLevel*2).dp else 0.dp,
            shape = MaterialTheme.shapes.small,
            //color = bgColor,
            modifier = modifier
                .fillMaxWidth(indentLevel(indent))
                .align(Alignment.End)

        ) {
                Row(
                    modifier = Modifier.padding(end = 6.dp)
                        .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
                ) {

                    if (indent < 2) {
                        OutlinedAvatar(
                            url = "",
                            contentDescription = "Placeholder avatar",
                            size = 45.dp,
                            outlineColor = MaterialTheme.colorScheme.background,
                            avatarShape = AvatarShape.Corner,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }

                    Column(
                        Modifier
                            .padding(top = 4.dp, start = 2.dp, end = 6.dp)
                            .fillMaxWidth()//.fillMaxWidth(indentLevel(indent))
                    ) {

                        Row(
                            modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (indent >= 2) {
                                OutlinedAvatar(
                                    url = "",
                                    contentDescription = "Placeholder avatar",
                                    size = 30.dp,
                                    avatarShape = AvatarShape.Rounded,
                                    outlineColor = MaterialTheme.colorScheme.background,
                                )
                            }
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                                            fontWeight = FontWeight.Medium
                                        )
                                    ) {
                                        "                 "
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = MaterialTheme.typography.labelLarge.fontSize.times(
                                                0.8f
                                            )
                                        )
                                    ) {
                                        append("@                ")
                                    }

                                },
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start)
                                    .weight(10.0F)
                                    .alignByBaseline()
                            )

                            Spacer(modifier = Modifier.width(1.dp).weight(0.1F))
                            Text(
                                text = "       ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = MaterialTheme.typography.labelLarge.fontSize.div(1.2F),
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.End)
                                    //.weight(3.0F)
                                    .alignByBaseline(),
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                softWrap = false,
                            )
                        }

                        Spacer(Modifier.height(100.dp))

                        DummyPostActions()
                    }
                }

            }
        }
    }
}