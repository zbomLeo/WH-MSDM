package com.experiment_4_3.experiment_4_3_2;

import com.entity.constants.FilePathConstants;
import com.geohash.GeoHash;
import com.octree.Octree;
import com.process.util.ProcessUtil;
import com.vdbTree.BranchFactor;
import com.vdbTree.VdbTree;
import com.whmsdm.MSDM;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class createFileTest {

    @Test
    public void Test_32_64() throws IOException {
        List<Integer> childrenBlockNums = new ArrayList<>();
//        childrenBlockNums.add(4);
        childrenBlockNums.add(2);
        childrenBlockNums.add(1);
        ProcessUtil.childrenBlockNums = childrenBlockNums;

        List<String> propertiesName = new ArrayList<>();
        propertiesName.add("地层");
        propertiesName.add("断裂");
        propertiesName.add("Pb");
        propertiesName.add("TS");
        propertiesName.add("Ag");
        ProcessUtil.propertiesName = propertiesName;

        //添加文件路径
        Map<Integer, String> paths = new HashMap<>();
        paths.put(0, FilePathConstants.A3D_FILE_PATH + "32-3Pb-Ts-Ag-fault-stratum.a3d");
        paths.put(1, FilePathConstants.A3D_FILE_PATH + "64-3Pb-Ts-Ag-fault-stratum.a3d");
//        paths.put(2, FilePathConstants.A3D_FILE_PATH + "128-3Pb-Ts-Ag-fault-stratum.a3d");
        ProcessUtil.Lmax = paths.size() - 1;

        ProcessUtil.num = 32 * 32 * 32 + 64 * 64 * 64;

        ProcessUtil.blockNums = new int[]{32, 64};

        long startTime = 0;
        long endTime = 0;

        //WH-MSBM
        startTime = System.currentTimeMillis();
        MSDM MSDM = new MSDM(paths, 3);
        MSDM.createMSBMFile(paths, FilePathConstants.FILE_PATH, MSDM.fileName(paths));
        endTime = System.currentTimeMillis();
        System.out.println("MSBM:" + (endTime - startTime) + "ms");


        //geoHash
        startTime = System.currentTimeMillis();
        GeoHash.createGeoHashFile(paths, "GeoHash-32-64.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("geoHash:" + (endTime - startTime) + "ms");

        //vdb树
        List<BranchFactor> branchFactors = new ArrayList<>();
        BranchFactor branchFactor0 = new BranchFactor(5, 5, 5);
        BranchFactor branchFactor1 = new BranchFactor(1, 1, 1);
//        BranchFactor branchFactor2 = new BranchFactor(1, 1, 1);
        branchFactors.add(branchFactor0);
        branchFactors.add(branchFactor1);
//        branchFactors.add(branchFactor2);
        startTime = System.currentTimeMillis();
        VdbTree vdbTree = new VdbTree(paths,branchFactors);
        vdbTree.createVdbTreeMultiScaleFile("VdbTree-32-64.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("vdb:" + (endTime - startTime) + "ms");

        //octree
        startTime = System.currentTimeMillis();
        Octree octree = new Octree(paths,7);
        octree.createOctreeMultiScaleFile("Octree-32-64.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("octree:" + (endTime - startTime) + "ms");
    }


    @Test
    public void Test_64_128() throws IOException{
        List<Integer> childrenBlockNums = new ArrayList<>();
//        childrenBlockNums.add(4);
        childrenBlockNums.add(2);
        childrenBlockNums.add(1);
        ProcessUtil.childrenBlockNums = childrenBlockNums;

        List<String> propertiesName = new ArrayList<>();
        propertiesName.add("地层");
        propertiesName.add("断裂");
        propertiesName.add("Pb");
        propertiesName.add("TS");
        propertiesName.add("Ag");
        ProcessUtil.propertiesName = propertiesName;

        //添加文件路径
        Map<Integer, String> paths = new HashMap<>();
//        paths.put(0, FilePathConstants.A3D_FILE_PATH + "32-3Pb-Ts-Ag-fault-stratum.a3d");
        paths.put(0, FilePathConstants.A3D_FILE_PATH + "64-3Pb-Ts-Ag-fault-stratum.a3d");
        paths.put(1, FilePathConstants.A3D_FILE_PATH + "128-3Pb-Ts-Ag-fault-stratum.a3d");
        ProcessUtil.Lmax = paths.size() - 1;

        ProcessUtil.num = 64 * 64 * 64 + 128 * 128 * 128;

        ProcessUtil.blockNums = new int[]{64, 128};

        ProcessUtil.LOffset = 6;

        long startTime = 0;
        long endTime = 0;

        //WH-MSBM
        startTime = System.currentTimeMillis();
        MSDM MSDM = new MSDM(paths, 3);
        MSDM.createMSBMFile(paths, FilePathConstants.FILE_PATH, MSDM.fileName(paths));
        endTime = System.currentTimeMillis();
        System.out.println("MSBM:" + (endTime - startTime) + "ms");


        //geoHash
        startTime = System.currentTimeMillis();
        GeoHash.createGeoHashFile(paths, "GeoHash-64-128.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("geoHash:" + (endTime - startTime) + "ms");

        //vdb树
        List<BranchFactor> branchFactors = new ArrayList<>();
        BranchFactor branchFactor0 = new BranchFactor(6, 6, 6);
        BranchFactor branchFactor1 = new BranchFactor(1, 1, 1);
//        BranchFactor branchFactor2 = new BranchFactor(1, 1, 1);
        branchFactors.add(branchFactor0);
        branchFactors.add(branchFactor1);
//        branchFactors.add(branchFactor2);
        startTime = System.currentTimeMillis();
        VdbTree vdbTree = new VdbTree(paths,branchFactors);
        vdbTree.createVdbTreeMultiScaleFile("VdbTree-64-128.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("vdb:" + (endTime - startTime) + "ms");

        //octree
        startTime = System.currentTimeMillis();
        Octree octree = new Octree(paths,8);
        octree.createOctreeMultiScaleFile("Octree-64-128.a3d");
        endTime = System.currentTimeMillis();
        System.out.println("octree:" + (endTime - startTime) + "ms");
    }
}
