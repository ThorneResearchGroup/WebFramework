package tech.tresearchgroup.palila.controller;

import io.activej.serializer.BinarySerializer;

public class ActiveJSerializer {
    public static byte[] serialize(Object object, BinarySerializer binarySerializer) {
        byte[] buffer = new byte[512];
        binarySerializer.encode(buffer, 0, object);
        return buffer;
    }

    public static Object deserialize(byte[] buffer, BinarySerializer binarySerializer) {
        return binarySerializer.decode(buffer, 0);
    }
}
