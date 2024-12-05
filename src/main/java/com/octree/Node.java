package com.octree;


import com.entity.constants.A3DFileConstants;
import com.entity.coor.Coordinate;
import com.process.util.ParserUtil;
import com.process.util.ProcessUtil;
import com.process.util.RWUtils;
import com.vdbTree.BlockModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Node {
    private int level;
    private long wHilbertCode;
    private Coordinate origin;
    private Node[] childrenNode;
    private boolean valid;

    public Node(int level, long wHilbertCode, int id, boolean valid){

        this.level = level;
        this.wHilbertCode = wHilbertCode;
        this.origin = getOrigin(level, id);
        this.valid = valid;
        if(level != ProcessUtil.octree.getTreeHeight() - 1){
            this.childrenNode = new Node[8];
        }
    }

    public Coordinate getOrigin(int level, int id){
        Coordinate coordinate = new Coordinate(0,0,0);
        int num = (1 << level);
        int range = (1<<(ProcessUtil.octree.getTreeHeight() - level - 1));
        int remainder = id % num;
        coordinate.setX(remainder * range);
        int quotient = id / num;
        remainder = quotient % num;
        coordinate.setY(remainder * range);
        quotient = quotient / num;
        remainder = quotient % num;
        coordinate.setZ(remainder * range);
        return coordinate;
    }

    public Node getChild(int level,long wHilbertCode,int id){
        int subscript = -1;
        Coordinate coordinate = this.idToCoordinate(level,id);
        int difference = 1 << (level - this.level - 1);
        coordinate.setX(coordinate.getX() / difference);
        coordinate.setY(coordinate.getY() / difference);
        coordinate.setZ(coordinate.getZ() / difference);
        subscript = (coordinate.getX() % 2) + (2 * (coordinate.getY() % 2)) + (4 * (coordinate.getZ() % 2));
        if(this.childrenNode[subscript] == null){
            int num = (1 << (this.level + 1));
            int id2 = this.coordinateToId(this.level + 1,coordinate);
            long code = ProcessUtil.blockEncode(id2,this.level - ProcessUtil.LOffset + 1, num, num, num);
            Node node;
            if((this.level + 1) <= (ProcessUtil.LOffset - 1)){
                node = new Node(this.level + 1, code, id2, true);
            }
            else {
                node = new Node(this.level + 1, code, id2, false);
            }
            this.childrenNode[subscript] = node;
        }
        return this.childrenNode[subscript];
    }

    public Coordinate idToCoordinate(int level, int id){
        Coordinate coordinate = new Coordinate(0,0,0);
        int num = (1 << level);
        int remainder = id % num;
        coordinate.setX(remainder);
        int quotient = id / num;
        remainder = quotient % num;
        coordinate.setY(remainder);
        quotient = quotient / num;
        remainder = quotient % num;
        coordinate.setZ(remainder);
        return coordinate;
    }

    public int coordinateToId(int level, Coordinate coordinate){
        int num = (1 << level);
        return coordinate.getX() + (num * coordinate.getY()) + (num * num * coordinate.getZ());
    }

    public void addChild(Node node, int id){
        int subscript = -1;
        Coordinate coordinate = this.idToCoordinate(node.getLevel(),id);
        subscript = (coordinate.getX() % 2) + (2 * (coordinate.getY() % 2)) + (4 * (coordinate.getZ() % 2));
        this.childrenNode[subscript] = node;
    }
    public long[] createOctreeMultiScaleFile(long newFcPositon, FileChannel fc) throws IOException{
        long[] position = new long[2];
        position[0] = newFcPositon;
        fc.position(newFcPositon);
        byte[] T ="T".getBytes(StandardCharsets.ISO_8859_1);
        byte[] F ="F".getBytes(StandardCharsets.ISO_8859_1);
        if(this.valid){
            fc.write(ByteBuffer.wrap(T));
            if(this.level >= ProcessUtil.LOffset){
                BlockModelProperty blockModelProperty = ProcessUtil.octree.getMsbmProperty().getBlockModelPropertyMap().get(this.level - ProcessUtil.LOffset);
                for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                    fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(blockModelProperty.getSingleBlockPropertyValue(blockModelProperty.getPropertiesName().get(i),this.wHilbertCode))));
//                    fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(getBlockProperty(
//                            blockModelProperty.getPropertiesName().get(i),
//                            ProcessUtil.getBlockIdByCode(this.wHilbertCode, blockModelProperty.getBlockNums()[0], blockModelProperty.getBlockNums()[1], blockModelProperty.getBlockNums()[2]),
//                            blockModelProperty.getFc(),
//                            this.level - ProcessUtil.LOffset
//                    ))));
                }
            }
            else {
                BlockModelProperty blockModelProperty = ProcessUtil.octree.getMsbmProperty().getBlockModelPropertyMap().get(0);
                for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                    fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
                }
            }
        }
        else{
            fc.write(ByteBuffer.wrap(F));
            BlockModelProperty blockModelProperty = ProcessUtil.octree.getMsbmProperty().getBlockModelPropertyMap().get(0);
            for (int i = 0; i < blockModelProperty.getPropertiesName().size(); i++) {
                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
            }
        }
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.level)));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(this.wHilbertCode)));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getX())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getY())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getZ())));
        if(this.childrenNode != null){
            position[1] = fc.position() + 64;
            for (int i = 0; i < 8; i++) {
                if(this.childrenNode[i] != null){
                    long[] childPosition = this.childrenNode[i].createOctreeMultiScaleFile(position[1],fc);
                    fc.position(newFcPositon + 25 + (ProcessUtil.octree.getMsbmProperty().getBlockModelPropertyMap().get(0).getPropertiesName().size() * 4) + (i * 8));
                    fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(childPosition[0])));
                    position[1] = childPosition[1];
                }
                else {
                    fc.position(newFcPositon + 25 + (ProcessUtil.octree.getMsbmProperty().getBlockModelPropertyMap().get(0).getPropertiesName().size() * 4) + (i * 8));
                    fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(0)));
                }
            }
        }
        else {
            for (int i = 0; i < 8; i++) {
                fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(0)));
            }
            position[1] = fc.position();
        }
        return position;
    }

    public static float getBlockProperty(String propertyName, long id, FileChannel fc, int level){
        long num = (long) Math.pow(2, level + ProcessUtil.LOffset);  // 一个方向上的块体数
        long n = (long) Math.pow(num, 3);  // 该层级总块体数
        long index = ProcessUtil.propertiesName.indexOf(propertyName);  // 属性的索引值
        try{
            fc.position(A3DFileConstants.FIRST_PROPERTY_POSITION + n * (index * 4 + 1) + 4 * id);
            return (float) RWUtils.read(fc, 4, "float_reverse");
        }catch (Exception e){
            e.printStackTrace();
            return 0.0f;
        }
    }

}
