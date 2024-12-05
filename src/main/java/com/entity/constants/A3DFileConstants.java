package com.entity.constants;

/***
 * 用来记录A3D文件头各属性在文件中的位置
 * 以下位置均为该属性的属性值位置，不包括8字节的属性字段
 */
public class A3DFileConstants {
    public static final int LEFT_TOP_COR_POSITION = 116;  // 左上角坐标
    public static final int BLOCK_NUMS_POSITION = 180;  // 各方向上块体数
    public static final int VALID_POSITION = 292;  // 块体有效性
    public static final int FIRST_PROPERTY_POSITION = 844;  // 第一个属性值出现的位置，不包括有效性。

}
