package com.experiment_4_3.experiment_4_3_2;

import com.entity.VO.SpaceQueryResultVO;
import com.entity.VO.SpaceQueryVO;
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

public class spaceTest {

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

        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            SpaceQueryVO spaceQueryVO = StatisticsUtil.randomSpaceQueryVO(64);
            statisticsUtil.getSpaceQueryVOS().add(spaceQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64.a3d");
            long startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO1 = MSDM.spaceQuery(spaceQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(spaceQueryResultVO1.getResult().get(0) + spaceQueryResultVO1.getResult().get(1));
            statisticsUtil.getWIOs().add(spaceQueryResultVO1.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO2 = Octree.multiScaleSpaceQuery3(spaceQueryVO, "Octree-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(spaceQueryResultVO2.getResult().get(0) + spaceQueryResultVO2.getResult().get(1));
            statisticsUtil.getOctreeIOs().add(spaceQueryResultVO2.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO3 = VdbTree.multiScaleSpaceQuery3(spaceQueryVO, "VdbTree-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(spaceQueryResultVO3.getResult().get(0) + spaceQueryResultVO3.getResult().get(1));
            statisticsUtil.getVdbIOs().add(spaceQueryResultVO3.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO geoHashResult = GeoHashQuery.geoHashSpaceQuery(spaceQueryVO, "GeoHash-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResult().get(0) + geoHashResult.getResult().get(1));
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("SpaceQuery", FilePathConstants.RESULT_PATH
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


        //空间查询循环测试，输出结果为满足要求的块体数目
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            SpaceQueryVO spaceQueryVO = SpaceQueryVO.getQueryVOByExcel(FilePathConstants.RESULT_PATH
                    + "experiment_4_3\\experiment_4_3_2\\32-spaceQueryStatistics.xlsx", i, 2.0f);
            statisticsUtil.getSpaceQueryVOS().add(spaceQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-64-128.a3d");
            long startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO1 = MSDM.spaceQuery(spaceQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(spaceQueryResultVO1.getResult().get(0) + spaceQueryResultVO1.getResult().get(1));
            statisticsUtil.getWIOs().add(spaceQueryResultVO1.getDiskIOUtil().getDataCount());

            //octree
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO2 = Octree.multiScaleSpaceQuery3(spaceQueryVO, "Octree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(spaceQueryResultVO2.getResult().get(0) + spaceQueryResultVO2.getResult().get(1));
            statisticsUtil.getOctreeIOs().add(spaceQueryResultVO2.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO3 = VdbTree.multiScaleSpaceQuery3(spaceQueryVO, "VdbTree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(spaceQueryResultVO3.getResult().get(0) + spaceQueryResultVO3.getResult().get(1));
            statisticsUtil.getVdbIOs().add(spaceQueryResultVO3.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO geoHashResult = GeoHashQuery.geoHashSpaceQuery(spaceQueryVO, "GeoHash-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResult().get(0) + geoHashResult.getResult().get(1));
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());

        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("SpaceQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_2\\", recorder, "64-");
    }
}
