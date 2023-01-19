package tech.tresearchgroup.palila.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class FileSystemController {
    public static byte[] readByteRange(Path file, long start, long end) throws IOException {
        end = end + 1;
        FileInputStream fis = new FileInputStream(file.toFile());
        int size = (int) (end - start);
        if (size < 0) {
            size = 0;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        fis.getChannel().read(byteBuffer, start);
        fis.close();
        return byteBuffer.array();
    }
}
