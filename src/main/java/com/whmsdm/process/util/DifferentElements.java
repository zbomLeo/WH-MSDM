package com.whmsdm.process.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DifferentElements {
    public static List<Long> findDifferentElements(List<Long> list1, List<Long> list2) {
        Set<Long> set = new HashSet<>();
        set.addAll(list1);
        set.addAll(list2);

        List<Long> result = new ArrayList<>();
        for (Long element : set) {
            if (!(list1.contains(element) && list2.contains(element))) {
                result.add(element);
            }
        }

        return result;
    }
}
