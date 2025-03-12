package com.whmsdm.entity.VO;

import com.whmsdm.entity.utils.DiskIOUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@AllArgsConstructor
@Getter
@Setter
public class ChildrenBlockResultVO {
    private List<Map<String, Float>> result;
    private DiskIOUtil diskIOUtil;

    public ChildrenBlockResultVO(){
        result = new ArrayList<>();
        diskIOUtil = new DiskIOUtil();
    }
}
