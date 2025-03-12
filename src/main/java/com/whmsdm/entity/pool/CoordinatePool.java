package com.whmsdm.entity.pool;

import java.util.LinkedList;
import com.whmsdm.entity.coor.Coordinate;

public class CoordinatePool {
    private final LinkedList<Coordinate> pool;

    public CoordinatePool() {
        this.pool = new LinkedList<>();
    }

    // 从池中获取一个Coordinate对象
    public Coordinate acquire() {
        if (!pool.isEmpty()) {
            return pool.removeLast(); // 从池中取出最后一个对象
        }
        return new Coordinate(); // 池中没有可用对象时，创建新的对象
    }

    // 将Coordinate对象放回池中
    public void release(Coordinate coordinate) {
        if (coordinate != null) {
            pool.addLast(coordinate); // 将对象添加到池的末尾
        }
    }
}
