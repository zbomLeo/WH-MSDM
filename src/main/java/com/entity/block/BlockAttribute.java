package com.entity.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BlockAttribute {
    private long WHCode;  // W-Hilbert编码
    private String propertyName;  // 属性名
    private float propertyValue;  // 属性值
}
