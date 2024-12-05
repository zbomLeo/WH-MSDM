package com.entity.VO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.entity.utils.DiskIOUtil;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpaceQueryResultVO {
    private List<Integer> result;
    private DiskIOUtil diskIOUtil;
}
