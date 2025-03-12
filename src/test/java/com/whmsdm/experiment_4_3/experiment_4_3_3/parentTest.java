package com.whmsdm.experiment_4_3.experiment_4_3_3;

import com.whmsdm.entity.VO.ParentBlockResultVO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class parentTest {
    @Test
    public void Test_3() throws IOException {
        ProcessUtil.setConfig(32, 128, 5);

        // 父块查询循环测试
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            long WHCode = StatisticsUtil.randomWHCode(ProcessUtil.Lmax,3,ProcessUtil.LOffset);
            statisticsUtil.getWHCodes().add(WHCode);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-32-64-128.a3d");
            long startTime = System.nanoTime();
            ParentBlockResultVO WResult = MSDM.parentBlockQuery(WHCode);
            long endTime = System.nanoTime();
            statisticsUtil.getWTimes().add((endTime - startTime));
            if(WResult != null){
                statisticsUtil.getWResultSize().add(1);
                statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());
            }else{
                statisticsUtil.getWResultSize().add(0);
                statisticsUtil.getWIOs().add(0);
            }


            //octree
            startTime = System.nanoTime();
            ParentBlockResultVO OctreeResult = Octree.parentBlockQuery(WHCode, "Octree-32-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            if(OctreeResult != null){
                statisticsUtil.getOctreeResultSize().add(1);
                statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());
            }else{
                statisticsUtil.getOctreeResultSize().add(0);
                statisticsUtil.getOctreeIOs().add(0);
            }


            //vdb树
            startTime = System.nanoTime();
            ParentBlockResultVO VdbResult = VdbTree.parentBlockQuery(WHCode, "VdbTree-32-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            if(VdbResult != null){
                statisticsUtil.getVdbResultSize().add(1);
                statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());
            }else {
                statisticsUtil.getVdbResultSize().add(0);
                statisticsUtil.getVdbIOs().add(0);
            }



            //GeoHash
            String geoHash = GeoHashCode.WHCodeToGeoHash(WHCode, ProcessUtil.Lmax);
            startTime = System.nanoTime();
            ParentBlockResultVO GeoHashResult = GeoHashQuery.geoHashParentBlockQuery(geoHash, "GeoHash-32-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));

            if(GeoHashResult != null){
                statisticsUtil.getGeoHashResultSize().add(1);
                statisticsUtil.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());
            }else {
                statisticsUtil.getGeoHashResultSize().add(0);
                statisticsUtil.getGeoHashIOs().add(0);
            }
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("ParentBlockQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_3\\", recorder, "3-");
    }

    @Test
    public void Test_2() throws IOException {
        ProcessUtil.setConfig(64, 128, 5);

        // 父块查询循环测试
        StatisticsUtil statisticsUtil = new StatisticsUtil();
        for (int i = 0; i < 1000; i++) {
            long WHCode = StatisticsUtil.randomWHCode(ProcessUtil.Lmax,3,ProcessUtil.LOffset);
            statisticsUtil.getWHCodes().add(WHCode);
            System.out.println("第" + i + "次查询");

            //W
            MSDM MSDM = new MSDM(FilePathConstants.FILE_PATH + "W-64-128.a3d");
            long startTime = System.nanoTime();
            ParentBlockResultVO WResult = MSDM.parentBlockQuery(WHCode);
            long endTime = System.nanoTime();
            statisticsUtil.getWTimes().add((endTime - startTime));
            if(WResult != null){
                statisticsUtil.getWResultSize().add(1);
                statisticsUtil.getWIOs().add(WResult.getDiskIOUtil().getDataCount());
            }else{
                statisticsUtil.getWResultSize().add(0);
                statisticsUtil.getWIOs().add(0);
            }


            //octree
            startTime = System.nanoTime();
            ParentBlockResultVO OctreeResult = Octree.parentBlockQuery(WHCode, "Octree-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getOctreeTimes().add((endTime - startTime));
            if(OctreeResult != null){
                statisticsUtil.getOctreeResultSize().add(1);
                statisticsUtil.getOctreeIOs().add(OctreeResult.getDiskIOUtil().getDataCount());
            }else{
                statisticsUtil.getOctreeResultSize().add(0);
                statisticsUtil.getOctreeIOs().add(0);
            }


            //vdb树
            startTime = System.nanoTime();
            ParentBlockResultVO VdbResult = VdbTree.parentBlockQuery(WHCode, "VdbTree-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getVdbTimes().add((endTime - startTime));
            if(VdbResult != null){
                statisticsUtil.getVdbResultSize().add(1);
                statisticsUtil.getVdbIOs().add(VdbResult.getDiskIOUtil().getDataCount());
            }else {
                statisticsUtil.getVdbResultSize().add(0);
                statisticsUtil.getVdbIOs().add(0);
            }



            //GeoHash
            String geoHash = GeoHashCode.WHCodeToGeoHash(WHCode, ProcessUtil.Lmax);
            startTime = System.nanoTime();
            ParentBlockResultVO GeoHashResult = GeoHashQuery.geoHashParentBlockQuery(geoHash, "GeoHash-64-128.a3d");
            endTime = System.nanoTime();
            statisticsUtil.getGeoHashTimes().add((endTime - startTime));

            if(GeoHashResult != null){
                statisticsUtil.getGeoHashResultSize().add(1);
                statisticsUtil.getGeoHashIOs().add(GeoHashResult.getDiskIOUtil().getDataCount());
            }else {
                statisticsUtil.getGeoHashResultSize().add(0);
                statisticsUtil.getGeoHashIOs().add(0);
            }
        }
        MethodUseRecorder recorder = new MethodUseRecorder(true,true,true,true);
        statisticsUtil.writeExcel("ParentBlockQuery", FilePathConstants.RESULT_PATH
                + "experiment_4_3\\experiment_4_3_3\\", recorder, "2-");
    }
}
