package com.kaancetin.displaypaginglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

// --------------------
// DATA MODEL
// --------------------
data class User(
    val id: Int,
    val name: String
)

// --------------------
// PAGING SOURCE (FAKE BACKEND)
// --------------------
class FakeUserPagingSource : PagingSource<Int, User>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        delay(1200) // loading hissi

        val users = List(pageSize) { index ->
            val id = page * pageSize + index
            User(
                id = id,
                name = "User $id"
            )
        }

        return LoadResult.Page(
            data = users,
            prevKey = if (page == 0) null else page - 1,
            nextKey = page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition
    }
}

// --------------------
// VIEWMODEL
// --------------------
class MainViewModel : ViewModel() {

    val users: Flow<PagingData<User>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        FakeUserPagingSource()
    }.flow.cachedIn(viewModelScope)
}

// --------------------
// ACTIVITY
// --------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                PagingScreen()
            }
        }
    }
}

// --------------------
// COMPOSE UI (RESMİ & LOW-CODE)
// --------------------
@Composable
fun PagingScreen(
    viewModel: MainViewModel = viewModel()
) {
    val lazyPagingItems = viewModel.users.collectAsLazyPagingItems()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            // 🔥 ANDROID RESMİ PATTERN
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { it.id },
                contentType = lazyPagingItems.itemContentType { "User" }
            ) { index ->
                val user = lazyPagingItems[index]
                user?.let {
                    Text(
                        text = it.name,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            // Sayfa eklenirken
            if (lazyPagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ){

                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(24.dp)
                    )
                    }
                }
            }
        }
    }
}
