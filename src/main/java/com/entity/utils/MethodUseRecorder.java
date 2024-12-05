package com.entity.utils;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MethodUseRecorder {
    public Boolean w;
    public Boolean octree;
    public Boolean vdb;
    public Boolean geoHash;
    public List<String> methodNames;
    public static final int num = 4;

    public MethodUseRecorder(Boolean W, Boolean octree, Boolean vdb, Boolean geoHash) {
        this.w = W;
        this.octree = octree;
        this.vdb = vdb;
        this.geoHash = geoHash;
        methodNames = new ArrayList<>();
        if(W) methodNames.add("WH-MSDM");
        if(octree) methodNames.add("Octree");
        if(vdb) methodNames.add("VDB");
        if(geoHash) methodNames.add("GeoHash");
    }
}
