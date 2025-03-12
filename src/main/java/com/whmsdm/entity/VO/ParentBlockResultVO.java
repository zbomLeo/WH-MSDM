package com.whmsdm.entity.VO;

import com.whmsdm.entity.utils.DiskIOUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


import java.util.HashMap;
import java.util.Map;
@AllArgsConstructor
@Getter
@Setter
public class ParentBlockResultVO {
    private Map<String, Float> result;
    private DiskIOUtil diskIOUtil;

    public ParentBlockResultVO(){
        result = new HashMap<>();
        diskIOUtil = new DiskIOUtil();
    }
}
