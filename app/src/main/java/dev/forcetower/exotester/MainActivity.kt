package dev.forcetower.exotester

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSink
import com.google.android.exoplayer2.util.Util
import dev.forcetower.exotester.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var databaseProvider: DatabaseProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseProvider = ExoDatabaseProvider(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)!!
            val group = NotificationChannelGroup("channel-group", "channel")
            manager.createNotificationChannelGroup(group)

            val channel = NotificationChannel("channel", "nada", NotificationManager.IMPORTANCE_LOW)
            channel.group = "channel-group"
            manager.createNotificationChannel(channel)
        }

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
//        playCache(binding)
//        playDefault(binding)
        playEnc(binding)
//        requestDownload()
    }

    private fun requestDownload() {
        val request = DownloadRequest(
            "78875",
            DownloadRequest.TYPE_PROGRESSIVE,
            Uri.parse("http://mirrors.standaloneinstaller.com/video-sample/Panasonic_HDC_TM_700_P_50i.mp4"),
            Collections.emptyList(),
            null,
            null
        )

        DownloadService.sendAddDownload(this, ExoDownloadService::class.java, request, true)
    }

    private fun playCache(binding: ActivityMainBinding) {
        val uri = Uri.parse("http://mirrors.standaloneinstaller.com/video-sample/Panasonic_HDC_TM_700_P_50i.mp4")

        val downloadCache = SimpleCache(
            filesDir,
            NoOpCacheEvictor(),
            databaseProvider
        )

        val defaultSource = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo"))
        val cacheSourceFactory = CacheDataSourceFactory(downloadCache, defaultSource)
        val dataSourceFactory = AESCypherDataSourceFactory(cacheSourceFactory.createDataSource(), "a_really_strong_password")

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)

        val player = SimpleExoPlayer.Builder(this).build()
        player.playWhenReady = true
        binding.videoView.player = player
        player.prepare(mediaSource)
    }

    private fun playDefault(binding: ActivityMainBinding) {
        val uri = Uri.parse("http://mirrors.standaloneinstaller.com/video-sample/lion-sample.mp4")
        val defaultSource = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo"))

        val source = ProgressiveMediaSource.Factory(defaultSource)
            .createMediaSource(uri)

        val player = SimpleExoPlayer.Builder(this).build()
        player.playWhenReady = true
        binding.videoView.player = player
        player.prepare(source)
    }

    private fun playEnc(binding: ActivityMainBinding) {
//        val uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/unes-uefs.appspot.com/o/lion_cipher2?alt=media&token=2c81f79f-9695-4108-aeed-e1ff453130b4")
        val uri = Uri.parse("asset:///d.mp4.enc")

        val defaultSource = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo"))
        val dataSourceFactory = AESCypherDataSourceFactory(defaultSource.createDataSource(), "a_really_strong_password")

        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)

        val player = SimpleExoPlayer.Builder(this).build()
        player.playWhenReady = true
        binding.videoView.player = player
        player.prepare(source)
    }

    private fun encryptFile(file: File, secretKey: String) {
        try {
            val am = this.assets

            // Fully read the input stream.
            val inputStream = am.open("lion.mp4")
            val inputStreamBytes = Util.toByteArray(inputStream)
            inputStream.close()

            // Create a sink that will encrypt and write to file.
            val encryptingDataSink = AesCipherDataSink(
                Util.getUtf8Bytes(secretKey),
                object : DataSink {
                    private lateinit var fileOutputStream: FileOutputStream

                    @Throws(IOException::class)
                    override fun open(dataSpec: DataSpec) {
                        fileOutputStream = FileOutputStream(file)
                    }

                    @Throws(IOException::class)
                    override fun write(
                        buffer: ByteArray,
                        offset: Int,
                        length: Int
                    ) {
                        fileOutputStream.write(buffer, offset, length)
                    }

                    @Throws(IOException::class)
                    override fun close() {
                        fileOutputStream.close()
                    }
                })

            // Push the data through the sink, and close everything.
            encryptingDataSink.open(DataSpec(Uri.fromFile(file)))
            encryptingDataSink.write(inputStreamBytes, 0, inputStreamBytes.size)
            encryptingDataSink.close()
            Toast.makeText(this, "File encrypted", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}