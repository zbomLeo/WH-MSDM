package com.whmsdm.experiment_4_4;

import com.whmsdm.entity.VO.SpaceQueryResultVO;
import com.whmsdm.entity.VO.SpaceQueryVO;
import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.entity.utils.MethodUseRecorder;
import com.whmsdm.entity.utils.StatisticsUtil;
import com.whmsdm.geohash.GeoHashQuery;
import com.whmsdm.process.util.ProcessUtil;
import com.whmsdm.vdbTree.VdbTree;
import com.whmsdm.whmsdm.MSDM;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class spaceTest {
    @Test
    public void test() throws IOException {
        ProcessUtil.setConfig(32, 512, 5);


        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 100; i++) {
            SpaceQueryVO spaceQueryVO = StatisticsUtil.randomSpaceQueryVO(512);
            statisticsUtil.getSpaceQueryVOS().add(spaceQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64-128-256-512.a3d");
            long startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO1 = MSDM.spaceQuery(spaceQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(spaceQueryResultVO1.getResultNum());
            statisticsUtil.getWIOs().add(spaceQueryResultVO1.getDiskIOUtil().getDataCount());


//            //octree
//            startTime = System.currentTimeMillis();
//            SpaceQueryResultVO spaceQueryResultVO2 = Octree.multiScaleSpaceQuery3(spaceQueryVO, "Octree-32-64-128-256-512.a3d");
//            endTime = System.currentTimeMillis();
//            statisticsUtil.getOctreeTimes().add((endTime - startTime));
//            statisticsUtil.getOctreeResultSize().add(spaceQueryResultVO2.getResultNum());
//            statisticsUtil.getOctreeIOs().add(spaceQueryResultVO2.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO spaceQueryResultVO3 = VdbTree.multiScaleSpaceQuery3(spaceQueryVO, "VdbTree-32-64-128-256-512.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(spaceQueryResultVO3.getResultNum());
            statisticsUtil.getVdbIOs().add(spaceQueryResultVO3.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            SpaceQueryResultVO geoHashResult = GeoHashQuery.geoHashSpaceQuery(spaceQueryVO, "GeoHash-32-64-128-256-512.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(geoHashResult.getResultNum());
            statisticsUtil.getGeoHashIOs().add(geoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true, false, true, true);
        statisticsUtil.writeExcel("SpaceQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_4\\", recorder, "");
    }
}
