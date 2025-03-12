package com.whmsdm.entity.pool;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PropertyPool {
    private final List<Map<String, Float>> pool;

    public PropertyPool() {
        this.pool = new LinkedList<>();
    }

    public Map<String, Float> acquire() {
        // 获取并移除列表的第一个元素，如果没有则创建一个新的对象
        return pool.isEmpty() ? new HashMap<>() : pool.remove(0);
    }

    public void release(Map<String, Float> property) {
        property.clear(); // 清空对象中的内容
        pool.add(property); // 添加到列表末尾
    }
}

