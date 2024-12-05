package com.entity.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Value {
    private int level;
    private long code;

    public String toString() {
        return "level: " + level + ", code: " + code;
    }
}
