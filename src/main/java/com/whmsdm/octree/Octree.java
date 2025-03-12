package com.whmsdm.octree;

import com.whmsdm.WHilbertUtil;
import com.whmsdm.entity.constants.FilePathConstants;
import com.whmsdm.entity.coor.Box;
import com.whmsdm.entity.coor.Coordinate;
import com.whmsdm.entity.property.PropertyRange;
import com.whmsdm.entity.utils.DiskIOUtil;
import com.whmsdm.process.util.ProcessUtil;
import com.whmsdm.vdbTree.BlockModelProperty;
import com.whmsdm.vdbTree.MultiScaleBlockModelProperty;
import com.whmsdm.entity.VO.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Octree {
    private Node root;// 根节点
    private int treeHeight;// 树的高度
    private MultiScaleBlockModelProperty msbmProperty;//  多尺度块模型属性

    public static int getLevelPos(){
        return 1 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getWCodePos(){
        return 5 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getOriginPos(){
        return 13 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getChildPos(){
        return 25 + 4 * ProcessUtil.propertiesName.size();
    }

    public Octree(Map<Integer, String> paths, int treeHeight) throws IOException {
        ProcessUtil.octree = this;
        this.treeHeight = treeHeight;
        this.root = new Node(0,-1,new Coordinate(0,0,0),new Node[8],true);
        this.msbmProperty = new MultiScaleBlockModelProperty();
        this.getMsbmProperty().setBlockModelPropertyMap(new HashMap<>());
        for (Map.Entry<Integer, String> entry : paths.entrySet()){
            BlockModelProperty blockModelProperty = new BlockModelProperty();
            File file = new File(entry.getValue());
            blockModelProperty.setRaf(new RandomAccessFile(file,"rw"));
            blockModelProperty.setFc(blockModelProperty.getRaf().getChannel());
            blockModelProperty.setLevel(entry.getKey());
            // 标识
            blockModelProperty.getFc().position(8);
            blockModelProperty.setIdentification((String) ProcessUtil.read(blockModelProperty.getFc(),8,"string"));
            // 版本
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setVersion((float) ProcessUtil.read(blockModelProperty.getFc(),4,"float"));
            // 时间
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setTime(new Date((Long) ProcessUtil.read(blockModelProperty.getFc(),8,"time_t")));
            // 模型名称
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setModelName((String) ProcessUtil.read(blockModelProperty.getFc(),32,"string"));
            // 模型uuid
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 16);
            // 起点
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setOrigin(new double[3]);
            blockModelProperty.getOrigin()[0] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            blockModelProperty.getOrigin()[1] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            blockModelProperty.getOrigin()[2] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            // 块体大小
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setBlockSize(new double[3]);
            blockModelProperty.getBlockSize()[0] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            blockModelProperty.getBlockSize()[1] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            blockModelProperty.getBlockSize()[2] = (double) ProcessUtil.read(blockModelProperty.getFc(), 8, "double");
            // 块体数
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.setBlockNums(new long[3]);
            blockModelProperty.getBlockNums()[0] = (long) ProcessUtil.read(blockModelProperty.getFc(), 8, "long");
            blockModelProperty.getBlockNums()[1] = (long) ProcessUtil.read(blockModelProperty.getFc(), 8, "long");
            blockModelProperty.getBlockNums()[2] = (long) ProcessUtil.read(blockModelProperty.getFc(), 8, "long");
            blockModelProperty.setTotalBlockCount(blockModelProperty.getBlockNums()[0] * blockModelProperty.getBlockNums()[1] * blockModelProperty.getBlockNums()[2]);
            // 父模型标识
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 16);
            // 父模型索引
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 48);
            // 有效性
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            long nums = blockModelProperty.getTotalBlockCount();
            blockModelProperty.setBlockValidity(new BitSet((int) nums));
            blockModelProperty.setValidBlockCount(0);
            for (int i = 0; i < nums; i++) {
                char valid = (char) ProcessUtil.read(blockModelProperty.getFc(), 1, "char");
                if ('T' == valid) {
                    blockModelProperty.getBlockValidity().set(i);
                    blockModelProperty.setValidBlockCount(blockModelProperty.getValidBlockCount() + 1);
                }
            }
            // 属性列表
            blockModelProperty.getFc().position(blockModelProperty.getFc().position() + 8);
            String[] splits = ((String) ProcessUtil.read(blockModelProperty.getFc(), 512, "string")).split("\t");
            for (int i = 0; i < splits.length; i++) {
                if ("顶底面".equals(splits[i])) {
                    splits[i] = "断裂";
                }
            }
            blockModelProperty.setPropertyStartOffset(blockModelProperty.getFc().position());
            // 将字符串数组的每个元素进行去除空格处理，并转换为列表，然后赋值给this.propertiesName
            blockModelProperty.setPropertiesName(ProcessUtil.propertiesName);
            // 为this.propertiesName中的每个属性名生成一个起始位置，并存储到this.propertiesOffsetMap中
            blockModelProperty.setPropertiesOffsetMap(IntStream.range(0, blockModelProperty.getPropertiesName().size())
                    .boxed()
                    .collect(Collectors.toMap(
                            i -> blockModelProperty.getPropertiesName().get(i),
                            blockModelProperty::getStartPosition
                    )));
            this.getMsbmProperty().getBlockModelPropertyMap().put(entry.getKey(), blockModelProperty);
            //  将这一层的数据插入树中
            for (int i = 0; i < blockModelProperty.getTotalBlockCount(); i++) {
                if(blockModelProperty.getBlockValidity().get(i)){  // 只有有效块材插入到树中
                    long wHilbertCode =  ProcessUtil.blockEncode(i, entry.getKey(), blockModelProperty.getBlockNums()[0], blockModelProperty.getBlockNums()[0], blockModelProperty.getBlockNums()[0]);

                    this.insert(entry.getKey() + ProcessUtil.LOffset, wHilbertCode, i);
                }
            }
        }
    }

    public void insert(int level, long wHilbertCode, int id){
        Node node = this.root;
        while (node.getLevel() != (level - 1)){
            node = node.getChild(level,wHilbertCode,id);
        }
        Node newNode = new Node(level,wHilbertCode,id,true);
        node.addChild(newNode, id);
    }

    public void createOctreeMultiScaleFile(String fileName) throws IOException{
        String octreeFileName = fileName;
        String path = FilePathConstants.FILE_PATH;
        File file = new File(path + octreeFileName);
        if (file.createNewFile()) {
            System.out.println("文件已创建成功。");
        } else {
            System.out.println("文件已存在，无需创建。");
            return;
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        fc.position(688);
        this.root.createOctreeMultiScaleFile(688, fc);
    }


    public static SpaceQueryResultVO multiScaleSpaceQuery3(SpaceQueryVO spaceQueryVO, String fileName) throws IOException{
        // 初始化结果列表
        SpaceQueryResultVO spaceQueryResultVO = new SpaceQueryResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            spaceQueryResultVO.getResult().add(0);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        spaceQueryRecursion(688,spaceQueryVO,spaceQueryResultVO,fc);
        return spaceQueryResultVO;
    }

    public static void spaceQueryRecursion(long position, SpaceQueryVO spaceQueryVO, SpaceQueryResultVO spaceQueryResultVO, FileChannel fc) throws IOException{
        fc.position(position);
        spaceQueryResultVO.getDiskIOUtil().insertData((position) / 4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            // 当前节点为有效结点
            fc.position(position + getLevelPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getLevelPos()) / 4096);
            int level = (int)ProcessUtil.read(fc,4,"int");

            int num = (1 << (ProcessUtil.Lmax + ProcessUtil.LOffset - level));//计算当前块一个方向上有多少最小的下属块
            fc.position(position + getOriginPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos()) / 4096);
            int x = (int)ProcessUtil.read(fc,4,"int");
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos() + 4) / 4096);
            int y = (int)ProcessUtil.read(fc,4,"int");
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos() + 8) / 4096);
            int z = (int)ProcessUtil.read(fc,4,"int");
            Coordinate coordinateMax = new Coordinate(x + num - 1,y + num - 1,z + num - 1);
            Coordinate coordinateMin = new Coordinate(x,y,z);
            if(spaceQueryVO.getBox().contain(new Box(coordinateMin,coordinateMax))){
                // 查询的空间范围包含了该块的管理范围
                DFS(position,spaceQueryResultVO,fc);
            } else if (spaceQueryVO.getBox().intersect(new Box(coordinateMin,coordinateMax))) {
                if(level >= ProcessUtil.LOffset){
                    //0-4层不包含数据
                    Map<String,Float> property = new HashMap<>();
                    List<String> propertysName = ProcessUtil.propertiesName;
                    for (int i = 0; i < propertysName.size(); i++) {
                        fc.position(position + 1 + 4 * i);
                        spaceQueryResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i) / 4096);
                        property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                    }
                    spaceQueryResultVO.getResult().set(level - ProcessUtil.LOffset, spaceQueryResultVO.getResult().get(level - ProcessUtil.LOffset) + 1);
                }
                for (int i = 0; i < 8; i++) {
                    fc.position(position + getChildPos() + 8 * i);
                    spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * i) / 4096);
                    long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                    if(childPosition != 0){
                        spaceQueryRecursion(childPosition,spaceQueryVO,spaceQueryResultVO,fc);
                    }
                }
            }
        }
    }

    public static void DFS(long position, SpaceQueryResultVO spaceQueryResultVO, FileChannel fc) throws IOException{
        fc.position(position);
        spaceQueryResultVO.getDiskIOUtil().insertData((position) / 4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            fc.position(position + getLevelPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getLevelPos()) / 4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            if(level >= ProcessUtil.LOffset){
                //0-4层不包含数据
                Map<String,Float> property = new HashMap<>();
                List<String> propertysName = ProcessUtil.propertiesName;
                for (int i = 0; i < propertysName.size(); i++) {
                    fc.position(position + 1 + 4 * i);
                    spaceQueryResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i) / 4096);
                    property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                }
                spaceQueryResultVO.getResult().set(level - ProcessUtil.LOffset, spaceQueryResultVO.getResult().get(level - ProcessUtil.LOffset) + 1);
            }
            for (int i = 0; i < 8; i++) {
                fc.position(position + getChildPos() + 8 * i);
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * i) / 4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                if(childPosition != 0){
                    DFS(childPosition,spaceQueryResultVO,fc);
                }
            }

        }
    }

    public static ResultVO OctreeMultiScaleSinglePropertyQuery(SinglePropertyQueryVO singlePropertyQueryVO, String fileName) throws IOException {
        ResultVO resultVO = new ResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            List<Long> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        singlePropertyQueryRecursion(688,singlePropertyQueryVO,resultVO,fc);
        return resultVO;
    }

    public static void singlePropertyQueryRecursion(long position, SinglePropertyQueryVO singlePropertyQueryVO, ResultVO resultVO, FileChannel fc) throws IOException{
        fc.position(position);
        resultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            // 当前节点为有效结点
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            if(level >= ProcessUtil.LOffset){
                //  0-4层结点不携带数据

                int propertyIndex = ProcessUtil.propertiesName.indexOf(singlePropertyQueryVO.getPropertyName());
                fc.position(position + 1 + 4 * propertyIndex);
                resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                float p = (float)ProcessUtil.read(fc,4,"float1");
                for (PropertyRange propertyRange : singlePropertyQueryVO.getPropertyRanges()){
                    if(propertyRange.contain(p)){
                        //满足查询条件,将本尺度的结点放入结果
                        fc.position(position + getWCodePos());
                        resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                        resultVO.getResult().get(level - ProcessUtil.LOffset).add((long)ProcessUtil.read(fc,8,"long1"));
                        break;
                    }
                }
            }
            for (int i = 0; i < 8; i++) {
                fc.position(position + getChildPos() + 8 * i);
                resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * i)/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                if(childPosition != 0){
                    // 递归查询子节点
                    singlePropertyQueryRecursion(childPosition,singlePropertyQueryVO,resultVO,fc);
                }
            }
        }
    }

    public static ResultVO multiScaleMixedQuery(MixedQueryVO mixedQueryVO, String fileName)  throws IOException{
        // 初始化结果列表
        ResultVO resultVO = new ResultVO(new ArrayList<>(),new DiskIOUtil());
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            List<Long> result = new ArrayList<>();
            resultVO.getResult().add(result);
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        mixedQueryRecursion(688,mixedQueryVO,resultVO,fc);
        return resultVO;
    }

    public static void mixedQueryRecursion(long position, MixedQueryVO mixedQueryVO, ResultVO resultVO, FileChannel fc) throws IOException{
        fc.position(position);
        resultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            // 当前节点为有效结点
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");

            int num = (1 << (ProcessUtil.Lmax + ProcessUtil.LOffset - level));//计算当前块一个方向上有多少最小的下属块
            fc.position(position + getOriginPos());
            resultVO.getDiskIOUtil().insertData((position + getOriginPos())/4096);
            int x = (int)ProcessUtil.read(fc,4,"int");
            resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 4)/4096);
            int y = (int)ProcessUtil.read(fc,4,"int");
            resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 8)/4096);
            int z = (int)ProcessUtil.read(fc,4,"int");
            Coordinate coordinateMax = new Coordinate(x + num - 1,y + num - 1,z + num - 1);
            Coordinate coordinateMin = new Coordinate(x,y,z);
            if(mixedQueryVO.getSpaceQueryVO().getBox().contain(new Box(coordinateMin,coordinateMax))){
                // 查询的空间范围包含了该块的管理范围,空间范围全满足，转入属性查询
                singlePropertyQueryRecursion(position,mixedQueryVO.getSinglePropertyQueryVO(),resultVO,fc);
            } else if (mixedQueryVO.getSpaceQueryVO().getBox().intersect(new Box(coordinateMin,coordinateMax))) {

                if(level >= ProcessUtil.LOffset){
                    //0-4层不包含数据

                    int propertyIndex = ProcessUtil.propertiesName.indexOf(mixedQueryVO.getSinglePropertyQueryVO().getPropertyName());
                    fc.position(position + 1 + 4 * propertyIndex);
                    resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                    float p = (float)ProcessUtil.read(fc,4,"float1");
                    for (PropertyRange propertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()){
                        if(propertyRange.contain(p)){
                            //满足查询条件,将本尺度的结点放入结果
                            fc.position(position + getWCodePos());
                            resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                            resultVO.getResult().get(level - ProcessUtil.LOffset).add((long)ProcessUtil.read(fc,8,"long1"));
                            break;
                        }
                    }

                }
                for (int i = 0; i < 8; i++) {
                    fc.position(position + getChildPos() + 8 * i);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * i)/4096);
                    long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                    if(childPosition != 0){
                        mixedQueryRecursion(childPosition,mixedQueryVO,resultVO,fc);
                    }
                }
            }
        }
    }


    public static ParentBlockResultVO parentBlockQuery(long WHCode, String fileName)throws IOException{
        int level = WHilbertUtil.getLevel(ProcessUtil.Lmax,WHCode,3) + ProcessUtil.LOffset;
        if (level <= ProcessUtil.LOffset){
            return null;
        }
        ParentBlockResultVO parentBlockResultVO = new ParentBlockResultVO();
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        long position = 688;
        position = getParentPosition(position,WHCode,0,level,fc,parentBlockResultVO);
        if(position == 0) return null;
        fc.position(position);
        parentBlockResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("F")){
            return null;
        }
        List<String> propertysName = ProcessUtil.propertiesName;
        for (int i = 0; i < propertysName.size(); i++) {
            fc.position(position + 1 + 4 * i);
            parentBlockResultVO.getDiskIOUtil().insertData((position)/4096);
            parentBlockResultVO.getResult().put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
        }
        return parentBlockResultVO;
    }

    public static long getChild(long position, long WHCode, int i, int level, FileChannel fc) throws IOException{
        fc.position(position);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            // 当前节点为有效节点
            long parentCode = WHilbertUtil.getParent(ProcessUtil.Lmax,WHCode,i - 4,3);
            for (int j = 0; j < 8; j++) {
                fc.position(position + 45 + 8 * j);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                fc.position(childPosition + 25);
                if(parentCode == (long)ProcessUtil.read(fc,8,"long1")){
                    return childPosition;
                }
            }
            return 0;
        }else {
            return 0;
        }
    }

    public static ChildrenBlockResultVO childrenBlockQuery(long WHCode, String fileName)throws IOException{
        ChildrenBlockResultVO childrenBlockResultVO = new ChildrenBlockResultVO();
        int level = WHilbertUtil.getLevel(ProcessUtil.Lmax,WHCode,3) + ProcessUtil.LOffset;
        if (level == ProcessUtil.Lmax + ProcessUtil.LOffset){
            return childrenBlockResultVO;
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        long position = 688;
        position = getPosition(position,WHCode,0,level,fc,childrenBlockResultVO.getDiskIOUtil());
        fc.position(position);
        childrenBlockResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("F")){
            return childrenBlockResultVO;
        }
        List<String> propertysName = ProcessUtil.propertiesName;
        for (int i = 0; i < 8; i++) {
            fc.position(position + getChildPos() + 8 * i);
            childrenBlockResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * i)/4096);
            long childPosition = (long)ProcessUtil.read(fc,8,"long1");
            if(childPosition != 0){
                fc.position(childPosition);
                childrenBlockResultVO.getDiskIOUtil().insertData((childPosition)/4096);
                if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
                    Map<String, Float> result = new HashMap<>();
                    for (int j = 0; j < propertysName.size(); j++) {
                        fc.position(childPosition + 1 + 4 * j);
                        childrenBlockResultVO.getDiskIOUtil().insertData((childPosition + 1 + 4 * j)/4096);
                        result.put(propertysName.get(j),(float)ProcessUtil.read(fc,4,"float1"));
                    }
                    childrenBlockResultVO.getResult().add(result);
                }
            }
        }
        return childrenBlockResultVO;
    }

    public static long getPosition(long position, long WHCode, int i, int level, FileChannel fc,DiskIOUtil diskIOUtil)throws IOException{
        fc.position(position);
        diskIOUtil.insertData((position)/4096);
        if (String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            if(i == level){
                fc.position(position + getWCodePos());
                diskIOUtil.insertData((position + getWCodePos())/4096);
                if(WHCode == (long)ProcessUtil.read(fc,8,"long1")){
                    return position;
                }
            }else {
                for (int j = 0; j < 8; j++) {
                    fc.position(position + getChildPos() + 8 * j);
                    diskIOUtil.insertData((position + getChildPos() + 8 * j)/4096);
                    long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                    if(childPosition != 0){
                        long result = getPosition(childPosition,WHCode,i + 1,level,fc,diskIOUtil);
                        if(result != 0) return result;
                    }
                }
            }
            return 0;
        }else {
            return 0;
        }
    }

    public static long getParentPosition(long position, long WHCode, int i, int level, FileChannel fc,ParentBlockResultVO parentBlockResultVO) throws IOException{
        fc.position(position);
        parentBlockResultVO.getDiskIOUtil().insertData((position)/4096);
        if (String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            if(i == level){
                fc.position(position + getWCodePos());
                parentBlockResultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                if(WHCode == (long)ProcessUtil.read(fc,8,"long1")){
                    return -1;
                }
            }else {
                for (int j = 0; j < 8; j++) {
                    fc.position(position + getChildPos() + 8 * j);
                    parentBlockResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * j)/4096);
                    long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                    if(childPosition != 0){
                        long result = getParentPosition(childPosition,WHCode,i + 1,level,fc,parentBlockResultVO);
                        if(result == -1){
                            return position;
                        }else if(result != 0){
                            return result;
                        }
                    }
                }
            }
            return 0;
        }else {
            return 0;
        }
    }
}
