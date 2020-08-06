package dev.forcetower.exotester

import android.app.Notification
import android.util.Log
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.*
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.*


class ExoDownloadService : DownloadService(0x7822) {
    private val TAG = "ExoDownloadService"
    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        Log.d(TAG, "onCreate: Exo create")
        super.onCreate()
//        createDownloader()

//        val cacheSink = CacheDataSinkFactory(downloadCache, 100 * 1024 * 1024).createDataSink()
//
//        CacheDataSourceFactory(downloadCache, dataSourceFactory)
//
//        val helper = DownloaderConstructorHelper(
//            downloadCache,
//            dataSourceFactory,
//            CacheDataSourceFactory(downloadCache, dataSourceFactory),
//            AESCipherDataSinkFactory("AESCipherDataSinkFactory", cacheSink),
//            null
//        )
    }

    private fun createDownloader() {
        databaseProvider = ExoDatabaseProvider(this)

        val downloadCache = SimpleCache(
            filesDir,
            NoOpCacheEvictor(),
            databaseProvider
        )

        val defaultSource = DefaultHttpDataSourceFactory("exo")
        val dataSourceFactory = AESCypherDataSourceFactory(defaultSource.createDataSource(), "a_really_strong_password")

        downloadManager = DownloadManager(
            this,
            databaseProvider,
            downloadCache,
            dataSourceFactory
        )
    }

    override fun getDownloadManager(): DownloadManager {
        if (!::downloadManager.isInitialized) createDownloader()
        return downloadManager
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        return DownloadNotificationHelper(this, "channel")
            .buildProgressNotification(
                R.drawable.ic_download,
                null,
                null,
                downloadManager.currentDownloads
            )
    }

    override fun getScheduler(): Scheduler? {
        return PlatformScheduler(this, 0x222)
    }
}