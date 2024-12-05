package com.geohash;


import com.entity.coor.Box;
import com.entity.coor.Coordinate;
import com.entity.utils.DiskIOUtil;
import com.entity.property.PropertyRange;
import com.entity.VO.*;
import com.entity.constants.FilePathConstants;
import com.process.util.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;

public class GeoHashQuery {
    public static SpaceQueryResultVO geoHashSpaceQuery(SpaceQueryVO spaceQueryVO, String fileName) throws IOException {
        // 初始化结果列表
        SpaceQueryResultVO spaceQueryResultVO = new SpaceQueryResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            spaceQueryResultVO.getResult().add(0);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        Map<Long,Integer> locs = new TreeMap<>();
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            Coordinate min = new Coordinate(0,0,0);
            Coordinate max = new Coordinate(0,0,0);
            int difference = (1 << (ProcessUtil.Lmax - i));
            min.setX(spaceQueryVO.getBox().getCoordinateMin().getX() / difference);
            min.setY(spaceQueryVO.getBox().getCoordinateMin().getY() / difference);
            min.setZ(spaceQueryVO.getBox().getCoordinateMin().getZ() / difference);
            max.setX(spaceQueryVO.getBox().getCoordinateMax().getX() / difference);
            max.setY(spaceQueryVO.getBox().getCoordinateMax().getY() / difference);
            max.setZ(spaceQueryVO.getBox().getCoordinateMax().getZ() / difference);
            for (int j = min.getZ(); j <= max.getZ(); j++) {
                for (int k = min.getY(); k <= max.getY() ; k++) {
                    for (int l = min.getX(); l <= max.getX() ; l++) {
//                        System.out.println("l:" + l + " k:" + k + " j:" + j + " i:" + i);
                        String geoHashCode = GeoHashCode.encode(i, new long[]{l,k,j});
                        locs.put(GeoHashCode.getLoc(geoHashCode), i);
                    }
                }
            }
        }
        long num = ProcessUtil.num;
        for (Map.Entry<Long, Integer> entry : locs.entrySet()){
            fc.position(688 + entry.getKey());
            spaceQueryResultVO.getDiskIOUtil().insertData((688 + entry.getKey())/4096);
            if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
                Map<String,Float> property = new HashMap<>();
                List<String> propertysName = ProcessUtil.propertiesName;
                for (int i = 0; i < propertysName.size(); i++) {
                    fc.position(688 + num + (entry.getKey() * 4) + (i * num * 4));
                    spaceQueryResultVO.getDiskIOUtil().insertData((688 + num + (entry.getKey() * 4) + (i * num * 4))/4096);
                    property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                }
                spaceQueryResultVO.getResult().set(entry.getValue(), spaceQueryResultVO.getResult().get(entry.getValue()) + 1);
            }
        }
        fc.close();
        return spaceQueryResultVO;
    }


    public static GeoHashResultVO geoHashSinglePropertyQuery(SinglePropertyQueryVO singlePropertyQueryVO, String fileName) throws IOException {
        GeoHashResultVO resultVO = new GeoHashResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            List<String> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        long num = ProcessUtil.num;
        fc.position(688);

        int propertyIndex = ProcessUtil.propertiesName.indexOf(singlePropertyQueryVO.getPropertyName());
        for (int i = 0; i < num; i++) {
            fc.position(688 + i);
            resultVO.getDiskIOUtil().insertData((688 + i)/4096);
            if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){

                fc.position(688 + num + (i * 4) + (propertyIndex * num * 4));
                resultVO.getDiskIOUtil().insertData((688 + num + (i * 4) + (propertyIndex * num * 4))/4096);
                float p = (float)ProcessUtil.read(fc,4,"float1");
                for (PropertyRange propertyRange : singlePropertyQueryVO.getPropertyRanges()){
                    if(propertyRange.contain(p)){
                        //满足查询条件,将本尺度的结点放入结果
                        String geoHashCode = GeoHashCode.getCodeByLoc(i, 0);
                        int level = GeoHashCode.calculateLevel(geoHashCode);
                        resultVO.getResult().get(level).add(geoHashCode);
                        break;
                    }
                }
            }
        }
        return resultVO;
    }


    public static GeoHashResultVO geoHashMixedQuery(MixedQueryVO mixedQueryVO, String fileName) throws IOException{
        // 初始化结果列表
        GeoHashResultVO resultVO = new GeoHashResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            List<String> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        Map<Long, String> locs = new TreeMap<>();
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            Coordinate min = new Coordinate(0,0,0);
            Coordinate max = new Coordinate(0,0,0);
            int difference = (1 << (ProcessUtil.Lmax - i));
            Box box = mixedQueryVO.getSpaceQueryVO().getBox();
            min.setX(box.getCoordinateMin().getX() / difference);
            min.setY(box.getCoordinateMin().getY() / difference);
            min.setZ(box.getCoordinateMin().getZ() / difference);
            max.setX(box.getCoordinateMax().getX() / difference);
            max.setY(box.getCoordinateMax().getY() / difference);
            max.setZ(box.getCoordinateMax().getZ() / difference);
            for (int j = min.getZ(); j <= max.getZ(); j++) {
                for (int k = min.getY(); k <= max.getY() ; k++) {
                    for (int l = min.getX(); l <= max.getX() ; l++) {
                        String geoHashCode = GeoHashCode.encode(i, new long[]{l,k,j});
                        long loc = GeoHashCode.getLoc(geoHashCode);
                        locs.put(loc,geoHashCode);
                    }
                }
            }
        }
        long num = ProcessUtil.num;
        int propertyIndex = ProcessUtil.propertiesName.indexOf(mixedQueryVO.getSinglePropertyQueryVO().getPropertyName());
        for (Map.Entry<Long, String> entry : locs.entrySet()){
            fc.position(688 + entry.getKey());
            resultVO.getDiskIOUtil().insertData((688 + entry.getKey())/4096);
            if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){

                fc.position(688 + num + (entry.getKey() * 4) + (propertyIndex * num * 4));
                resultVO.getDiskIOUtil().insertData((688 + num + (entry.getKey() * 4) + (propertyIndex * num * 4))/4096);
                float p = (float)ProcessUtil.read(fc,4,"float1");
                for (PropertyRange propertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()){
                    if(propertyRange.contain(p)){
                        //满足查询条件,将本尺度的结点放入结果
                        resultVO.getResult().get(GeoHashCode.calculateLevel(entry.getValue())).add(entry.getValue());
                        break;
                    }
                }
            }
        }
        return resultVO;
    }


    public static ParentBlockResultVO geoHashParentBlockQuery(String geoHashCode, String fileName) throws IOException{
        int level = GeoHashCode.calculateLevel(geoHashCode);
        if(level == 0){
            return null;
        }
        ParentBlockResultVO parentBlockResultVO = new ParentBlockResultVO();
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();

        long loc = GeoHashCode.getLoc(geoHashCode);
        fc.position(688 + loc);
        parentBlockResultVO.getDiskIOUtil().insertData((688 + loc)/4096);
        if (String.valueOf(ProcessUtil.read(fc,1,"char")).equals("F")){
            return null;
        }

        String parentCode = GeoHashCode.getParentCode(geoHashCode, level - 1);
        long parentLoc = GeoHashCode.getLoc(parentCode);
        fc.position(688 + parentLoc);
        parentBlockResultVO.getDiskIOUtil().insertData((688 + parentLoc)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            List<String> propertysName = ProcessUtil.propertiesName;
            for (int i = 0; i < propertysName.size(); i++) {
                fc.position(688 + ProcessUtil.num + (parentLoc * 4) + (i * ProcessUtil.num * 4));
                parentBlockResultVO.getDiskIOUtil().insertData((688 + ProcessUtil.num + (parentLoc * 4) + (i * ProcessUtil.num * 4))/4096);
                parentBlockResultVO.getResult().put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
            }
            return parentBlockResultVO;
        }else {
            return null;
        }
    }


    public static ChildrenBlockResultVO childrenBlockQuery(String geoHashCode, String fileName) throws IOException{
        ChildrenBlockResultVO childrenBlockResultVO = new ChildrenBlockResultVO();
        int level = GeoHashCode.calculateLevel(geoHashCode);
        if(level == ProcessUtil.Lmax){
            return childrenBlockResultVO;
        }
        String[] childrenCode = GeoHashCode.getChildCode(geoHashCode,level + 1);
        long[] locs = new long[childrenCode.length];
        for (int i = 0; i < childrenCode.length; i++) {
            locs[i] = GeoHashCode.getLoc(childrenCode[i]);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        List<String> propertysName = ProcessUtil.propertiesName;
        for (int i = 0; i < locs.length; i++) {
            fc.position(688 + locs[i]);
            childrenBlockResultVO.getDiskIOUtil().insertData((688 + locs[i])/4096);
            if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
                Map<String, Float> result = new HashMap<>();
                for (int j = 0; j < propertysName.size(); j++) {
                    fc.position(688 + ProcessUtil.num + (locs[i] * 4) + (j * ProcessUtil.num * 4));
                    childrenBlockResultVO.getDiskIOUtil().insertData((688 + ProcessUtil.num + (locs[i] * 4) + (j * ProcessUtil.num * 4))/4096);
                    result.put(propertysName.get(j),(float)ProcessUtil.read(fc,4,"float1"));
                }
                childrenBlockResultVO.getResult().add(result);
            }
        }
        return childrenBlockResultVO;
    }
}
