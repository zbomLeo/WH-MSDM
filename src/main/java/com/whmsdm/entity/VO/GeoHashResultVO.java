package com.whmsdm.entity.VO;

import com.whmsdm.entity.utils.DiskIOUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GeoHashResultVO {
    private List<List<String>> result;
    private DiskIOUtil diskIOUtil;

    public int getResultNum(){
        int num = 0;
        for (List<String> list : result) {
            num += list.size();
        }
        return num;
    }
}
