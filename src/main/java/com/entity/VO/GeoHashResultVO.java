package com.entity.VO;

import com.entity.utils.DiskIOUtil;
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
}
