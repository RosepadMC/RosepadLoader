package net.buj.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Terrible binary diff generator and applier
 */
public class BadBinDiff {
    public static byte[] diff(byte[] src, byte[] target) {
        byte[] bytes = new byte[target.length];

        for (int i = 0; i < bytes.length; i++) {
            byte a = i < src.length ? src[i] : 0;
            byte b = target[i];
            bytes[i] = (byte) (a ^ b); // xor my beloved
        }

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bytes.length);
             GZIPOutputStream compression = new GZIPOutputStream(byteStream)) {
            compression.write(bytes);
            compression.close();
            return byteStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] patch(byte[] src, byte[] patch) {
        byte[] uncompressed;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ByteArrayInputStream patchStream = new ByteArrayInputStream(patch);
             GZIPInputStream decompression = new GZIPInputStream(patchStream)) {
            byte[] buffer = new byte[32767];
            int len;
            while ((len = decompression.read(buffer)) != -1) {
                byteStream.write(buffer, 0, len);
            }
            uncompressed = byteStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] bytes = new byte[uncompressed.length];

        for (int i = 0; i < uncompressed.length; i++) {
            byte a = i < src.length ? src[i] : 0;
            byte b = uncompressed[i];
            bytes[i] = (byte) (a ^ b); // xor my beloved
        }

        return bytes;
    }
}
