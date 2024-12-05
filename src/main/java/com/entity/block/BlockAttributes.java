package com.entity.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BlockAttributes {
    private long WHCode;
    private Map<String, Float> propertiesValue;
}
