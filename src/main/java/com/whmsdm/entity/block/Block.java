package com.whmsdm.entity.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Block {
    private int level;
    private long WHCode;
    public String toString() {
        return "level: " + level + ", WHCode: " + WHCode;
    }
}
