package com.whmsdm.whmsdm;

import com.whmsdm.WHilbertUtil;
import com.whmsdm.entity.block.Value;
import com.whmsdm.entity.constants.A3DFileConstants;
import com.whmsdm.entity.constants.MSBMFileConstants;
import com.whmsdm.entity.coor.Box;
import com.whmsdm.entity.coor.Coordinate;
import com.whmsdm.entity.property.PropertyRange;
import com.whmsdm.entity.utils.DiskIOUtil;
import com.whmsdm.process.util.ProcessUtil;
import com.whmsdm.process.util.RWUtils;
import com.whmsdm.entity.VO.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class MSDM {
    public String multiScaleModelName; // 模型名
    public UUID uuid;
    public double[] leftTopCor;   // 模型左上坐标
    public double[] rightBottomCor;   // 模型右下角坐标
    public double[] minBlockSize;  // 最小块体尺寸
    public long[] minBlockNums;    // 最小块体各方向数量
    public int LOffset;   // W-Hilbert曲线与Hilbert曲线的层级偏移量
    public int N;   // 模型的维度
    public int Lmax;   // 模型尺度（最大层级）
    public long blockNum;   // 块体总数
    public long validBlockNum;   // 有效块数目
    public List<String> propertiesName;    // 属性列表
    public List<BitSet> blockValidity;   // 块体有效性

    public FileChannel fc;    //WH-MSBM文件通道

    /*依据原始文件*/
    public MSDM(Map<Integer, String> paths, int dimensions) {
        this.N = dimensions;
        this.blockValidity = new ArrayList<>();
        long num = 0L;
        long validNum = 0L;
        for (int i = 0; i < paths.size(); i++) {
            try {
                File file = new File(paths.get(i));
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel fc = raf.getChannel();
                fc.position(A3DFileConstants.BLOCK_NUMS_POSITION);   // 在A3D文件中，各方向块体数存储在180字节处
                long[] bNum = new long[3];
                bNum[0] = (long) RWUtils.read(fc, 8, "long_reverse");
                bNum[1] = (long) RWUtils.read(fc, 8, "long_reverse");
                bNum[2] = (long) RWUtils.read(fc, 8, "long_reverse");
                long n = bNum[0] * bNum[1] * bNum[2];   // 当前文件的总块体数
                num += n;
                BitSet bitSet = new BitSet((int) n);   // 用来记录当前文件块体的有效性
                fc.position(A3DFileConstants.VALID_POSITION);   // 在A3D文件中，块体有效性存储在292字节处
                for (int j = 0; j < n; j++) {
                    char valid = (char) RWUtils.read(fc, 1, "char");
                    if ('T' == valid) {
                        bitSet.set(j);
                        validNum++;
                    }
                }
                this.blockValidity.add(bitSet);

                if (i == paths.size() - 1) {
                    // 属性列表
                    fc.position(fc.position() + 8);   // A3D文件中，两个字段之间有8字节的字段名
                    String[] splits = ((String) RWUtils.read(fc, 512, "string")).split("\t");
                    for (int j = 0; j < splits.length; j++) {
                        if ("顶底面".equals(splits[j])) {
                            splits[j] = "断裂";
                        }
                    }
                    this.propertiesName = ProcessUtil.propertiesName;
                    // 模型名称
                    this.multiScaleModelName = "W-Hilbert Multi-scale Block Model";
                    // UUID
                    this.uuid = UUID.randomUUID();
                    // 左上角坐标
                    fc.position(A3DFileConstants.LEFT_TOP_COR_POSITION);   // 在A3D文件中，左上角坐标存储于116字节处
                    this.leftTopCor = new double[3];
                    this.leftTopCor[0] = (double) RWUtils.read(fc, 8, "double_reverse");
                    this.leftTopCor[1] = (double) RWUtils.read(fc, 8, "double_reverse");
                    this.leftTopCor[2] = (double) RWUtils.read(fc, 8, "double_reverse");
                    // 最小块的尺寸
                    fc.position(fc.position() + 8);
                    this.minBlockSize = new double[3];
                    this.minBlockSize[0] = (double) RWUtils.read(fc, 8, "double_reverse");
                    this.minBlockSize[1] = (double) RWUtils.read(fc, 8, "double_reverse");
                    this.minBlockSize[2] = (double) RWUtils.read(fc, 8, "double_reverse");
                    // 最小块各方向上的数量
                    fc.position(fc.position() + 8);
                    this.minBlockNums = new long[3];
                    this.minBlockNums[0] = (long) RWUtils.read(fc, 8, "long_reverse");
                    this.minBlockNums[1] = (long) RWUtils.read(fc, 8, "long_reverse");
                    this.minBlockNums[2] = (long) RWUtils.read(fc, 8, "long_reverse");
                    // 右下角坐标
                    this.rightBottomCor = new double[3];
                    this.rightBottomCor[0] = this.leftTopCor[0] + this.minBlockSize[0] * this.minBlockNums[0];
                    this.rightBottomCor[1] = this.leftTopCor[1] + this.minBlockSize[1] * this.minBlockNums[1];
                    this.rightBottomCor[2] = this.leftTopCor[2] + this.minBlockSize[2] * this.minBlockNums[2];
                    // 最大尺度层级
                    this.Lmax = i;
                    // 层级偏移量
                    Pattern pattern = Pattern.compile("\\\\(\\d+)-");
                    Matcher matcher = pattern.matcher(paths.get(i));
                    if (matcher.find()) {
                        int number = Integer.parseInt(matcher.group(1));
                        double power = Math.log(number) / Math.log(2);
                        int roundedPower = (int) Math.round(power);
                        this.LOffset = roundedPower - i;
                    } else {
                        System.out.println("no match");
                    }
                    this.fc = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.blockNum = num;
        this.validBlockNum = validNum;
    }

    /*依据WH-MSBM文件进行构造*/
    public MSDM(String path) {
        try {
            File file = new File(path);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel fc = raf.getChannel();
            fc.position(0);

            /*读取MSBM文件头部分*/
            // 多尺度模型名称
            this.multiScaleModelName = (String) RWUtils.read(fc, 32, "string");
            // UUID,标识
            this.uuid = UUID.fromString((String) RWUtils.read(fc, 36, "string"));
            // 模型左上角坐标
            this.leftTopCor = new double[3];
            this.leftTopCor[0] = (double) RWUtils.read(fc, 8, "double");
            this.leftTopCor[1] = (double) RWUtils.read(fc, 8, "double");
            this.leftTopCor[2] = (double) RWUtils.read(fc, 8, "double");
            // 模型右下角坐标
            this.rightBottomCor = new double[3];
            this.rightBottomCor[0] = (double) RWUtils.read(fc, 8, "double");
            this.rightBottomCor[1] = (double) RWUtils.read(fc, 8, "double");
            this.rightBottomCor[2] = (double) RWUtils.read(fc, 8, "double");
            // 模型最小块体尺寸
            this.minBlockSize = new double[3];
            this.minBlockSize[0] = (double) RWUtils.read(fc, 8, "double");
            this.minBlockSize[1] = (double) RWUtils.read(fc, 8, "double");
            this.minBlockSize[2] = (double) RWUtils.read(fc, 8, "double");
            // 最小块体各方向数量
            this.minBlockNums = new long[3];
            this.minBlockNums[0] = (long) RWUtils.read(fc, 8, "long");
            this.minBlockNums[1] = (long) RWUtils.read(fc, 8, "long");
            this.minBlockNums[2] = (long) RWUtils.read(fc, 8, "long");
            // W-Hilbert曲线与Hilbert曲线的层级偏移量
            this.LOffset = (int) RWUtils.read(fc, 4, "int");
            // 模型的维度
            this.N = (int) RWUtils.read(fc, 4, "int");
            // 模型的最大尺度层级
            this.Lmax = (int) RWUtils.read(fc, 4, "int");
            // 模型的块体总数
            this.blockNum = (long) RWUtils.read(fc, 8, "long");
            // 模型的有效块体数
            this.validBlockNum = (long) RWUtils.read(fc, 8, "long");
            // 模型的属性列表
            String propertyList = (String) RWUtils.read(fc, 512, "string");
            this.propertiesName = Arrays.stream(propertyList.split("\t"))
                    .map(String::trim) // 去掉每个属性名的前后空白
                    .filter(property -> !property.trim().isEmpty()) // 过滤掉空字符串和只包含空白的字符串
                    .collect(Collectors.toList());
            // 块体有效性
            this.blockValidity = null;
            // MSBM文件通道
            this.fc = fc;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createMSBMFile(Map<Integer, String> paths, String filePath, String fileName) {
        try {
            File file = new File(filePath + fileName);
            if (file.createNewFile()) {
                System.out.println("文件创建成功");
            } else {
                System.out.println("文件已存在，无需创建");
                return;
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel fc = raf.getChannel();
            fc.position(0);

            /*文件头部分，共计704字节*/

            // 多尺度模型名称
            fc.write(ByteBuffer.wrap(RWUtils.string2Byte(this.multiScaleModelName, 32)));
            // UUID,标识
            fc.write(ByteBuffer.wrap(RWUtils.string2Byte(this.uuid.toString(), 36)));
            // 模型左上角坐标
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.leftTopCor[0])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.leftTopCor[1])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.leftTopCor[2])));
            // 模型右下角坐标
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.rightBottomCor[0])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.rightBottomCor[1])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.rightBottomCor[2])));
            // 模型最小块体尺寸
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.minBlockSize[0])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.minBlockSize[1])));
            fc.write(ByteBuffer.wrap(RWUtils.double2Byte(this.minBlockSize[2])));
            // 最小块体各方向数量
            fc.write(ByteBuffer.wrap(RWUtils.long2Byte(minBlockNums[0])));
            fc.write(ByteBuffer.wrap(RWUtils.long2Byte(minBlockNums[1])));
            fc.write(ByteBuffer.wrap(RWUtils.long2Byte(minBlockNums[2])));
            // W-Hilbert曲线与Hilbert曲线的层级偏移量
            fc.write(ByteBuffer.wrap(RWUtils.int2Byte(this.LOffset)));
            // 模型的维度
            fc.write(ByteBuffer.wrap(RWUtils.int2Byte(this.N)));
            // 模型的最大尺度层级
            fc.write(ByteBuffer.wrap(RWUtils.int2Byte(this.Lmax)));
            // 模型的块体总数
            fc.write(ByteBuffer.wrap(RWUtils.long2Byte(this.blockNum)));
            // 模型的有效块体数
            fc.write(ByteBuffer.wrap(RWUtils.long2Byte(this.validBlockNum)));
            // 模型的属性列表
            String propertyList = "";
            for (String propertyName : this.propertiesName) {
                propertyList += propertyName + "\t";
            }
            fc.write(ByteBuffer.wrap(RWUtils.string2Byte(propertyList, 512)));

            /*有效性和属性值部分*/
            byte[] T = "T".getBytes(StandardCharsets.ISO_8859_1);
            byte[] F = "F".getBytes(StandardCharsets.ISO_8859_1);
            for (Map.Entry<Integer, String> entry : paths.entrySet()) {
                long num = (long) Math.pow(2, entry.getKey() + this.LOffset);  // 一个方向上的块体数
                long n = (long) Math.pow(num, this.N);  // 该层级总块体数
                File entryFile = new File(entry.getValue());
                RandomAccessFile entryRaf = new RandomAccessFile(entryFile, "r");
                FileChannel entryFc = entryRaf.getChannel();
                for (int i = 0; i < n; i++) {
                    long code = WHilbertUtil.getCodeById(i, this.LOffset, this.Lmax, this.N, entry.getKey());
                    long loc = WHilbertUtil.getLocByCode(code, this.Lmax, this.N);
                    boolean valid = parentIsValid(code);
                    fc.position(MSBMFileConstants.FILE_HEADER_SIZE + loc);
                    if (valid) {
                        fc.write(ByteBuffer.wrap(T));
                        for (int j = 0; j < this.propertiesName.size(); j++) {
                            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum
                                    + (loc * 4) + (j * this.blockNum * 4));
                            float property = getBlockProperty(
                                    this.propertiesName.get(j % 5), i, entryFc, entry.getKey());
                            fc.write(ByteBuffer.wrap(RWUtils.float2Byte(property)));
                        }
                    } else {
                        fc.write(ByteBuffer.wrap(F));
                        for (int j = 0; j < this.propertiesName.size(); j++) {
                            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum
                                    + (loc * 4) + (j * this.blockNum * 4));
                            fc.write(ByteBuffer.wrap(RWUtils.float2Byte(0.0f)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String fileName(Map<Integer, String> paths) {
        // 提取文件名中的数字
        Pattern pattern = Pattern.compile(".+\\\\(\\d+)-.*");
        List<String> numbers = paths.values().stream()
                .map(path -> {
                    Matcher matcher = pattern.matcher(path);
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        System.out.println("No match found for path: " + path);
                        return "";
                    }
                })
                .collect(Collectors.toList());

        // 拼接成最终文件名
        String finalFileName = "W-" + java.lang.String.join("-", numbers) + ".a3d";
        return finalFileName;
    }

    public boolean parentIsValid(long code) {
        int level = WHilbertUtil.getLevel(this.Lmax, code, this.N);
        long id = WHilbertUtil.getIdByCode(code, this.LOffset, this.Lmax, this.N);
        boolean valid = this.blockValidity.get(level).get((int) id);
        if (valid) {
            if (level == 0) return true;
            long parentCode = WHilbertUtil.getParent(Lmax, code, level - 1, this.N);
            return parentIsValid(parentCode);
        } else {
            return false;
        }
    }

    public float getBlockProperty(String propertyName, long id, FileChannel fc, int level) {
        long num = (long) Math.pow(2, level + this.LOffset);  // 一个方向上的块体数
        long n = (long) Math.pow(num, this.N);  // 该层级总块体数
        long index = this.propertiesName.indexOf(propertyName);  // 属性的索引值
        try {
            fc.position(A3DFileConstants.FIRST_PROPERTY_POSITION + n * (index * 4 + 1) + 4 * id);
            return (float) RWUtils.read(fc, 4, "float_reverse");
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static Map<String, Float> blockPropertyQuery(FileChannel fc, List<String> propertiesName,
                                                        long num, long loc) throws IOException {
        Map<String, Float> res = new HashMap<>();
        for (int i = 0; i < propertiesName.size(); i++) {
            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + num + (loc * 4) + (i * num * 4));
            float p = (float) RWUtils.read(fc, 4, "float");
            res.put(propertiesName.get(i), p);
        }
        return res;
    }

    public SpaceQueryResultVO spaceQuery(SpaceQueryVO spaceQueryVO) throws IOException {
        // 初始化结果列表
        SpaceQueryResultVO spaceQueryResultVO = new SpaceQueryResultVO(new ArrayList<>(), new DiskIOUtil());
        for (int i = 0; i <= this.Lmax; i++) {
            spaceQueryResultVO.getResult().add(0);
        }
        // 得到满足查询要求的编码集
        Map<Long, Integer> codes = new TreeMap<>();  // Integer用于记录层级
        for (int i = 0; i <= this.Lmax; i++) {
            Coordinate min = new Coordinate(0, 0, 0);
            Coordinate max = new Coordinate(0, 0, 0);
            int difference = (1 << (this.Lmax - i));
            min.setX(spaceQueryVO.getBox().getCoordinateMin().getX() / difference);
            min.setY(spaceQueryVO.getBox().getCoordinateMin().getY() / difference);
            min.setZ(spaceQueryVO.getBox().getCoordinateMin().getZ() / difference);
            max.setX(spaceQueryVO.getBox().getCoordinateMax().getX() / difference);
            max.setY(spaceQueryVO.getBox().getCoordinateMax().getY() / difference);
            max.setZ(spaceQueryVO.getBox().getCoordinateMax().getZ() / difference);
            for (int j = min.getZ(); j <= max.getZ(); j++) {
                for (int k = min.getY(); k <= max.getY(); k++) {
                    for (int l = min.getX(); l <= max.getX(); l++) {
                        long code = WHilbertUtil.encode(this.Lmax, i, this.N, new long[]{l, k, j}, this.LOffset);
                        codes.put(code, i);
                    }
                }
            }
        }
        // 按顺序遍历满足空间查询要求的编码集
        for (Map.Entry<Long, Integer> entry : codes.entrySet()) {
            long loc = WHilbertUtil.getLocByCode(entry.getKey(), this.Lmax, this.N);
            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + loc);
            spaceQueryResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + loc) / 4096);
            if ((char) RWUtils.read(fc, 1, "char") == 'T') {
                Map<String, Float> property = new HashMap<>();
                for (int i = 0; i < this.propertiesName.size(); i++) {
                    fc.position(MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum +
                            loc * 4 + i * this.blockNum * 4);
                    spaceQueryResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE +
                            this.blockNum + loc * 4 + i * this.blockNum * 4) / 4096);
                    float pValue = (float) RWUtils.read(fc, 4, "float");
                    property.put(this.propertiesName.get(i), pValue);
                }
                spaceQueryResultVO.getResult().set(entry.getValue(), spaceQueryResultVO.getResult().get(entry.getValue()) + 1);
            }
        }
        return spaceQueryResultVO;
    }

    public ResultVO propertyQuery(SinglePropertyQueryVO singlePropertyQueryVO) throws IOException {
        ResultVO resultVO = new ResultVO(new ArrayList<>(), new DiskIOUtil());
        for (int i = 0; i <= this.Lmax; i++) {
            List<Long> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }
        int propertyIndex = this.propertiesName.indexOf(singlePropertyQueryVO.getPropertyName());
        for (int i = 0; i < this.blockNum; i++) {
            this.fc.position(MSBMFileConstants.FILE_HEADER_SIZE + i);
            resultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + i) / 4096);
            if (String.valueOf(RWUtils.read(this.fc, 1, "char")).equals("T")) {
                this.fc.position(MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum + (i * 4L) + (propertyIndex * this.blockNum * 4L));
                resultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum + (i * 4L) + (propertyIndex * this.blockNum * 4L)) / 4096);
                float p = (float) ProcessUtil.read(this.fc, 4, "float1");
                for (PropertyRange propertyRange : singlePropertyQueryVO.getPropertyRanges()) {
                    if (propertyRange.contain(p)) {
                        //满足查询条件,将本尺度的结点放入结果
                        Value value = WHilbertUtil.getCodeAndLevelById(i, 0);
                        resultVO.getResult().get(value.getLevel()).add(value.getCode());
                        break;
                    }
                }
            }
        }
        return resultVO;
    }

    public ResultVO mixedQuery(MixedQueryVO mixedQueryVO) throws IOException {
        // 初始化结果列表
        ResultVO resultVO = new ResultVO(new ArrayList<>(), new DiskIOUtil());
        for (int i = 0; i <= this.Lmax; i++) {
            List<Long> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }

        // 计算各尺度的空间坐标范围
        List<Coordinate> minList = new ArrayList<>();
        List<Coordinate> maxList = new ArrayList<>();
        Box box = mixedQueryVO.getSpaceQueryVO().getBox();
        int num = 0;
        for (int i = 0; i <= this.Lmax; i++) {
            Coordinate min = new Coordinate();
            Coordinate max = new Coordinate();
            int difference = (1 << (this.Lmax - i));
            min.setX(box.getCoordinateMin().getX() / difference);
            min.setY(box.getCoordinateMin().getY() / difference);
            min.setZ(box.getCoordinateMin().getZ() / difference);
            max.setX(box.getCoordinateMax().getX() / difference);
            max.setY(box.getCoordinateMax().getY() / difference);
            max.setZ(box.getCoordinateMax().getZ() / difference);
            minList.add(min);
            maxList.add(max);
            num += (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
        }
        // 得到满足查询要求的编码集
        long[] codes = new long[num];
        int index = 0;
        for (int i = 0; i <= this.Lmax; i++) {
            for (int j = minList.get(i).getZ(); j <= maxList.get(i).getZ(); j++) {
                for (int k = minList.get(i).getY(); k <= maxList.get(i).getY(); k++) {
                    for (int l = minList.get(i).getX(); l <= maxList.get(i).getX(); l++) {
                        long code = WHilbertUtil.encode(this.Lmax, i, this.N, new long[]{l, k, j}, this.LOffset);
                        codes[index] = code;
                        index++;
                    }
                }
            }
        }
        // 对编码集排序，方便读取磁盘数据
        Arrays.sort(codes);

        int propertyIndex = this.propertiesName.indexOf(mixedQueryVO.getSinglePropertyQueryVO().getPropertyName());
        for (long code : codes) {
            long loc = WHilbertUtil.getLocByCode(code, this.Lmax, this.N);
            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + loc);
            resultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + loc) / 4096);
            if (String.valueOf(RWUtils.read(fc, 1, "char")).equals("T")) {
                fc.position(MSBMFileConstants.FILE_HEADER_SIZE + blockNum + (loc * 4) + (propertyIndex * blockNum * 4));
                resultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + blockNum + (loc * 4) + (propertyIndex * blockNum * 4)) / 4096);
                float p = (float) RWUtils.read(fc, 4, "float");
                for (PropertyRange propertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()) {
                    if (propertyRange.contain(p)) {
                        //满足查询条件,将本尺度的结点放入结果
                        int level = WHilbertUtil.getLevel(this.Lmax, code, this.N);
                        resultVO.getResult().get(level).add(code);
                        break;
                    }
                }
            }
        }
        fc.close();
        return resultVO;
    }

    public ParentBlockResultVO parentBlockQuery(long WHCode) throws IOException {
        int level = WHilbertUtil.getLevel(this.Lmax, WHCode, this.N);
        // 最高层级没有父块
        if (level == 0) {
            return null;
        }
        ParentBlockResultVO parentBlockResultVO = new ParentBlockResultVO();
        long id = WHilbertUtil.getLocByCode(WHCode, this.Lmax, this.N);
        fc.position(MSBMFileConstants.FILE_HEADER_SIZE + id);
        parentBlockResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + id) / 4096);
        if ((char) RWUtils.read(fc, 1, "char") == 'F') {
            // 当前块体无效
            return null;
        }

        // 检查父块是否有效
        long parentCode = WHilbertUtil.getParent(this.Lmax, WHCode, level - 1, this.N);
        long pId = WHilbertUtil.getLocByCode(parentCode, this.Lmax, this.N);
        fc.position(MSBMFileConstants.FILE_HEADER_SIZE + pId);
        parentBlockResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + pId) / 4096);
        if ((char) RWUtils.read(fc, 1, "char") == 'T') {
            // 有效则读取父块数据
            List<String> propertysName = this.propertiesName;
            for (int i = 0; i < propertysName.size(); i++) {
                fc.position(MSBMFileConstants.FILE_HEADER_SIZE + this.blockNum + pId * 4 + i * this.blockNum * 4);
                parentBlockResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE +
                        this.blockNum + pId * 4 + i * this.blockNum * 4) / 4096);
                parentBlockResultVO.getResult().put(propertysName.get(i), (float) RWUtils.read(fc, 4, "float"));
            }
            return parentBlockResultVO;
        }
        return null;
    }

    public ChildrenBlockResultVO childrenBlockQuery(long WHCode) throws IOException {
        ChildrenBlockResultVO childrenBlockResultVO = new ChildrenBlockResultVO();
        int level = WHilbertUtil.getLevel(this.Lmax, WHCode, this.N);
        // 最低层没有子块
        if (level == this.Lmax) {
            return childrenBlockResultVO;
        }
        // 计算子码以及其对应位置
        long[] childrenCode = WHilbertUtil.getChildren(this.Lmax, WHCode, level + 1, this.N);
        long[] ids = new long[childrenCode.length];
        for (int i = 0; i < childrenCode.length; i++) {
            ids[i] = WHilbertUtil.getLocByCode(childrenCode[i], this.Lmax, this.N);
        }

        List<String> propertysName = this.propertiesName;
        for (int i = 0; i < ids.length; i++) {
            fc.position(MSBMFileConstants.FILE_HEADER_SIZE + ids[i]);
            childrenBlockResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE + ids[i]) / 4096);
            if ((char) RWUtils.read(fc, 1, "char") == 'T') {
                Map<String, Float> result = new HashMap<>();
                for (int j = 0; j < propertysName.size(); j++) {
                    fc.position(MSBMFileConstants.FILE_HEADER_SIZE +
                            this.blockNum + ids[i] * 4 + j * this.blockNum * 4);
                    childrenBlockResultVO.getDiskIOUtil().insertData((MSBMFileConstants.FILE_HEADER_SIZE +
                            this.blockNum + ids[i] * 4 + j * this.blockNum * 4) / 4096);
                    result.put(propertysName.get(j), (float) RWUtils.read(fc, 4, "float"));
                }
                childrenBlockResultVO.getResult().add(result);
            }
        }
        return childrenBlockResultVO;
    }
}
