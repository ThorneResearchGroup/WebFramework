package tech.tresearchgroup.palila.controller;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import tech.tresearchgroup.palila.model.BaseSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionController {
    public static byte[] compress(byte[] data) throws IOException {
        return switch (BaseSettings.compressionMethod) {
            case GZIP -> CompressionController.gzipCompress(data);
            case BR -> CompressionController.brotliCompress(data);
            case NONE -> data;
        };
    }

    public static byte[] decompress(byte[] data) throws IOException {
        return switch (BaseSettings.compressionMethod) {
            case GZIP -> CompressionController.gzipDecompress(data);
            case BR -> CompressionController.brotliDecompress(data);
            case NONE -> data;
        };
    }

    public static byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        try (byteStream) {
            try (GZIPOutputStream stream = new GZIPOutputStream(byteStream)) {
                stream.write(data);
            }
        }
        return byteStream.toByteArray();
    }

    public static byte[] gzipDecompress(byte[] data) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        GZIPInputStream gzipInputStream = new GZIPInputStream(input);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int res = 0;
        byte[] buf = new byte[1024];
        while (res >= 0) {
            res = gzipInputStream.read(buf, 0, buf.length);
            if (res > 0) {
                outputStream.write(buf, 0, res);
            }
        }
        return outputStream.toByteArray();
    }

    public static byte[] brotliCompress(byte[] data) throws IOException {
        Brotli4jLoader.ensureAvailability();
        Encoder.Parameters params = new Encoder.Parameters().setQuality(BaseSettings.compressionQuality);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        try (byteStream) {
            try (BrotliOutputStream stream = new BrotliOutputStream(byteStream, params)) {
                stream.write(data);
            }
        }
        return byteStream.toByteArray();
    }

    public static byte[] brotliDecompress(byte[] data) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        BrotliInputStream brotliInputStream = new BrotliInputStream(input);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int res = 0;
        byte[] buf = new byte[1024];
        while (res >= 0) {
            res = brotliInputStream.read(buf, 0, buf.length);
            if (res > 0) {
                outputStream.write(buf, 0, res);
            }
        }
        return outputStream.toByteArray();
    }
}
