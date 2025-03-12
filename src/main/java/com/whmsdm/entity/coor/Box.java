package com.whmsdm.entity.coor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Box {
    private Coordinate coordinateMin;
    private Coordinate coordinateMax;

    public boolean intersect(Box box) {
        // 判断两个Box是否相交
        return coordinateMin.getX() <= box.getCoordinateMax().getX() &&
                coordinateMax.getX() >= box.getCoordinateMin().getX() &&
                coordinateMin.getY() <= box.getCoordinateMax().getY() &&
                coordinateMax.getY() >= box.getCoordinateMin().getY() &&
                coordinateMin.getZ() <= box.getCoordinateMax().getZ() &&
                coordinateMax.getZ() >= box.getCoordinateMin().getZ();
    }

    public boolean contain(Coordinate coordinate) {
        // 判断一个坐标是否在Box内
        return coordinate.getX() >= coordinateMin.getX() &&
                coordinate.getX() <= coordinateMax.getX() &&
                coordinate.getY() >= coordinateMin.getY() &&
                coordinate.getY() <= coordinateMax.getY() &&
                coordinate.getZ() >= coordinateMin.getZ() &&
                coordinate.getZ() <= coordinateMax.getZ();
    }

    public boolean contain(Box box) {
        // 判断一个Box是否在Box内
        return contain(box.getCoordinateMin()) && contain(box.getCoordinateMax());
    }

    public String toString() {
        return "Box{" +
                "coordinateMin=" + coordinateMin.toString() +
                ", coordinateMax=" + coordinateMax.toString() +
                '}';
    }
}
