package com.vdbTree;

import com.WHilbertUtil;
import com.entity.coor.Coordinate;
import com.entity.property.PropertyRange;
import com.octree.Num;
import com.process.util.ParserUtil;
import com.process.util.ProcessUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InternalNode extends Node{
    private Map<Integer, Node> childrenNode;//key为子节点的id，value为子节点
    private Map<String, PropertyRange> propertyRangeMap;//key为属性名，value为属性值的范围

    public InternalNode(int level, int wHilbertCode, Coordinate origin , Map<String, Float> property){
        this.level = level;
        this.wHilbertCode = wHilbertCode;
        this.origin = origin;
        this.childrenNode = new HashMap<>();
        this.propertyRangeMap = new HashMap<>();
        for (Map.Entry<String, Float> entry : property.entrySet()) {
            this.propertyRangeMap.put(entry.getKey(), new PropertyRange(entry.getValue(),entry.getValue()));
        }
    }

    public Node getChildren(Map<String, Float> property,int level,int id){
        int subscript = -1;
        if((level - this.level) == 1){
            subscript = id;
        }
        else {
            Coordinate coordinate = this.idToCoordinate(level,id);
            int difference = 1 << (level - this.level - 1);
            coordinate.setX(coordinate.getX() / difference);
            coordinate.setY(coordinate.getY() / difference);
            coordinate.setZ(coordinate.getZ() / difference);
            subscript = this.coordinateToId(this.level,coordinate);
        }
        if(this.childrenNode.get(subscript) == null){
            int num = (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(this.level + 1).getBlockNums()[0];
            int code = (int)ProcessUtil.blockEncode(subscript,this.level + 1,num,num,num);
            InternalNode node = new InternalNode(this.level + 1, code,ProcessUtil.vdbTree.getOrigin(this.level + 1,subscript) , property);
            this.childrenNode.put(subscript,node);
        }
        this.updatePropertyRange(property);
        return this.childrenNode.get(subscript);
    }

    public void updatePropertyRange(Map<String, Float> property) {
        for (Map.Entry<String, Float> entry : property.entrySet()) {
            this.propertyRangeMap.get(entry.getKey()).merge(entry.getValue());  // 有效则维护属性范围
        }
    }

    public Coordinate idToCoordinate(int level, int id){
        Coordinate coordinate = new Coordinate(0,0,0);
        int remainder = id % (int) ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level).getBlockNums()[2];
        coordinate.setX(remainder);
        int quotient = id / (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level).getBlockNums()[2];
        remainder = quotient % (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level).getBlockNums()[1];
        coordinate.setY(remainder);
        quotient = quotient / (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level).getBlockNums()[1];
        remainder = quotient % (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level).getBlockNums()[0];
        coordinate.setZ(remainder);
        return coordinate;
    }

    public int coordinateToId(int level, Coordinate coordinate){
        int num = (int)ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(level + 1).getBlockNums()[0];
        return coordinate.getX() + (num * coordinate.getY()) + (num * num * coordinate.getZ());
    }

    public void insert(Node node,Map<String, Float> property,int id){
        this.childrenNode.put(id,node);
        this.updatePropertyRange(property);
    }

    public long[] createVdbTreeMultiScaleFile(boolean valid, long newFcPositon, FileChannel fc) throws IOException {
        long[] position = new long[2];
        position[0] = newFcPositon;
        fc.position(newFcPositon);
        byte[] T ="T".getBytes(StandardCharsets.ISO_8859_1);
        byte[] F ="F".getBytes(StandardCharsets.ISO_8859_1);
        if(valid){
            fc.write(ByteBuffer.wrap(T));
            if(this.level == -1){
                //根节点不包含数据
                BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(0);
                for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                    fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
                }
            }
            else{
                BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(this.level);
                for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                    fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(blockModelProperty.getSingleBlockPropertyValue(blockModelProperty.getPropertiesName().get(i),this.wHilbertCode))));
                }
            }
        }
        else {
            fc.write(ByteBuffer.wrap(F));
            BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(0);
            for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
            }
        }
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.level)));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(this.wHilbertCode)));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getX())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getY())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getZ())));
        BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(0);
        for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
            PropertyRange propertyRange = this.getPropertyRangeMap().get(blockModelProperty.getPropertiesName().get(i));
            fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(propertyRange.getPropertyMin())));
            fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(propertyRange.getPropertyMax())));
        }
        position[1] = fc.position() + (8 * (this.childrenNode.size() + 1));
        int count = 0;
        for (Map.Entry<Integer, Node> entry : this.childrenNode.entrySet()){
            boolean childValid = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(entry.getValue().getLevel()).getBlockValidity().get(entry.getKey());
            long[] childPosition = entry.getValue().createVdbTreeMultiScaleFile(childValid, position[1], fc);
            fc.position(newFcPositon + 25 + ProcessUtil.propertiesName.size() * 12 + (8 * count));
            fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(childPosition[0])));
            position[1] = childPosition[1];
            count++;
        }
        fc.position(newFcPositon + 25 + ProcessUtil.propertiesName.size() * 12 + (8 * count));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(0)));
        return position;
    }

    public void getNum(Num num){
        num.setNum(num.getNum() + 0);
        for (Map.Entry<Integer, Node> entry : this.childrenNode.entrySet()){
            entry.getValue().getNum(num);
        }
    }

    public Node findNode(long wHCode, int level){
        if(this.level == level){
            return this;
        }
        long parentCode = WHilbertUtil.getParent(ProcessUtil.Lmax, wHCode,this.level + 1,3);
        long id = ProcessUtil.getBlockIdByCode(parentCode,ProcessUtil.blockNums[this.level + 1],ProcessUtil.blockNums[this.level + 1],ProcessUtil.blockNums[this.level + 1]);
        return this.childrenNode.get((int)id).findNode(wHCode,level);
    }
}
