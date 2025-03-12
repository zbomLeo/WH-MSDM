package com.whmsdm.vdbTree;


import com.whmsdm.entity.property.PropertyMapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MultiScaleBlockModelProperty {
    private UUID uuid;
    private Map<Integer, BlockModelProperty> BlockModelPropertyMap;   // 多尺度模型
    private List<String> propertiesName;    // 属性列表
    private PropertyMapping propertyMapping;    // 属性code映射
    private String multiScaleModelName; // 模型名
    private double[] leftTopCor;   // 模型左上坐标
    private double[] rightBottomCor;   // 模型右下角坐标
    private double[] minBlockSize;  // 最小块体尺寸
    private long[] minBlockNums;    // 最小块体各方向数量
    private int Lmax;   // 模型尺度（最大层级）
    private long totalMultiScaleBlockCount;   // 有效块体总数
    private long dataOffset;    // 真实数据偏移量
    private RandomAccessFile raf;
    private FileChannel fc;
}
