package com.entity.property;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PropertyRange {
    private float propertyMin;
    private float propertyMax;

    public boolean intersect(PropertyRange range){
        return propertyMin <= range.getPropertyMax() && propertyMax >= range.getPropertyMin();
    }
    public boolean contain(PropertyRange range){
        return propertyMin <= range.getPropertyMin() && propertyMax >= range.getPropertyMax();
    }

    public boolean contain(float property){
        return propertyMin <= property && propertyMax >= property;
    }

    public void merge(PropertyRange range){
        propertyMin = Math.min(propertyMin, range.getPropertyMin());
        propertyMax = Math.max(propertyMax, range.getPropertyMax());
    }

    public void merge(float property){
        propertyMin = Math.min(propertyMin, property);
        propertyMax = Math.max(propertyMax, property);
    }

    public String toString(){
        return propertyMin + "-" + propertyMax;
    }
}
