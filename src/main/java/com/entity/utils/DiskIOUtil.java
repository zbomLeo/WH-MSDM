package com.entity.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class DiskIOUtil {
    private Set<Long> disks;

    public DiskIOUtil(){
        disks = new HashSet<>();
    }


    // 插入数据
    public boolean insertData(long data) {
        return disks.add(data); // 如果数据已经存在，add方法会返回false
    }

    // 获取数据的个数
    public int getDataCount() {
        return disks.size();
    }
}
