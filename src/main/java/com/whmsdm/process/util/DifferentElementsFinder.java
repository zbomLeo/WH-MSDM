package com.whmsdm.process.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DifferentElementsFinder {

    public static Map<Long, String> findDifferentElements(List<Long> list1, List<Long> list2) {
        Map<Long, String> map = new HashMap<>();

        for (Long num : list1) {
            map.put(num, "list1");
        }

        for (Long num : list2) {
            if (map.containsKey(num)) {
                map.put(num, "both");
            } else {
                map.put(num, "list2");
            }
        }
        return map;
    }
}

