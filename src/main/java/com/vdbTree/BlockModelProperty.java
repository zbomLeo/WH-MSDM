package com.vdbTree;

import com.WHilbertUtil;
import com.process.util.ProcessUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BlockModelProperty {
    private long propertyStartOffset;    // 属性偏移量
    private String identification;  // 坤迪标识
    private float version;  // 版本
    private Date time;  // 时间
    private String modelName;   // 模型名
    private String identifier;  // 唯一标识
    private double[] origin;    // 左上角起点坐标
    private double[] blockSize;  // 块体尺寸
    private long[] blockNums;   // 各方向块体数
    private long previousLevelModelIdentifier;  // 父模型唯一标识符
    private long previousLevelModelIndex;   // 父模型索引
    private BitSet blockValidity;   // 块体有效性
    private long validBlockCount;   // 有效块体数量
    private Map<String, Long> propertiesOffsetMap;   // 属性偏移量
    private List<String> propertiesName;    // 属性名
    private long totalBlockCount;   // 块体总数
    private int level;  // 块体尺度（层级、分辨率）
    private RandomAccessFile raf;
    private FileChannel fc;

    public Long getStartPosition(Integer integer) {
        return this.propertyStartOffset + 32 + integer * (32 + totalBlockCount * 4) ;
//        return this.propertyStartOffset + 32 + integer * (totalBlockCount * 4) ;
    }

    public float getSingleBlockPropertyValue(String propertyName, long blockCode) throws IOException {
        fc.position(propertiesOffsetMap.get(propertyName)
                + ProcessUtil.getBlockIdByCode(blockCode, blockNums[0], blockNums[1], blockNums[2]) * 4);
        return ((float) ProcessUtil.read(fc, 4, "float"));
    }

    public Map<String, Float> getSingleBlockAllPropertyValue(long code) throws IOException {
        Map<String, Float> property = new HashMap<>();
        for (String propertyName : this.propertiesName) {
            property.put(propertyName, this.getSingleBlockPropertyValue(propertyName, code));
        }
        return property;
    }

    public boolean judgeValid(long code) {
        long id = ProcessUtil.getBlockIdByCode(code, blockNums[0], blockNums[1], blockNums[2]);
        boolean valid = blockValidity.get((int) id);
        if(valid){
            return this.parentIsValid(code);
        }
        return false;
    }

    public boolean parentIsValid(long code){
        if(this.level == 0){
            //  已经是最高层次模型，没有父块
            return true;
        }
        long parentCode = WHilbertUtil.getParent(ProcessUtil.Lmax,code,this.level - 1,3);  // 获得该块的父块编码
        BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(this.level - 1);
        int id = (int) ProcessUtil.getBlockIdByCode(parentCode,blockModelProperty.getBlockNums()[0],blockModelProperty.getBlockNums()[1],blockModelProperty.getBlockNums()[2]);
        if(blockModelProperty.getBlockValidity().get(id)){
            //  父块有效
            return blockModelProperty.parentIsValid(parentCode);
        } else {
            //  父块无效
            return false;
        }
    }
}
