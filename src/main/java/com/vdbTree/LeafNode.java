package com.vdbTree;

import com.entity.coor.Coordinate;
import com.entity.VO.ResultVO;
import com.entity.VO.SpaceQueryVO;
import com.octree.Num;
import com.process.util.ParserUtil;
import com.process.util.ProcessUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class LeafNode extends Node{
    public LeafNode(int level, int wHilbertCode, Coordinate origin){
        super(level, wHilbertCode, origin);
    }
    public void DFS(ResultVO resultVO){
        resultVO.getResult().get(this.level).add((long)this.wHilbertCode);
    }

    public void multiScaleSpaceQuery(SpaceQueryVO spaceQueryVO, ResultVO resultVO){
        if(spaceQueryVO.getBox().contain(this.origin)){
            resultVO.getResult().get(this.level).add((long)this.wHilbertCode);
        }
    }

    public long[] createVdbTreeMultiScaleFile(boolean valid, long newFcPositon, FileChannel fc) throws IOException {
        long[] position = new long[2];
        position[0] = newFcPositon;
        fc.position(newFcPositon);
        byte[] T ="T".getBytes(StandardCharsets.ISO_8859_1);
        byte[] F ="F".getBytes(StandardCharsets.ISO_8859_1);
        if(valid){
            fc.write(ByteBuffer.wrap(T));
            BlockModelProperty blockModelProperty = ProcessUtil.vdbTree.getMsbmProperty().getBlockModelPropertyMap().get(this.level);
            for (int i = 0; i < ProcessUtil.propertiesName.size(); i++) {
                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(blockModelProperty.getSingleBlockPropertyValue(blockModelProperty.getPropertiesName().get(i),this.wHilbertCode))));
            }
        }
        else {
            fc.write(ByteBuffer.wrap(F));
            for (int i = 0; i < ProcessUtil.propertiesName.size(); i++) {
                fc.write(ByteBuffer.wrap(ParserUtil.float2Byte(0.0f)));
            }
        }
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.level)));
        fc.write(ByteBuffer.wrap(ParserUtil.long2Byte(this.wHilbertCode)));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getX())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getY())));
        fc.write(ByteBuffer.wrap(ParserUtil.int2Byte(this.origin.getZ())));
        position[1] = fc.position();
        return position;
    }

    public void getNum(Num num) {
        num.setNum(num.getNum() + 1);
    }

    public Node findNode(long wHCode, int level){
        return this;
    }
}
