package com.vdbTree;

import com.entity.coor.Box;
import com.entity.coor.Coordinate;
import com.octree.Num;
import com.process.util.ProcessUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.channels.FileChannel;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class Node {
    public int level;
    public int wHilbertCode;
    public Coordinate origin;

    // 获得该结点表示的空间范围
    public Box getBox() {
        int num = ProcessUtil.vdbTree.getChildrenBlockNums().get(level);
        Coordinate coordinateMax = new Coordinate(this.origin.getX() + num - 1, this.origin.getY() + num - 1, this.origin.getZ() + num - 1);
        return new Box(this.origin, coordinateMax);
    }

    protected abstract long[] createVdbTreeMultiScaleFile(boolean childValid, long l, FileChannel fc) throws IOException;

    protected abstract void getNum(Num num);

    public abstract Node findNode(long wHCode, int level);
}
