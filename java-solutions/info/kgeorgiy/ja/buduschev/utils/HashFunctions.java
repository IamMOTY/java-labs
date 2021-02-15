package info.kgeorgiy.ja.buduschev.utils;

import java.io.*;

public class HashFunctions {
    final static int BUFFER_SIZE = 4096;


    public static long PJW(InputStream input) throws IOException {
        long hash = 0;
        long high;
        byte[] buff = new byte[BUFFER_SIZE];
        int size;
        while ((size = input.read(buff)) != -1)
            for (int i = 0; i < size; i++) {
                hash = (hash << 8) + (buff[i] & 0xFF);
                if ((high = (hash & 0xFF00_0000_0000_0000L)) != 0) {
                    hash ^= high >> 48;
                    hash &= ~high;
                }
            }
        return hash;
    }


}
