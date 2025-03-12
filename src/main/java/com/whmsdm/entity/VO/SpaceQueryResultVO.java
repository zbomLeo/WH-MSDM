package com.whmsdm.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.whmsdm.entity.utils.DiskIOUtil;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpaceQueryResultVO {
    private List<Integer> result;
    private DiskIOUtil diskIOUtil;

    public int getResultNum(){
        int num = 0;
        for (Integer integer : result) {
            num += integer;
        }
        return num;
    }
}
