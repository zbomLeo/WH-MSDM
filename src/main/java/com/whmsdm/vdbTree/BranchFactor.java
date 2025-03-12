package com.whmsdm.vdbTree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BranchFactor {
    //分支因子,x代表在x轴方向上将母块分为2^x份
    private int x;
    private int y;
    private int z;

    public int getTotal() {
        return 1<<(x+y+z);
    }

    public BranchFactor merge(BranchFactor branchFactor) {
        return (new BranchFactor(x+branchFactor.x,y+branchFactor.y,z+branchFactor.z));
    }
}
