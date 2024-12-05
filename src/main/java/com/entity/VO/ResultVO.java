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
public class ResultVO {
    private List<List<Long>> result;
    private DiskIOUtil MSBMDiskIOUtil;
    private DiskIOUtil UVDBDiskIOUtil;
    private DiskIOUtil diskIOUtil;

    public ResultVO(List<List<Long>> result, DiskIOUtil diskIOUtil) {
        this.result = result;
        this.MSBMDiskIOUtil = null;
        this.UVDBDiskIOUtil = null;
        this.diskIOUtil = diskIOUtil;
    }

    public ResultVO(List<List<Long>> result, DiskIOUtil MSBMDiskIOUtil, DiskIOUtil UVDBDiskIOUtil) {
        this.result = result;
        this.MSBMDiskIOUtil = MSBMDiskIOUtil;
        this.UVDBDiskIOUtil = UVDBDiskIOUtil;
        this.diskIOUtil = null;
    }
}
