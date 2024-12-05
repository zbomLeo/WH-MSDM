package com.entity.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class PropertyMapping<K, V> {

    private Map<String, List<Map>> propertyMap;

    public PropertyMapping(String path, List<String> propertiesName) {
        this.propertyMap = new HashMap<>();
        for (String propertyName : propertiesName) {
            String filePath = path + "\\" + propertyName + ".txt";
            if (Files.notExists(Paths.get(filePath))) {
                continue;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"))) {
                String line;
                reader.readLine();reader.readLine();
                Map<K, V> keyToValue = new HashMap<>();
                Map<V, K> valueToKey = new HashMap<>();
                while ((line = reader.readLine()) != null) {
                    String[] splits = line.split("\t");
                    if (splits.length < 2) {
                        continue;
                    }
                    K split = (K) splits[0];
                    keyToValue.put((K) splits[0], (V) splits[1]);
                    valueToKey.put((V) splits[1], (K) splits[0]);
                }
                List<Map> list = new ArrayList<>();
                list.add(keyToValue);list.add(valueToKey);
                this.propertyMap.put(propertyName, list);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据属性值获取属性code
     * @param propertyName
     * @param value
     * @return
     */
    public K valueToKey(String propertyName, V value) {
        return (K) propertyMap.get(propertyName).get(0).get(value);
    }

    /**
     * 根据属性code获取属性值
     * @param propertyName
     * @param key
     * @return
     */
    public V keyToValue(String propertyName, K key) {
        return (V) propertyMap.get(propertyName).get(1).get(key);
    }
}
