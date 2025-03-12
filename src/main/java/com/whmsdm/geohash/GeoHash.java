package com.whmsdm.geohash;

import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.process.util.ParserUtil;
import com.whmsdm.process.util.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeoHash {
    public static void createGeoHashFile(Map<Integer, String> paths, String fileName) throws IOException {
        String geoHashFileName = fileName;
        String path = FilePathConstants.FILE_PATH;
        File file = new File(path + geoHashFileName);
        if (file.createNewFile()) {
            System.out.println("文件已创建成功。");
        } else {
            System.out.println("文件已存在，无需创建。");

            return;
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();

        writeGeoHashFileHeader(fc, paths);

        List<BitSet> validity = new ArrayList<>();
        validity = getValidity(paths);

        // 用于写块体有效性
        byte[] T = "T".getBytes(StandardCharsets.ISO_8859_1);
        byte[] F = "F".getBytes(StandardCharsets.ISO_8859_1);

        // 遍历每一个层级
        for (Map.Entry<Integer, String> entry : paths.entrySet()) {
            int entryLevel = entry.getKey();  // 层级
            String entryPath = entry.getValue();  // 该层级文件路径
            File entryFile = new File(entryPath);
            RandomAccessFile entryRaf = new RandomAccessFile(entryFile, "rw");
            FileChannel entryFc = entryRaf.getChannel();  // 该层级文件通道
            int entryNum = (int) Math.pow(2, entryLevel + ProcessUtil.LOffset);  // 该层级一个方向上块体数

            for (int i = 0; i < entryNum; i++) {
                for (int j = 0; j < entryNum; j++) {
                    for (int k = 0; k < entryNum; k++) {

                        /*写块体有效性*/
                        long entryId = k + j * entryNum + i * entryNum * entryNum;  // 在源文件中的id
                        String geoHashCode = GeoHashCode.encode(entryLevel, new long[]{k, j, i});  // 计算geoHash编码
                        long entryLoc = GeoHashCode.getLoc(geoHashCode);  // 计算在geoHash文件中的位置
//                        System.out.println(geoHashCode);
//                        boolean valid = parentIsValid(geoHashCode, paths);
                        boolean valid = parentIsValid(geoHashCode, validity);
                        fc.position(688 + entryLoc);
                        if (valid) {
                            fc.write(ByteBuffer.wrap(T));
                        } else {
                            fc.write(ByteBuffer.wrap(F));
                        }

                        /*写属性*/
                        if (valid) {
                            for (int m = 0; m < ProcessUtil.propertiesName.size(); m++) {
                                fc.position(688 + ProcessUtil.num + (entryLoc * 4) + (m * ProcessUtil.num * 4));
                                float property = getBlockProperty(ProcessUtil.propertiesName.get(m % 5), entryId, entryFc, entryLevel);
                                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(property)));
                            }
                        } else {
                            for (int m = 0; m < ProcessUtil.propertiesName.size(); m++) {
                                fc.position(688 + ProcessUtil.num + (entryLoc * 4) + (m * ProcessUtil.num * 4));
                                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean parentIsValid(String geoHashCode, List<BitSet> validity) {
        long[] cor = GeoHashCode.decode(geoHashCode);
        int level = GeoHashCode.calculateLevel(geoHashCode);
        long num = (long) Math.pow(2, level + ProcessUtil.LOffset);
        long id = ProcessUtil.getBlockIdByCor(cor, num, num, num);
        boolean valid = validity.get(level).get((int) id);
        if (valid) {
            if (level == 0) return true;
            String parentCode = GeoHashCode.getParentCode(geoHashCode, level - 1);
            return parentIsValid(parentCode, validity);
        } else {
            return false;
        }
    }

    public static List<BitSet> getValidity(Map<Integer, String> paths) {
        List<BitSet> res = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            File file = new File(path);
            try {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel fc = raf.getChannel();
                fc.position(292);
                long n = (long) Math.pow(2, i + ProcessUtil.LOffset);
                long nums = n * n * n;
                BitSet blockValidity = new BitSet((int) nums);
                for (long j = 0; j < nums; j++) {
                    char valid = (char) ProcessUtil.read(fc, 1, "char");
                    if ('T' == valid) {
                        blockValidity.set((int) j);
                    }
                }
                res.add(blockValidity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }


    public static float getBlockProperty(String propertyName, long id, FileChannel fc, int level) throws IOException {
        long num = (int) Math.pow(2, level + ProcessUtil.LOffset);  // 一个方向上块体数
        long index = ProcessUtil.propertiesName.indexOf(propertyName);
        fc.position(844 + num * num * num * (index * 4 + 1) + 4 * id);
        return (float) ProcessUtil.read(fc, 4, "float");
    }


    /*写文件头*/
    public static void writeGeoHashFileHeader(FileChannel fc, Map<Integer, String> paths) throws IOException {
        // 获得最低层级文件的文件通道
        String path = paths.get(ProcessUtil.Lmax);
        File file = new File(path);
        RandomAccessFile LmaxRaf = new RandomAccessFile(file, "rw");
        FileChannel LmaxFc = LmaxRaf.getChannel();

        fc.position(0);
        //多尺度模型名称
        fc.write(ByteBuffer.wrap(ParserUtil.string2Byte("Multi-scale GeoHash Block Model", 32)));
        // 标识
        fc.write(ByteBuffer.wrap(ParserUtil.string2Byte(UUID.randomUUID().toString(), 36)));

        double[] leftTopCor = new double[3];
        LmaxFc.position(116);
        // 从Lmax层级文件拿到左上角坐标
        leftTopCor[0] = (double) ProcessUtil.read(LmaxFc, 8, "double");
        leftTopCor[1] = (double) ProcessUtil.read(LmaxFc, 8, "double");
        leftTopCor[2] = (double) ProcessUtil.read(LmaxFc, 8, "double");

        // 写入左上角坐标
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(leftTopCor[0])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(leftTopCor[1])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(leftTopCor[2])));

        // 从Lmax层级文件拿到最小块体尺寸
        LmaxFc.position(LmaxFc.position() + 8);
        double[] minBlockSize = new double[3];
        minBlockSize[0] = (double) ProcessUtil.read(LmaxFc, 8, "double");
        minBlockSize[1] = (double) ProcessUtil.read(LmaxFc, 8, "double");
        minBlockSize[2] = (double) ProcessUtil.read(LmaxFc, 8, "double");

        // 从Lmax层级文件中拿到最小块体各方向数量
        LmaxFc.position(LmaxFc.position() + 8);
        long[] minBlockNums = new long[3];
        minBlockNums[0] = (long) ProcessUtil.read(fc, 8, "long");
        minBlockNums[1] = (long) ProcessUtil.read(fc, 8, "long");
        minBlockNums[2] = (long) ProcessUtil.read(fc, 8, "long");

        // 计算得到右下角坐标
        double[] rightBottomCor = new double[3];
        rightBottomCor[0] = leftTopCor[0] + minBlockSize[0] * minBlockNums[0];
        rightBottomCor[1] = leftTopCor[1] + minBlockSize[1] * minBlockNums[1];
        rightBottomCor[2] = leftTopCor[2] + minBlockSize[2] * minBlockNums[2];

        // 写入右下角坐标
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(rightBottomCor[0])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(rightBottomCor[1])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(rightBottomCor[2])));

        // 写入最小块体尺寸
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(minBlockSize[0])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(minBlockSize[1])));
        fc.write(ByteBuffer.wrap(ParserUtil.double2Byte(minBlockSize[2])));

        // 写入最小块体各方向数量
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(minBlockNums[0])));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(minBlockNums[1])));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(minBlockNums[2])));

        // 最大层级Lmax
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(ProcessUtil.Lmax)));
        // 块体数
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(ProcessUtil.num)));
        // 属性列表
        String names = "";
        for (String propertyName : ProcessUtil.propertiesName) {
            names += propertyName + "\t";
        }
        fc.write(ByteBuffer.wrap(ParserUtil.string2Byte(names, 512)));
    }


    public static boolean parentIsValid(String geoHashCode, Map<Integer, String> paths) throws IOException {
        int level = GeoHashCode.calculateLevel(geoHashCode);
        long[] cor = GeoHashCode.decode(geoHashCode);
        long num = (long) Math.pow(2, level + ProcessUtil.LOffset);
        long id = cor[0] + cor[1] * num + cor[2] * num * num;
        File file = new File(paths.get(level));
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            FileChannel fc = raf.getChannel();
            fc.position(292 + id);
            char valid = (char) ProcessUtil.read(fc, 1, "char");

            if (valid == 'T') {
                if (level == 0) return true;
                String parentCode = GeoHashCode.getParentCode(geoHashCode, level - 1);
                return parentIsValid(parentCode, paths);
            } else {
                return false;
            }
        }
    }
}
