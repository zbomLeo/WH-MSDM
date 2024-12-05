package com.entity.VO;

import com.entity.property.PropertyRange;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SinglePropertyQueryVO {
    private String propertyName;
    private List<PropertyRange> propertyRanges;

    public boolean contain(PropertyRange propertyRange) {
        for (PropertyRange range : propertyRanges) {
            if (range.contain(propertyRange)) {
                return true;
            }
        }
        return false;
    }

    public boolean contain(float pValue){
        for (PropertyRange queryPropertyRange : this.propertyRanges){
            if(queryPropertyRange.contain(pValue)){
                return true;
            }
        }
        return false;
    }

    public boolean intersect(PropertyRange propertyRange) {
        for (PropertyRange range : propertyRanges) {
            if (range.intersect(propertyRange)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(propertyName).append(": ");
        for (PropertyRange range : propertyRanges) {
            sb.append(range.toString()).append(" ");
        }
        return sb.toString();
    }

    public static SinglePropertyQueryVO fromString(String str) {
        String[] parts = str.split(": ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("String format is invalid");
        }

        String propertyName = parts[0];
        String rangesStr = parts[1];

        List<PropertyRange> propertyRanges = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+)-(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(rangesStr);
        while (matcher.find()) {
            float propertyMin = Float.parseFloat(matcher.group(1));
            float propertyMax = Float.parseFloat(matcher.group(2));
            propertyRanges.add(new PropertyRange(propertyMin, propertyMax));
        }

        return new SinglePropertyQueryVO(propertyName, propertyRanges);
    }

    public static SinglePropertyQueryVO getQueryVOByExcel(String path, int i) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(i + 1);
        Cell cell = row.getCell(0);
        return fromString(cell.getStringCellValue());
    }
}
