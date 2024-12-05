package com.vdbTree;

import com.entity.coor.Box;
import com.WHilbertUtil;
import com.entity.coor.Coordinate;
import com.entity.utils.DiskIOUtil;
import com.entity.property.PropertyRange;
import com.entity.VO.*;
import com.entity.constants.FilePathConstants;
import com.octree.Num;
import com.process.util.ProcessUtil;
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
public class VdbTree {
    private InternalNode root;// 根节点
    private List<BranchFactor> branchFactors;//  分支因子组，除叶子结点外，一个分支因子对应一层的结点
    private int treeHeight;  //  树的高度，同时也是分支因子组中分支因子数量+1，因为叶子节点没有分支因子
    private MultiScaleBlockModelProperty msbmProperty;//  多尺度块模型属性
    private List<Integer> blockNums;//  块体数量列表，每个元素对应一个level，表示该level层的块体数量
    private List<Integer> childrenBlockNums;

    public static int getLevelPos(){
        return 1 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getWCodePos(){
        return 5 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getOriginPos(){
        return 13 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getRangePos(){
        return 25 + 4 * ProcessUtil.propertiesName.size();
    }

    public static int getChildPos(){
        return 25 + 12 * ProcessUtil.propertiesName.size();
    }

    public VdbTree(Map<Integer, String> paths, List<BranchFactor> branchFactors) throws IOException{
        ProcessUtil.vdbTree = this;
        this.msbmProperty = new MultiScaleBlockModelProperty();
        this.getMsbmProperty().setBlockModelPropertyMap(new HashMap<>());
        this.treeHeight = branchFactors.size() + 1;
        this.branchFactors = branchFactors;
        this.setBlockNums();
        this.setChildrenBlockNums();
        this.root = new InternalNode();
        this.root.setLevel(-1); // 根节点level为-1，这里的level对应模型的level，根节点不对应任意一层模型，所以此处level为-1
        this.root.setOrigin(new Coordinate(0,0,0));
        this.root.setWHilbertCode(-1);  // 根节点不属于多尺度模型中的一个块，所以其没有对应的Hilbert编码，此处Hilbert编码为-1
        this.root.setChildrenNode(new HashMap<>());
        this.root.setPropertyRangeMap(new HashMap<>());
        for(Map.Entry<Integer, String> entry : paths.entrySet()){
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
            for (int i = 0; i < this.getBlockNums().get(entry.getKey()); i++) {
                if(blockModelProperty.getBlockValidity().get(i)){  // 只有有效块材插入到树中
                    int wHilbertCode = (int) ProcessUtil.blockEncode(i, entry.getKey(), blockModelProperty.getBlockNums()[0], blockModelProperty.getBlockNums()[0], blockModelProperty.getBlockNums()[0]);
                    insert(entry.getKey(), i,blockModelProperty.getSingleBlockAllPropertyValue(wHilbertCode),wHilbertCode);
                }
            }
        }
    }

    public void setBlockNums(){
        this.setBlockNums(new ArrayList<>());
        BranchFactor branchFactor = new BranchFactor(0,0,0);
        for(BranchFactor entry : this.branchFactors){
            branchFactor = entry.merge(branchFactor);
            this.getBlockNums().add(branchFactor.getTotal());
        }
    }

    public void setChildrenBlockNums(){
        this.setChildrenBlockNums(new ArrayList<>());
//        this.getChildrenBlockNums().add(8);
//        this.getChildrenBlockNums().add(4);
//        this.getChildrenBlockNums().add(2);
//        this.getChildrenBlockNums().add(1);
        int value = 1 << (this.branchFactors.size() - 1); // 计算2的size次幂
        while (value >= 1) {
            this.getChildrenBlockNums().add(value);
            value >>= 1; // 每次减半
        }
    }

    public void insert(int level, int id, Map<String,Float> property, int wHilbertCode){
        if( this.root.getPropertyRangeMap().isEmpty() ) {  //插入的第一个数据，要先初始化根节点的属性范围
            for (Map.Entry<String, Float> entry : property.entrySet()) {
                this.root.getPropertyRangeMap().put(entry.getKey(), new PropertyRange(entry.getValue(),entry.getValue()));
            }
        }
        InternalNode node = this.root;
        while (node.getLevel() != level - 1){
            node = (InternalNode) node.getChildren(property,level,id);
        }
        if(level == ProcessUtil.Lmax){  //如果插入的是叶子结点
            LeafNode leafNode = new LeafNode(level,wHilbertCode,this.getOrigin(level,id));
            node.insert(leafNode,property,id);
        }else {  // 插入的是非叶子节点
            InternalNode internalNode = new InternalNode(level,wHilbertCode,this.getOrigin(level,id),property);
            node.insert(internalNode,property,id);
        }
    }

    public Coordinate getOrigin(int level, int id){
        Coordinate coordinate = new Coordinate(0,0,0);
        int remainder = id % (int)this.msbmProperty.getBlockModelPropertyMap().get(level).getBlockNums()[2];
        coordinate.setX(remainder * this.childrenBlockNums.get(level));
        int quotient = id / (int)this.msbmProperty.getBlockModelPropertyMap().get(level).getBlockNums()[2];
        remainder = quotient % (int)this.msbmProperty.getBlockModelPropertyMap().get(level).getBlockNums()[1];
        coordinate.setY(remainder * this.childrenBlockNums.get(level));
        quotient = quotient / (int)this.msbmProperty.getBlockModelPropertyMap().get(level).getBlockNums()[1];
        remainder = quotient % (int)this.msbmProperty.getBlockModelPropertyMap().get(level).getBlockNums()[0];
        coordinate.setZ(remainder * this.childrenBlockNums.get(level));
        return coordinate;
    }

    public void createVdbTreeMultiScaleFile(String fileName) throws IOException {
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
        this.root.createVdbTreeMultiScaleFile(true,688, fc);
    }

    public void getNum(Num num){
        this.root.getNum(num);
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

    public static void spaceQueryRecursion(long position,SpaceQueryVO spaceQueryVO,SpaceQueryResultVO spaceQueryResultVO,FileChannel fc) throws IOException{
        fc.position(position);
        spaceQueryResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            fc.position(position + getLevelPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            Coordinate coordinateMin = new Coordinate(0,0,0);
            int s = ProcessUtil.Lmax;
            Coordinate coordinateMax = new Coordinate(ProcessUtil.blockNums[s],ProcessUtil.blockNums[s],ProcessUtil.blockNums[s]);
            if(level != -1){
                //非根节点
                int num = ProcessUtil.childrenBlockNums.get(level);
                fc.position(position + getOriginPos());
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos())/4096);
                int x = (int)ProcessUtil.read(fc,4,"int");
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos() + 4)/4096);
                int y = (int)ProcessUtil.read(fc,4,"int");
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getOriginPos() + 8)/4096);
                int z = (int)ProcessUtil.read(fc,4,"int");
                coordinateMin.setX(x);
                coordinateMin.setY(y);
                coordinateMin.setZ(z);
                coordinateMax.setX(x + num - 1);
                coordinateMax.setY(y + num - 1);
                coordinateMax.setZ(z + num - 1);
            }
            if (level == ProcessUtil.Lmax) {
                if(spaceQueryVO.getBox().contain(coordinateMin)){
                    Map<String,Float> property = new HashMap<>();
                    List<String> propertysName = ProcessUtil.propertiesName;
                    for (int i = 0; i < propertysName.size(); i++) {
                        fc.position(position + 1 + 4 * i);
                        spaceQueryResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i)/4096);
                        property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                    }
                    spaceQueryResultVO.getResult().set(level, spaceQueryResultVO.getResult().get(level) + 1);
                }
                return;
            }
            if(spaceQueryVO.getBox().contain(new Box(coordinateMin,coordinateMax))){
                // 查询的空间范围包含了该块的管理范围
                DFS(position,spaceQueryResultVO,fc);
            }else if (spaceQueryVO.getBox().intersect(new Box(coordinateMin,coordinateMax))) {
                // 查询的空间范围与该块的管理范围有交集
                if(level != -1){
                    //根节点不携带数据
                    Map<String,Float> property = new HashMap<>();
                    List<String> propertysName = ProcessUtil.propertiesName;
                    for (int i = 0; i < propertysName.size(); i++) {
                        fc.position(position + 1 + 4 * i);
                        spaceQueryResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i)/4096);
                        property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                    }
                    spaceQueryResultVO.getResult().set(level, spaceQueryResultVO.getResult().get(level) + 1);
                }
                int count = 0;
                fc.position(position + getChildPos());
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    spaceQueryRecursion(childPosition,spaceQueryVO,spaceQueryResultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
                }
            }
        }
    }

    public static void DFS(long position, SpaceQueryResultVO spaceQueryResultVO, FileChannel fc) throws IOException{
        fc.position(position);
        spaceQueryResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            fc.position(position + getLevelPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            if(level != -1){
                //根节点不携带数据
                Map<String,Float> property = new HashMap<>();
                List<String> propertysName = ProcessUtil.propertiesName;
                for (int i = 0; i < propertysName.size(); i++) {
                    fc.position(position + 1 + 4 * i);
                    spaceQueryResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i)/4096);
                    property.put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
                }
                spaceQueryResultVO.getResult().set(level, spaceQueryResultVO.getResult().get(level) + 1);
            }
            if (level == ProcessUtil.Lmax){
                return;
            }
            int count = 0;
            fc.position(position + getChildPos());
            spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
            long childPosition = (long)ProcessUtil.read(fc,8,"long1");
            while(childPosition != 0){
                DFS(childPosition,spaceQueryResultVO,fc);
                count++;
                fc.position(position + getChildPos() + 8 * count);
                spaceQueryResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                childPosition = (long)ProcessUtil.read(fc,8,"long1");
            }

        }
    }

    public static void DFS(long position,ResultVO resultVO,FileChannel fc) throws IOException{
        fc.position(position);
        resultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            if(level != -1){
                fc.position(position + getWCodePos());
                resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
            }
            if (level == ProcessUtil.Lmax){
                return;
            }

            // 递归在子节点下寻找
            int count = 0;
            fc.position(position + getChildPos());
            resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
            long childPosition = (long)ProcessUtil.read(fc,8,"long1");
            while(childPosition != 0){
                DFS(childPosition,resultVO,fc);
                count++;
                fc.position(position + getChildPos() + 8 * count);
                resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                childPosition = (long)ProcessUtil.read(fc,8,"long1");
            }

        }
    }

    public static void spaceQueryRecursion(long position,SpaceQueryVO spaceQueryVO,ResultVO resultVO,FileChannel fc) throws IOException{
        fc.position(position);
        resultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            Coordinate coordinateMin = new Coordinate(0,0,0);
            int s = ProcessUtil.Lmax;
            Coordinate coordinateMax = new Coordinate(ProcessUtil.blockNums[s],ProcessUtil.blockNums[s],ProcessUtil.blockNums[s]);
            if(level != -1){
                //非根节点
                int num = ProcessUtil.childrenBlockNums.get(level);
                fc.position(position + getOriginPos());
                resultVO.getDiskIOUtil().insertData((position + getOriginPos())/4096);
                int x = (int)ProcessUtil.read(fc,4,"int");
                resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 4)/4096);
                int y = (int)ProcessUtil.read(fc,4,"int");
                resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 8)/4096);
                int z = (int)ProcessUtil.read(fc,4,"int");
                coordinateMin.setX(x);
                coordinateMin.setY(y);
                coordinateMin.setZ(z);
                coordinateMax.setX(x + num - 1);
                coordinateMax.setY(y + num - 1);
                coordinateMax.setZ(z + num - 1);
            }
            if (level == ProcessUtil.Lmax) {
                if(spaceQueryVO.getBox().contain(coordinateMin)){
                    //满足查询条件,将本尺度的结点放入结果
                    fc.position(position + getWCodePos());
                    resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                    resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                }
                return;
            }
            if(spaceQueryVO.getBox().contain(new Box(coordinateMin,coordinateMax))){
                // 查询的空间范围包含了该块的管理范围
                DFS(position,resultVO,fc);
            }else if (spaceQueryVO.getBox().intersect(new Box(coordinateMin,coordinateMax))) {
                // 查询的空间范围与该块的管理范围有交集
                if(level != -1){
                    //根节点不携带数据
                    //满足查询条件,将本尺度的结点放入结果
                    fc.position(position + getWCodePos());
                    resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                    resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                }
                int count = 0;
                fc.position(position + getChildPos());
                resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    spaceQueryRecursion(childPosition,spaceQueryVO,resultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
                }
            }
        }
    }

    public static ResultVO vdbMultiScaleSinglePropertyQuery(SinglePropertyQueryVO singlePropertyQueryVO, String fileName) throws IOException {
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
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            int propertyIndex = ProcessUtil.propertiesName.indexOf(singlePropertyQueryVO.getPropertyName());
            if(level == ProcessUtil.Lmax){
                // 叶子节点，没有属性范围

                fc.position(position + 1 + 4 * propertyIndex);
                resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                float p = (float)ProcessUtil.read(fc,4,"float1");
                for (PropertyRange propertyRange : singlePropertyQueryVO.getPropertyRanges()){
                    if(propertyRange.contain(p)){
                        //满足查询条件,将本尺度的结点放入结果
                        fc.position(position + getWCodePos());
                        resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                        resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                        break;
                    }
                }
                return;
            }
            fc.position(position + getRangePos() + 8 * propertyIndex);
            resultVO.getDiskIOUtil().insertData((position + getRangePos() + 8 * propertyIndex)/4096);
            float propertyMin = (float)ProcessUtil.read(fc,4,"float1");
            resultVO.getDiskIOUtil().insertData((position + getRangePos() + 4 + 8 * propertyIndex)/4096);
            float propertyMax = (float)ProcessUtil.read(fc,4,"float1");
            PropertyRange propertyRange = new PropertyRange(propertyMin,propertyMax);

            if(singlePropertyQueryVO.contain(propertyRange)){
                // 查询的范围包含了该块的管理范围
                DFS(position,resultVO,fc);
            }else if (singlePropertyQueryVO.intersect(propertyRange)) {
                // 查询的范围与该块的管理范围有交集
                if(level != -1){
                    //根节点不携带数据

                    //查看当前块是否满足查询条件
                    fc.position(position + 1 + 4 * propertyIndex);
                    resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                    float p = (float)ProcessUtil.read(fc,4,"float1");
                    for (PropertyRange queryPropertyRange : singlePropertyQueryVO.getPropertyRanges()){
                        if(queryPropertyRange.contain(p)){
                            //满足查询条件,将本尺度的结点放入结果
                            fc.position(position + getWCodePos());
                            resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                            resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                            break;
                        }
                    }
                }

                // 递归在子节点下寻找
                int count = 0;
                fc.position(position + getChildPos());
                resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    singlePropertyQueryRecursion(childPosition,singlePropertyQueryVO,resultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
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
            fc.position(position + getLevelPos());
            resultVO.getDiskIOUtil().insertData((position + getLevelPos())/4096);
            int level = (int)ProcessUtil.read(fc,4,"int");
            Coordinate coordinateMin = new Coordinate(0,0,0);
            int s = ProcessUtil.Lmax;
            Coordinate coordinateMax = new Coordinate(ProcessUtil.blockNums[s],ProcessUtil.blockNums[s],ProcessUtil.blockNums[s]);
            int propertyIndex = ProcessUtil.propertiesName.indexOf(mixedQueryVO.getSinglePropertyQueryVO().getPropertyName());
            if(level != -1){
                //非根节点，需要计算其表示的空间范围
                int num = ProcessUtil.childrenBlockNums.get(level);
                fc.position(position + getOriginPos());
                resultVO.getDiskIOUtil().insertData((position + getOriginPos())/4096);
                int x = (int)ProcessUtil.read(fc,4,"int");
                resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 4)/4096);
                int y = (int)ProcessUtil.read(fc,4,"int");
                resultVO.getDiskIOUtil().insertData((position + getOriginPos() + 8)/4096);
                int z = (int)ProcessUtil.read(fc,4,"int");
                coordinateMin.setX(x);
                coordinateMin.setY(y);
                coordinateMin.setZ(z);
                coordinateMax.setX(x + num - 1);
                coordinateMax.setY(y + num - 1);
                coordinateMax.setZ(z + num - 1);
            }
            if (level == ProcessUtil.Lmax) {
                //叶子结点，直接判断本结点是否满足要求，然后返回
                if(mixedQueryVO.getSpaceQueryVO().getBox().contain(coordinateMin)){
                    //满足空间条件

                    //查看当前块是否满足查询条件
                    fc.position(position + 1 + 4 * propertyIndex);
                    resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                    float p = (float)ProcessUtil.read(fc,4,"float1");
                    for (PropertyRange queryPropertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()){
                        if(queryPropertyRange.contain(p)){
                            //满足查询条件,将本尺度的结点放入结果
                            fc.position(position + getWCodePos());
                            resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                            resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                            break;
                        }
                    }
                }
                return;
            }
            // 获得非叶子结点的数据范围
            fc.position(position + getRangePos() + 8 * propertyIndex);
            resultVO.getDiskIOUtil().insertData((position + getRangePos() + 8 * propertyIndex)/4096);
            float propertyMin = (float)ProcessUtil.read(fc,4,"float1");
            resultVO.getDiskIOUtil().insertData((position + getRangePos() + 4 + 8 * propertyIndex)/4096);
            float propertyMax = (float)ProcessUtil.read(fc,4,"float1");
            PropertyRange propertyRange = new PropertyRange(propertyMin,propertyMax);
            Box box = new Box(coordinateMin,coordinateMax);
            if(mixedQueryVO.getSpaceQueryVO().getBox().contain(box) &&
                    mixedQueryVO.getSinglePropertyQueryVO().contain(propertyRange)) {
                //空间包含，属性包含

                //将此节点以及下属的所有结点都放入结果
                DFS(position,resultVO,fc);

            } else if (mixedQueryVO.getSpaceQueryVO().getBox().contain(box) &&
                    mixedQueryVO.getSinglePropertyQueryVO().intersect(propertyRange)) {
                //空间包含，属性相交

                //查看当前块是否满足查询条件
                if(level != -1){
                    //非根节点，需要计算其表示的空间范围
                    fc.position(position + 1 + 4 * propertyIndex);
                    resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                    float p = (float)ProcessUtil.read(fc,4,"float1");
                    for (PropertyRange queryPropertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()){
                        if(queryPropertyRange.contain(p)){
                            //满足查询条件,将本尺度的结点放入结果
                            fc.position(position + getWCodePos());
                            resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                            resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                            break;
                        }
                    }
                }

                // 递归在子节点下寻找
                int count = 0;
                fc.position(position + getChildPos());
                resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    //递归进行属性查询
                    singlePropertyQueryRecursion(childPosition,mixedQueryVO.getSinglePropertyQueryVO(),resultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
                }

            } else if (mixedQueryVO.getSpaceQueryVO().getBox().intersect(box) &&
                    mixedQueryVO.getSinglePropertyQueryVO().contain(propertyRange))  {
                //空间相交，属性包含

                if(level != -1){
                    //根节点不携带数据

                    //满足查询条件,将本尺度的结点放入结果
                    fc.position(position + getWCodePos());
                    resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                    resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                }

                // 递归在子节点下寻找
                int count = 0;
                fc.position(position + getChildPos());
                resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    //递归进行空间查询
                    spaceQueryRecursion(childPosition,mixedQueryVO.getSpaceQueryVO(),resultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
                }

            } else if (mixedQueryVO.getSpaceQueryVO().getBox().intersect(box) &&
                    mixedQueryVO.getSinglePropertyQueryVO().intersect(propertyRange))   {
                //空间相交，属性相交

                //查看当前块是否满足查询条件
                if(level != -1){
                    //非根节点，需要计算其表示的空间范围
                    fc.position(position + 1 + 4 * propertyIndex);
                    resultVO.getDiskIOUtil().insertData((position + 1 + 4 * propertyIndex)/4096);
                    float p = (float)ProcessUtil.read(fc,4,"float1");
                    for (PropertyRange queryPropertyRange : mixedQueryVO.getSinglePropertyQueryVO().getPropertyRanges()){
                        if(queryPropertyRange.contain(p)){
                            //满足查询条件,将本尺度的结点放入结果
                            fc.position(position + getWCodePos());
                            resultVO.getDiskIOUtil().insertData((position + getWCodePos())/4096);
                            resultVO.getResult().get(level).add((long)ProcessUtil.read(fc,8,"long1"));
                            break;
                        }
                    }
                }

                // 递归在子节点下寻找
                int count = 0;
                fc.position(position + getChildPos());
                resultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
                long childPosition = (long)ProcessUtil.read(fc,8,"long1");
                while(childPosition != 0){
                    //递归进行混合查询
                    mixedQueryRecursion(childPosition,mixedQueryVO,resultVO,fc);
                    count++;
                    fc.position(position + getChildPos() + 8 * count);
                    resultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
                    childPosition = (long)ProcessUtil.read(fc,8,"long1");
                }
            }
        }
    }

    public static ParentBlockResultVO parentBlockQuery(long WHCode, String fileName) throws IOException{
        int level = WHilbertUtil.getLevel(ProcessUtil.Lmax,WHCode,3);
        if (level == 0){
            return null;
        }
        ParentBlockResultVO parentBlockResultVO = new ParentBlockResultVO();
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();

        long position = 688;
        for (int i = -1; i < level - 1; i++) {
            position = getChild(position,WHCode,i,level,fc,parentBlockResultVO.getDiskIOUtil());
            if (position == 0){
                return null;
            }
        }
        fc.position(position);
        parentBlockResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("F")){
            return null;
        }

        // 判断查询的块体是否有效
        boolean isValid = false;
        int count = 0;
        fc.position(position + getChildPos());
        parentBlockResultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
        long childPosition = (long)ProcessUtil.read(fc,8,"long1");
        while(childPosition != 0){
            fc.position(childPosition);
            parentBlockResultVO.getDiskIOUtil().insertData((childPosition)/4096);
            boolean Valid = String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T");
            fc.position(childPosition + getWCodePos());
            parentBlockResultVO.getDiskIOUtil().insertData((childPosition + getWCodePos())/4096);
            long Code = (long)ProcessUtil.read(fc,8,"long1");
            if(Valid && Code == WHCode){
                isValid = true;
                break;
            }
            count++;
            fc.position(position + getChildPos() + 8 * count);
            parentBlockResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
            childPosition = (long)ProcessUtil.read(fc,8,"long1");
        }
        if(!isValid){
            return null;
        }

        List<String> propertysName = ProcessUtil.propertiesName;
        for (int i = 0; i < propertysName.size(); i++) {
            fc.position(position + 1 + 4 * i);
            parentBlockResultVO.getDiskIOUtil().insertData((position + 1 + 4 * i)/4096);
            parentBlockResultVO.getResult().put(propertysName.get(i),(float)ProcessUtil.read(fc,4,"float1"));
        }
        return parentBlockResultVO;
    }

    public static long getChild(long position,long WHCode,int i,int level,FileChannel fc,DiskIOUtil diskIOUtil) throws IOException{
        fc.position(position);
        diskIOUtil.insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("T")){
            // 当前节点为有效节点
            long parentCode = WHilbertUtil.getParent(ProcessUtil.Lmax,WHCode,i + 1,3);
            int count = 0;
            fc.position(position + getChildPos());
            diskIOUtil.insertData((position + getChildPos())/4096);
            long childPosition = (long)ProcessUtil.read(fc,8,"long1");
            while(childPosition != 0){
                //递归进行属性查询
                fc.position(childPosition + getWCodePos());
                diskIOUtil.insertData((position + getWCodePos())/4096);
                if(parentCode == (long)ProcessUtil.read(fc,8,"long1")){
                    return childPosition;
                }
                count++;
                fc.position(position + getChildPos() + 8 * count);
                diskIOUtil.insertData((position + getChildPos() + 8 * count)/4096);
                childPosition = (long)ProcessUtil.read(fc,8,"long1");
            }
            return 0;
        }else {
            return 0;
        }
    }

    public static ChildrenBlockResultVO childrenBlockQuery(long WHCode, String fileName)throws IOException{
        ChildrenBlockResultVO childrenBlockResultVO = new ChildrenBlockResultVO();
        int level = WHilbertUtil.getLevel(ProcessUtil.Lmax,WHCode,3);
        if (level == ProcessUtil.Lmax){
            return childrenBlockResultVO;
        }
        File file = new File(FilePathConstants.FILE_PATH + fileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        long position = 688;
        for (int i = -1; i < level; i++) {
            position = getChild(position,WHCode,i,level,fc,childrenBlockResultVO.getDiskIOUtil());
            if (position == 0){
                return childrenBlockResultVO;
            }
        }
        fc.position(position);
        childrenBlockResultVO.getDiskIOUtil().insertData((position)/4096);
        if(String.valueOf(ProcessUtil.read(fc,1,"char")).equals("F")){
            return childrenBlockResultVO;
        }
        List<String> propertysName = ProcessUtil.propertiesName;
        int count = 0;
        fc.position(position + getChildPos());
        childrenBlockResultVO.getDiskIOUtil().insertData((position + getChildPos())/4096);
        long childPosition = (long)ProcessUtil.read(fc,8,"long1");
        while(childPosition != 0){
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
            count++;
            fc.position(position + getChildPos() + 8 * count);
            childrenBlockResultVO.getDiskIOUtil().insertData((position + getChildPos() + 8 * count)/4096);
            childPosition = (long)ProcessUtil.read(fc,8,"long1");
        }
        return childrenBlockResultVO;
    }
}
