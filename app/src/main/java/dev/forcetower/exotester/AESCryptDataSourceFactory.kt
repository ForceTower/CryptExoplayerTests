package dev.forcetower.exotester

import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSource

class AESCryptDataSourceFactory(
    private val source: DataSource,
    private val secret: String
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return AesCryptDataSource(secret.toByteArray(), source)
    }
}