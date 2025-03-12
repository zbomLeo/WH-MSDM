package com.whmsdm.process.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ParserUtil {
    public static void reverseArray(byte[] array) {
        int start = 0;
        int end = array.length - 1;

        while (start < end) {
            // 交换数组的两个元素
            byte temp = array[start];
            array[start] = array[end];
            array[end] = temp;

            // 移动指针
            start++;
            end--;
        }
    }


    public static long parseLong(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 8);
        return buffer.getLong();
    }
    public static double parseDouble(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 8);
        return buffer.getDouble();
    }
    public static byte[] double2Byte(double value) {
        byte[] array = ByteBuffer.allocate(Double.BYTES).putDouble(value).array();
//        reverseArray(array);
        return array;
    }
    public static float parseFloat(byte[] buf) {
        Float value = ByteBuffer.wrap(buf).getFloat();
        return value.isNaN() ? 0F : value;
    }
    public static byte[] float2Byte(float value) {
        byte[] array = ByteBuffer.allocate(Float.BYTES).putFloat(value).array();
//        reverseArray(array);
        return array;
    }
    public static char parseChar(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 2);
        return buffer.getChar();

    }
    public static byte[] long2Byte(long value) {
        byte[] array = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
//        reverseArray(array);
        return array;
    }
    public static byte[] int2Byte(int value) {
        return ByteBuffer.allocate(Integer.SIZE/Byte.SIZE).putInt(value).array();
    }
    public static byte[] short2Byte(short value) {
        return ByteBuffer.allocate(Short.SIZE/Byte.SIZE).putShort(value).array();
    }

    public static short byte2Short(byte[] buf) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf, 0, 2);
        return byteBuffer.getShort();
    }

    public static int parseInt(byte[] buf) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf, 0, 4);
        return byteBuffer.getInt();
    }

    public static short parseShort(byte[] buf) {
        ByteBuffer buffer = ByteBuffer.wrap(buf, 0, 2);
        return buffer.getShort();
    }

    public static byte[] string2Byte(String value, int length) throws UnsupportedEncodingException {
        return Arrays.copyOf(value.getBytes("GBK"), length);
    }

    public static byte[] char2Byte(char value) {
        return ByteBuffer.allocate(2).putChar(value).array();
    }
}
