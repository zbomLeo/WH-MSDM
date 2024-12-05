package com.experiment_4_3.experiment_4_3_3;

import com.entity.VO.GeoHashResultVO;
import com.entity.VO.MixedQueryVO;
import com.entity.VO.ResultVO;
import com.entity.constants.FilePathConstants;
import com.entity.utils.MethodUseRecorder;
import com.entity.utils.StatisticsUtil;
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

public class mixedTest {
    @Test
    public void Test_3() throws IOException {
        List<Integer> childrenBlockNums = new ArrayList<>();
        childrenBlockNums.add(4);
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
        paths.put(2, FilePathConstants.A3D_FILE_PATH + "128-3Pb-Ts-Ag-fault-stratum.a3d");
        ProcessUtil.Lmax = paths.size() - 1;


        //混合查询循环测试
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            MixedQueryVO mixedQueryVO = StatisticsUtil.randomMixedQueryVO(128);
            statisticsUtil.getMixedQueryVOS().add(mixedQueryVO);
            System.out.println("第" + i + "次查询");



            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64-128.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.mixedQuery(mixedQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResult().get(0).size() + WResult.getResult().get(1).size()
                    + WResult.getResult().get(2).size());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.multiScaleMixedQuery(mixedQueryVO,"Octree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResult().get(0).size() + OctreeResult.getResult().get(1).size()
                    + OctreeResult.getResult().get(2).size());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.multiScaleMixedQuery(mixedQueryVO, "VdbTree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResult().get(0).size() + VdbResult.getResult().get(1).size()
                    + VdbResult.getResult().get(2).size());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO geoHashResult = GeoHashQuery.geoHashMixedQuery(mixedQueryVO, "GeoHash-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResult().get(0).size() + geoHashResult.getResult().get(1).size()
                    + geoHashResult.getResult().get(2).size());
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("MixedQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_3\\", recorder, "3-");
    }

    @Test
    public void Test_2() throws IOException {
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


        //混合查询循环测试
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
//            MixedQueryVO mixedQueryVO = StatisticsUtil.randomMixedQueryVO(128);
            MixedQueryVO mixedQueryVO = MixedQueryVO.getQueryVOByExcel(
                    FilePathConstants.RESULT_PATH
                            + "experiment_4_3\\experiment_4_3_3\\3-mixedQueryStatistics.xlsx", i, 1.0f
            );
            statisticsUtil.getMixedQueryVOS().add(mixedQueryVO);
            System.out.println("第" + i + "次查询");



            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-64-128.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.mixedQuery(mixedQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResult().get(0).size() + WResult.getResult().get(1).size());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.multiScaleMixedQuery(mixedQueryVO,"Octree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResult().get(0).size() + OctreeResult.getResult().get(1).size());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.multiScaleMixedQuery(mixedQueryVO, "VdbTree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResult().get(0).size() + VdbResult.getResult().get(1).size());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO geoHashResult = GeoHashQuery.geoHashMixedQuery(mixedQueryVO, "GeoHash-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResult().get(0).size() + geoHashResult.getResult().get(1).size());
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("MixedQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_3\\", recorder, "2-");
    }
}
