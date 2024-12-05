package com.experiment_4_3.experiment_4_3_2;

import com.entity.VO.ChildrenBlockResultVO;
import com.entity.constants.FilePathConstants;
import com.entity.utils.MethodUseRecorder;
import com.entity.utils.StatisticsUtil;
import com.geohash.GeoHashCode;
import com.geohash.GeoHashQuery;
import com.octree.Octree;
import com.process.util.ProcessUtil;
import com.vdbTree.VdbTree;
import com.whmsdm.MSDM;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class childTest {
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

        // 子块查询循环测试
        StatisticsUtil statisticsUtil2 = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            long WHCode = StatisticsUtil.randomWHCode(ProcessUtil.Lmax,3,ProcessUtil.LOffset);
            statisticsUtil2.getWHCodes().add(WHCode);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64.a3d");
            long startTime = System.nanoTime();
            ChildrenBlockResultVO result = MSDM.childrenBlockQuery(WHCode);
            long endTime = System.nanoTime();
            statisticsUtil2.getWTimes().add((endTime - startTime));
            statisticsUtil2.getWResultSize().add(result.getResult().size());
            statisticsUtil2.getWIOs().add(result.getDiskIOUtil().getDataCount());


            //octree
            startTime = System.nanoTime();
            ChildrenBlockResultVO OctreeResult = Octree.childrenBlockQuery(WHCode, "Octree-32-64.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getOctreeTimes().add((endTime - startTime));
            statisticsUtil2.getOctreeResultSize().add(OctreeResult.getResult().size());
            statisticsUtil2.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.nanoTime();
            ChildrenBlockResultVO VdbResult = VdbTree.childrenBlockQuery(WHCode, "VdbTree-32-64.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getVdbTimes().add((endTime - startTime));
            statisticsUtil2.getVdbResultSize().add(VdbResult.getResult().size());
            statisticsUtil2.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            String geoHash = GeoHashCode.WHCodeToGeoHash(WHCode, ProcessUtil.Lmax);
            startTime = System.nanoTime();
            ChildrenBlockResultVO GeoHashResult = GeoHashQuery.childrenBlockQuery(geoHash, "GeoHash-32-64.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil2.getGeoHashResultSize().add(GeoHashResult.getResult().size());
            statisticsUtil2.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());


        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil2.writeExcel("ChildBlockQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_2\\", recorder, "32-");
    }

    @Test
    public void Test_64_128() throws IOException {
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

        // 子块查询循环测试
        StatisticsUtil statisticsUtil2 = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            long WHCode = StatisticsUtil.randomWHCode(ProcessUtil.Lmax,3,ProcessUtil.LOffset);
            statisticsUtil2.getWHCodes().add(WHCode);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-64-128.a3d");
            long startTime = System.nanoTime();
            ChildrenBlockResultVO result = MSDM.childrenBlockQuery(WHCode);
            long endTime = System.nanoTime();
            statisticsUtil2.getWTimes().add((endTime - startTime));
            statisticsUtil2.getWResultSize().add(result.getResult().size());
            statisticsUtil2.getWIOs().add(result.getDiskIOUtil().getDataCount());


            //octree
            startTime = System.nanoTime();
            ChildrenBlockResultVO OctreeResult = Octree.childrenBlockQuery(WHCode, "Octree-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getOctreeTimes().add((endTime - startTime));
            statisticsUtil2.getOctreeResultSize().add(OctreeResult.getResult().size());
            statisticsUtil2.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.nanoTime();
            ChildrenBlockResultVO VdbResult = VdbTree.childrenBlockQuery(WHCode, "VdbTree-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getVdbTimes().add((endTime - startTime));
            statisticsUtil2.getVdbResultSize().add(VdbResult.getResult().size());
            statisticsUtil2.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            String geoHash = GeoHashCode.WHCodeToGeoHash(WHCode, ProcessUtil.Lmax);
            startTime = System.nanoTime();
            ChildrenBlockResultVO GeoHashResult = GeoHashQuery.childrenBlockQuery(geoHash, "GeoHash-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil2.getGeoHashResultSize().add(GeoHashResult.getResult().size());
            statisticsUtil2.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());


        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil2.writeExcel("ChildBlockQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_2\\", recorder, "64-");
    }
}
