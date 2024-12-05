package com.process.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/***
 *  文件读写工具类
 */
public class RWUtils {

    public static Object read(FileChannel fc, int length, String type) throws IOException {
        Object res = null;
        ByteBuffer byteBuffer = null;
        switch (type) {
            case "string" :
                byteBuffer = ByteBuffer.wrap(new byte[length == 0 ? 8 : length]);
                fc.read(byteBuffer);
                res = new String(byteBuffer.array(), Charset.forName("GBK"));
                break;
            case "float_reverse" :
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                byte[] floatArray = byteBuffer.array();
                reverseArray(floatArray);
                res = parseFloat(floatArray);
                break;
            case "float":
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                byte[] floatArray2 = byteBuffer.array();
                res = parseFloat(floatArray2);
                break;
            case "int" :
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                res = parseInt(byteBuffer.array());
                break;
            case "time_t_reverse" :
            case "long_reverse" :
            case "unsigned_long_long_reverse" :
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] longArray = byteBuffer.array();
                reverseArray(longArray);
                res = parseLong(longArray);
                break;
            case "long":
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] longArray2 = byteBuffer.array();
                res = parseLong(longArray2);
                break;
            case "UUID" :
                byteBuffer = ByteBuffer.wrap(new byte[16]);
                fc.read(byteBuffer);
                res = new String(byteBuffer.array(), StandardCharsets.UTF_8);
                break;
            case "double_reverse" :
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] doubleArray = byteBuffer.array();
                reverseArray(doubleArray);
                res = parseDouble(doubleArray);
                break;
            case "double" :
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] double1Array = byteBuffer.array();
                res = parseDouble(double1Array);
                break;
            case "char" :
                byteBuffer = ByteBuffer.wrap(new byte[1]);
                fc.read(byteBuffer);
                res = (char) byteBuffer.array()[0];
                break;
            case "char2": // Adding the case for two-byte char
                byteBuffer = ByteBuffer.wrap(new byte[2]);
                fc.read(byteBuffer);
                char ch = byteBuffer.getChar(0); // Assuming char is encoded in UTF-16
                res = ch;
                break;
        }
        return res;
    }

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
