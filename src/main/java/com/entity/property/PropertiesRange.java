package com.entity.property;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PropertiesRange {
    private Map<String, PropertyRange> propertyRangeMap;

    public PropertiesRange(Map<String,Float> property){
        this.propertyRangeMap = new HashMap<>();
        for(Map.Entry<String,Float> entry : property.entrySet()){
            PropertyRange propertyRange = new PropertyRange(entry.getValue(), entry.getValue());
            this.propertyRangeMap.put(entry.getKey(), propertyRange);
        }
    }

    public PropertiesRange(){
        this.propertyRangeMap = new HashMap<>();
    }

    public void merge(PropertiesRange propertiesRange){
        for (Map.Entry<String, PropertyRange> entry : this.propertyRangeMap.entrySet()){
            entry.getValue().merge(propertiesRange.propertyRangeMap.get(entry.getKey()));
        }
    }

    public void merge(Map<String,Float> property){
        for (Map.Entry<String, PropertyRange> entry : this.propertyRangeMap.entrySet()){
            entry.getValue().merge(property.get(entry.getKey()));
        }
    }
}
