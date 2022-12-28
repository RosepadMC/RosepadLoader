package net.buj.loader;

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

        return bytes;
    }
}
