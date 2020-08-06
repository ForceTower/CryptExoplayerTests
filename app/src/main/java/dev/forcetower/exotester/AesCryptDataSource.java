package dev.forcetower.exotester;

import static com.google.android.exoplayer2.util.Util.castNonNull;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.crypto.AesFlushingCipher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;

/**
 * A {@link DataSource} that decrypts the data read from an upstream source.
 */
public final class AesCryptDataSource implements DataSource {

  private final DataSource upstream;
  private final byte[] secretKey;

  @Nullable private AesFlushingCipher cipher;

  public AesCryptDataSource(byte[] secretKey, DataSource upstream) {
    this.upstream = upstream;
    this.secretKey = secretKey;
  }

  @Override
  public void addTransferListener(TransferListener transferListener) {
    upstream.addTransferListener(transferListener);
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    long dataLength = upstream.open(dataSpec);
    long nonce = getFNV64Hash(dataSpec.key);
    cipher = new AesFlushingCipher(Cipher.ENCRYPT_MODE, secretKey, nonce,
        dataSpec.absoluteStreamPosition);
    return dataLength;
  }

  @Override
  public int read(byte[] data, int offset, int readLength) throws IOException {
    if (readLength == 0) {
      return 0;
    }
    int read = upstream.read(data, offset, readLength);
    if (read == C.RESULT_END_OF_INPUT) {
      return C.RESULT_END_OF_INPUT;
    }
    castNonNull(cipher).updateInPlace(data, offset, read);
    return read;
  }

  @Override
  @Nullable
  public Uri getUri() {
    return upstream.getUri();
  }

  @Override
  public Map<String, List<String>> getResponseHeaders() {
    return upstream.getResponseHeaders();
  }

  @Override
  public void close() throws IOException {
    cipher = null;
    upstream.close();
  }

  public static long getFNV64Hash(@Nullable String input) {
    if (input == null) {
      return 0;
    }

    long hash = 0;
    for (int i = 0; i < input.length(); i++) {
      hash ^= input.charAt(i);
      // This is equivalent to hash *= 0x100000001b3 (the FNV magic prime number).
      hash += (hash << 1) + (hash << 4) + (hash << 5) + (hash << 7) + (hash << 8) + (hash << 40);
    }
    return hash;
  }
}
