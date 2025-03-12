package com.whmsdm.process.util;

import com.whmsdm.WHilbertUtil;
import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.octree.Octree;
import com.whmsdm.vdbTree.VdbTree;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessUtil {
    public static int Lmax;

    public static VdbTree vdbTree;

    public static Octree octree;

    public static List<String> propertiesName;

    public static List<Integer> childrenBlockNums;

    public static long num = 32 * 32 * 32 + 64 * 64 * 64 + 128 * 128 * 128;

    public static int[] blockNums = new int[]{32, 64, 128};

    public static int LOffset = 5;

    public static final int MSBM_FILE_HEADER = 704;

    public static Object read(FileChannel fc, int length, String type) throws IOException {
        Object res = null;
        ByteBuffer byteBuffer = null;
        switch (type) {
            case "string":
                byteBuffer = ByteBuffer.wrap(new byte[length == 0 ? 8 : length]);
                fc.read(byteBuffer);
                res = new String(byteBuffer.array(), Charset.forName("GBK"));
                break;
            case "float":
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                byte[] floatArray = byteBuffer.array();
                ParserUtil.reverseArray(floatArray);
                res = ParserUtil.parseFloat(floatArray);
                break;
            case "float1":
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                byte[] floatArray2 = byteBuffer.array();
                res = ParserUtil.parseFloat(floatArray2);
                break;
            case "int":
                byteBuffer = ByteBuffer.wrap(new byte[4]);
                fc.read(byteBuffer);
                res = ParserUtil.parseInt(byteBuffer.array());
                break;
            case "time_t":
            case "long":
            case "unsigned long long":
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] longArray = byteBuffer.array();
                ParserUtil.reverseArray(longArray);
                res = ParserUtil.parseLong(longArray);
                break;
            case "long1":
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] longArray2 = byteBuffer.array();
                res = ParserUtil.parseLong(longArray2);
                break;
            case "UUID":
                byteBuffer = ByteBuffer.wrap(new byte[16]);
                fc.read(byteBuffer);
                res = new String(byteBuffer.array(), StandardCharsets.UTF_8);
                break;
            case "double":
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] doubleArray = byteBuffer.array();
                ParserUtil.reverseArray(doubleArray);
                res = ParserUtil.parseDouble(doubleArray);
                break;
            case "double1":
                byteBuffer = ByteBuffer.wrap(new byte[8]);
                fc.read(byteBuffer);
                byte[] double1Array = byteBuffer.array();
                res = ParserUtil.parseDouble(double1Array);
                break;
            case "char":
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

    // 根据编号获取块体坐标
    public static long[] getBlockCor(long id, long numsX, long numsY, long numsZ) {
        long[] cor = new long[3];
        cor[0] = (id % (numsY * numsX)) % numsX;
        cor[1] = (id % (numsY * numsX)) / numsX;
        cor[2] = id / (numsY * numsX);
        return cor;
    }

    // 块体W编码
    public static long blockEncode(long id, int L, long numsX, long numsY, long numsZ) {
        return WHilbertUtil.encode(Lmax, L, 3, getBlockCor(id, numsX, numsY, numsZ), LOffset);
    }


    // 根据编码计算块体在当前层的id
    public static long getBlockIdByCode(long blockCode, long numsX, long numsY, long numsZ) {
        return getBlockIdByCor(WHilbertUtil.decode(Lmax, blockCode, 3, LOffset), numsX, numsY, numsZ);
    }

    // 根据cor计算块体id
    public static long getBlockIdByCor(long[] cor, long numsX, long numsY, long numsZ) {
        // 确保输入的 cor 数组长度为 3
        if (cor.length != 3) {
            throw new IllegalArgumentException("cor 数组长度必须为 3");
        }
        // 计算 id
        long id = cor[0] + cor[1] * numsX + cor[2] * numsX * numsY;
        return id;
    }


    // 末尾添加0，使字符串长度达到n
    public static String padWithZeros(String str, int n) {
        if (str.length() >= n) {
            return str; // 如果字符串长度已经达到或超过n，直接返回原字符串
        }

        StringBuilder padded = new StringBuilder(str);
        int zerosToAdd = n - str.length();

        // 在字符串末尾添加zerosToAdd个0
        for (int i = 0; i < zerosToAdd; i++) {
            padded.append('0');
        }

        return padded.toString();
    }

    /***
     * 设置配置
     * @param first 起始层级，单方向块体数
     * @param last 结束层级，单方向块体数
     * @param propertyNum 属性个数
     */
    public static void setConfig(int first, int last, int propertyNum) {
        int lFirst = calLog(first);
        int lList = calLog(last);
        int i = lList - lFirst + 1;

        ProcessUtil.LOffset = lFirst;

        List<Integer> childrenBlockNums = new ArrayList<>();
        for (int l = i - 1; l >= 0; l--) {
            childrenBlockNums.add(1 << l);
        }
        ProcessUtil.childrenBlockNums = childrenBlockNums;

        List<String> propertiesName = new ArrayList<>();
        for (int l = 0; l < propertyNum; l++) {
            int quotient = l / 5;
            int remainder = l % 5;
            String property;
            switch (remainder) {
                case 0:
                    property = "地层";
                    break;
                case 1:
                    property = "断裂";
                    break;
                case 2:
                    property = "Pb";
                    break;
                case 3:
                    property = "TS";
                    break;
                case 4:
                    property = "Ag";
                    break;
                default:
                    property = "";
            }
            if (quotient > 0) {
                property += quotient;
            }
            propertiesName.add(property);
        }
        ProcessUtil.propertiesName = propertiesName;

        //添加文件路径
        Map<Integer, String> paths = new HashMap<>();
        for (int l = 0; l < i; l++) {
            String path = FilePathConstants.A3D_FILE_PATH + (first << l) + "-3Pb-Ts-Ag-fault-stratum.a3d";
            paths.put(l, path);
        }
        ProcessUtil.Lmax = paths.size() - 1;

        int num = 0;
        int[] blockNums = new int[i];
        for (int l = 0; l < i; l++) {
            int block = first << l;
            blockNums[l] = block;
            num += block * block * block;
        }
        ProcessUtil.num = num;

        ProcessUtil.blockNums = blockNums;
    }

    public static int calLog(int num) {
        int result = 0;
        while (num > 1) {
            num >>= 1;  // 右移一位，除2
            result++;
        }
        return result;
    }
}
