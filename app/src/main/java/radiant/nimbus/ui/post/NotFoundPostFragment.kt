package radiant.nimbus.ui.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import radiant.nimbus.api.AtUri
import radiant.nimbus.ui.utils.indentLevel

@Composable
fun NotFoundPostFragment(
    modifier: Modifier = Modifier,
    post: AtUri? = null,
    indentLevel: Int = 0,
    role: PostFragmentRole = PostFragmentRole.Solo,
 ) {
    Column(
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
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}