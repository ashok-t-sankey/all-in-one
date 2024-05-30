package com.example.allinone

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.example.allinone.ui.theme.AllInOneTheme
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var permissionGranted: Boolean = false;
        requestStoragePermission(context = this, onPermissionGranted = { permissionGranted = true })
        setContent {
            AllInOneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    if (permissionGranted) {
                    HomeScreen(
                        context = LocalContext.current,
                        modifier = Modifier.padding(innerPadding)
                    )
//                    } else {
//                        Text(text = "Please grant storage permission")
//                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                // Call controllerFuture.get() to retrieve the MediaController.
                // MediaController implements the Player interface, so it can be
                // attached to the PlayerView UI component.
                PlayerView(this).setPlayer(controllerFuture.get())
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
//        MediaController.releaseFuture()
        super.onStop()
    }

//    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == storagePermissionCOde && grantResults.isNotEmpty() &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // Permission granted, proceed with accessing audio file
//        }
//    }
}

@Composable
fun HomeScreen(context: Context, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Hello App!",
            modifier = modifier
        )
        Box(modifier = Modifier) {
//            val exoPlayer = ExoPlayer.Builder(context).build()
//
//            val uri =
//                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
//            val mediaSource: MediaItem = remember {
//                MediaItem.fromUri(uri)
//            }
//            LaunchedEffect(mediaSource) {
//                exoPlayer.setMediaItem(mediaSource)
//            }
//
//            DisposableEffect(Unit) {
//                onDispose {
//                    exoPlayer.release()
//                }
//            }
//
//            AndroidView(
//                factory = { ctx ->
//                    PlayerView(ctx).apply {
//                        player = exoPlayer
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            var audioList by remember {
                mutableStateOf(emptyList<AudioItem>())
            }
            LaunchedEffect(Unit) {
                Log.d(TAG, "HomeScreen: Launched Effect")
                val mediaStore = context.contentResolver
                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val cursor = mediaStore.query(
                    collection,
                    arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.DURATION,
                    ),
                    null,
                    null,
                    null
                )
                Log.d(TAG, "HomeScreen cursor: $cursor")
                if (cursor != null) {
                    val audioItems = mutableListOf<AudioItem>()
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        val title = cursor.getString(1)
                        val data = cursor.getString(2)
                        val artistId = cursor.getString(3)
                        val year = cursor.getString(4)
                        val duration = cursor.getString(5)
                        Log.d(
                            TAG,
                            "while loop cursor: $id,$title,$data,$artistId, $year, $duration"
                        )
                        audioItems.add(
                            AudioItem(
                                id,
                                title,
                                data,
                                artistId ?: "0",
                                year ?: "",
                                duration ?: "0"
                            )
                        )
                    }
                    Log.d(TAG, "HomeScreen: audioItems: $audioItems")
                    audioList = audioItems
                    cursor.close()
                }
            }
            val viewModel by remember{mutableStateOf(MediaPlayerViewModel(context))}
            MediaPlayerScreen(viewModel, audioList, context)
        }
    }
}

fun requestStoragePermission(context: Context, onPermissionGranted: () -> Unit) {
    val storagePermissionCode = 101
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "android.permission.READ_MEDIA_AUDIO"
    } else {
        "android.permission.READ_EXTERNAL_STORAGE"
    }
    if (ContextCompat.checkSelfPermission(context, permission)
        != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d("TAG", "requestStoragePermission: $permission")
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(permission),
            storagePermissionCode
        )
    } else {
        onPermissionGranted()
    }
}

fun handleAudioClick(
    item: AudioItem,
    context: Context,
    viewModel: MediaPlayerViewModel,
    callback: () -> Unit,
) {
    requestStoragePermission(context, onPermissionGranted = { viewModel.playAudio(item) })
}

@Composable
fun MediaPlayerScreen(
    viewModel: MediaPlayerViewModel,
    audioList: List<AudioItem>,
    context: Context
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Audio Files")
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(8f)
            ) {
                items(audioList) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .height(50.dp)
                            .clickable(
                                onClick = {
                                    handleAudioClick(item, context = context, viewModel, callback =
                                    { viewModel.playAudio(item) })
                                })
                            .background(color = Color.Red)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.audio),
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                        )
                        Column(
                            verticalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(10f)
                        ) {
                            Text(text = item.title, fontSize = 14.sp, modifier = Modifier)
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.artist, fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                Text(
                                    text = item.year,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            PlaybackControls(
                viewModel,
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun PlaybackControls(viewModel: MediaPlayerViewModel, modifier: Modifier) {
    Row(
        horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Yellow)
    ) {
        Button(onClick = { viewModel.playAudio() }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
        }
        Text(text = viewModel.audioFile?.title ?: "No Audio File", modifier = Modifier)
        Button(onClick = { viewModel.pauseAudio() }) {
            Icon(
                painter = painterResource(id = R.drawable.pause_icon),
                contentDescription = "Pause",
                modifier = Modifier.height(25.dp)
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AllInOneTheme {
//        Greeting("Android")
//    }
//}