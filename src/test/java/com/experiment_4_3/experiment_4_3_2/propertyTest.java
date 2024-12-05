package com.experiment_4_3.experiment_4_3_2;

import com.entity.VO.GeoHashResultVO;
import com.entity.VO.ResultVO;
import com.entity.VO.SinglePropertyQueryVO;
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

public class propertyTest {
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

        // 单种属性查询循环测试__vdbW
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            SinglePropertyQueryVO singlePropertyQueryVO = StatisticsUtil.randomStratumPropertyQueryVO();
            statisticsUtil.getSinglePropertyQueryVOS().add(singlePropertyQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.propertyQuery(singlePropertyQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResult().get(0).size() + WResult.getResult().get(1).size());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.OctreeMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"Octree-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResult().get(0).size() + OctreeResult.getResult().get(1).size());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.vdbMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"VdbTree-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResult().get(0).size() + VdbResult.getResult().get(1).size());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO GeoHashResult = GeoHashQuery.geoHashSinglePropertyQuery(singlePropertyQueryVO,"GeoHash-32-64.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(GeoHashResult.getResult().get(0).size() + GeoHashResult.getResult().get(1).size());

            statisticsUtil.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("SinglePropertyQuery", FilePathConstants.RESULT_PATH
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

        // 单种属性查询循环测试__vdbW
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
//            SinglePropertyQueryVO singlePropertyQueryVO = StatisticsUtil.randomStratumPropertyQueryVO();
            SinglePropertyQueryVO singlePropertyQueryVO = SinglePropertyQueryVO.getQueryVOByExcel(
                    FilePathConstants.RESULT_PATH
                            + "experiment_4_3\\experiment_4_3_2\\32-singlePropertyQueryStatistics.xlsx", i
            );
            statisticsUtil.getSinglePropertyQueryVOS().add(singlePropertyQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-64-128.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.propertyQuery(singlePropertyQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResult().get(0).size() + WResult.getResult().get(1).size());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.OctreeMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"Octree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResult().get(0).size() + OctreeResult.getResult().get(1).size());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.vdbMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"VdbTree-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResult().get(0).size() + VdbResult.getResult().get(1).size());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO GeoHashResult = GeoHashQuery.geoHashSinglePropertyQuery(singlePropertyQueryVO,"GeoHash-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(GeoHashResult.getResult().get(0).size() + GeoHashResult.getResult().get(1).size());

            statisticsUtil.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("SinglePropertyQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_2\\", recorder, "64-");
    }
}
