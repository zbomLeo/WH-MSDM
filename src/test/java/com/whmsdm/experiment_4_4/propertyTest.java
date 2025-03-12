package com.whmsdm.experiment_4_4;

import com.whmsdm.entity.VO.GeoHashResultVO;
import com.whmsdm.entity.VO.ResultVO;
import com.whmsdm.entity.VO.SinglePropertyQueryVO;
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

public class propertyTest {
    @Test
    public void test() throws IOException {
        ProcessUtil.setConfig(32, 512, 5);


        // 单种属性查询循环测试__vdbW
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 100; i++) {
            SinglePropertyQueryVO singlePropertyQueryVO = StatisticsUtil.randomStratumPropertyQueryVO();
            statisticsUtil.getSinglePropertyQueryVOS().add(singlePropertyQueryVO);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64-128-256-512.a3d");
            long startTime = System.currentTimeMillis();
            ResultVO WResult = MSDM.propertyQuery(singlePropertyQueryVO);
            long endTime = System.currentTimeMillis();
            statisticsUtil.getWTimes().add((endTime - startTime));
            statisticsUtil.getWResultSize().add(WResult.getResultNum());
            statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());



//            //octree
//            startTime = System.currentTimeMillis();
//            ResultVO OctreeResult = Octree.OctreeMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"Octree-32-64-128-256-512.a3d");
//            endTime = System.currentTimeMillis();
//            statisticsUtil.getOctreeTimes().add((endTime - startTime));
//            statisticsUtil.getOctreeResultSize().add(OctreeResult.getResultNum());
//            statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.currentTimeMillis();
            ResultVO VdbResult = VdbTree.vdbMultiScaleSinglePropertyQuery(singlePropertyQueryVO,"VdbTree-32-64-128-256-512.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            statisticsUtil.getVdbResultSize().add(VdbResult.getResultNum());
            statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            startTime = System.currentTimeMillis();
            GeoHashResultVO GeoHashResult = GeoHashQuery.geoHashSinglePropertyQuery(singlePropertyQueryVO,"GeoHash-32-64-128-256-512.a3d");
            endTime = System.currentTimeMillis();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil.getGeoHashResultSize().add(GeoHashResult.getResultNum());
            statisticsUtil.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,false,true,true);
        statisticsUtil.writeExcel("SinglePropertyQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_4\\", recorder, "");
    }
}
