package com.whmsdm.experiment_4_4;

import com.whmsdm.entity.VO.ChildrenBlockResultVO;
import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.entity.utils.MethodUseRecorder;
import com.whmsdm.entity.utils.StatisticsUtil;
import com.whmsdm.geohash.GeoHashCode;
import com.whmsdm.geohash.GeoHashQuery;
import com.whmsdm.octree.Octree;
import com.whmsdm.process.util.ProcessUtil;
import com.whmsdm.vdbTree.VdbTree;
import com.whmsdm.whmsdm.MSDM;
import org.junit.Test;

import java.io.IOException;

public class childTest {
    @Test
    public void test() throws IOException {
        ProcessUtil.setConfig(32, 512, 5);


        // 子块查询循环测试
        StatisticsUtil statisticsUtil2 = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            long WHCode = StatisticsUtil.randomWHCode(ProcessUtil.Lmax,3,ProcessUtil.LOffset);
            statisticsUtil2.getWHCodes().add(WHCode);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64-128-256-512.a3d");
            long startTime = System.nanoTime();
            ChildrenBlockResultVO result = MSDM.childrenBlockQuery(WHCode);
            long endTime = System.nanoTime();
            statisticsUtil2.getWTimes().add((endTime - startTime));
            statisticsUtil2.getWResultSize().add(result.getResult().size());
            statisticsUtil2.getWIOs().add(result.getDiskIOUtil().getDataCount());


//            //octree
//            startTime = System.nanoTime();
//            ChildrenBlockResultVO OctreeResult = Octree.childrenBlockQuery(WHCode, "Octree-32-64-128-256-512.a3d");
//            endTime = System.nanoTime();
//            statisticsUtil2.getOctreeTimes().add((endTime - startTime));
//            statisticsUtil2.getOctreeResultSize().add(OctreeResult.getResult().size());
//            statisticsUtil2.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());


            //vdb树
            startTime = System.nanoTime();
            ChildrenBlockResultVO VdbResult = VdbTree.childrenBlockQuery(WHCode, "VdbTree-32-64-128-256-512.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getVdbTimes().add((endTime - startTime));
            statisticsUtil2.getVdbResultSize().add(VdbResult.getResult().size());
            statisticsUtil2.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());


            // GeoHash
            String geoHash = GeoHashCode.WHCodeToGeoHash(WHCode, ProcessUtil.Lmax);
            startTime = System.nanoTime();
            ChildrenBlockResultVO GeoHashResult = GeoHashQuery.childrenBlockQuery(geoHash, "GeoHash-32-64-128-256-512.a3d");
            endTime = System.nanoTime();
            statisticsUtil2.getGeoHashTimes().add((endTime - startTime));
            statisticsUtil2.getGeoHashResultSize().add(GeoHashResult.getResult().size());
            statisticsUtil2.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());


        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,false,true,true);
        statisticsUtil2.writeExcel("ChildBlockQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_4\\", recorder, "");
    }
}
