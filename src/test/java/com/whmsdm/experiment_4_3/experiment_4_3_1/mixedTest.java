package com.whmsdm.experiment_4_3.experiment_4_3_1;

import com.whmsdm.entity.VO.GeoHashResultVO;
import com.whmsdm.entity.VO.MixedQueryVO;
import com.whmsdm.entity.VO.ResultVO;
import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.entity.utils.MethodUseRecorder;
import com.whmsdm.entity.utils.StatisticsUtil;
import com.whmsdm.geohash.GeoHashQuery;
import com.whmsdm.octree.Octree;
import com.whmsdm.process.util.ProcessUtil;
import com.whmsdm.vdbTree.VdbTree;
import com.whmsdm.whmsdm.MSDM;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mixedTest {
    @Test
    public void fiveMixedQueryTest() throws IOException {
        ProcessUtil.setConfig(32, 128, 5);


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
            statisticsUtil.getWResultSize().add(WResult.getResultNum());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.multiScaleMixedQuery(mixedQueryVO,"Octree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResultNum());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.multiScaleMixedQuery(mixedQueryVO, "VdbTree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResultNum());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO geoHashResult = GeoHashQuery.geoHashMixedQuery(mixedQueryVO, "GeoHash-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResultNum());
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("MixedQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_1\\", recorder, "5-");
    }

    @Test
    public void fourMixedQueryTest() throws IOException {
        ProcessUtil.setConfig(32, 128, 4);


        //混合查询循环测试
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
//            MixedQueryVO mixedQueryVO = StatisticsUtil.randomMixedQueryVO(128);
            MixedQueryVO mixedQueryVO = MixedQueryVO.getQueryVOByExcel(
                    FilePathConstants.RESULT_PATH
                            + "experiment_4_3\\experiment_4_3_1\\5-mixedQueryStatistics.xlsx", i, 1.0f
            );
            statisticsUtil.getMixedQueryVOS().add(mixedQueryVO);
            System.out.println("第" + i + "次查询");



            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "4-W-32-64-128.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.mixedQuery(mixedQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResultNum());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



            //octree
            startTime = System.currentTimeMillis();
            ResultVO OctreeResult = Octree.multiScaleMixedQuery(mixedQueryVO,"4-Octree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResultNum());
            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());



            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.multiScaleMixedQuery(mixedQueryVO, "4-VdbTree-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResultNum());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO geoHashResult = GeoHashQuery.geoHashMixedQuery(mixedQueryVO, "4-GeoHash-32-64-128.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResultNum());
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("MixedQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_1\\", recorder, "4-");
    }
}
