package dev.forcetower.exotester

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSource

class AESCypherDataSourceFactory(
    private val source: DataSource,
    private val secret: String
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return AesCipherDataSource(secret.toByteArray(), source)
    }
}