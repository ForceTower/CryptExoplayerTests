package dev.forcetower.exotester

import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.crypto.AesCipherDataSink

class AESCipherDataSinkFactory(
    private val secret: String,
    private val dataSink: DataSink
) : DataSink.Factory {
    override fun createDataSink(): DataSink {
        return AesCipherDataSink(secret.toByteArray(), dataSink)
    }
}