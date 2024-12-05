package com.entity.VO;

import com.entity.coor.Box;
import com.entity.coor.Coordinate;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SpaceQueryVO {
    private Box box;

    public String toString(){
        return box.toString();
    }

    public static SpaceQueryVO fromString(String str, float times) {
        Pattern pattern = Pattern.compile("Box\\{coordinateMin=\\{x=(\\d+), y=(\\d+), z=(\\d+)\\}, coordinateMax=\\{x=(\\d+), y=(\\d+), z=(\\d+)\\}\\}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            Coordinate coordinateMin = new Coordinate(
                    (int) (Integer.parseInt(matcher.group(1)) * times),
                    (int) (Integer.parseInt(matcher.group(2)) * times),
                    (int) (Integer.parseInt(matcher.group(3)) * times)
            );
            Coordinate coordinateMax = new Coordinate(
                    (int) (Integer.parseInt(matcher.group(4)) * times),
                    (int) (Integer.parseInt(matcher.group(5)) * times),
                    (int) (Integer.parseInt(matcher.group(6)) * times)
            );
            Box box = new Box(coordinateMin, coordinateMax);
            return new SpaceQueryVO(box);
        } else {
            throw new IllegalArgumentException("String format is invalid");
        }
    }

    public static SpaceQueryVO getQueryVOByExcel(String path, int i, float times) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(i + 1);
        Cell cell = row.getCell(0);
        return fromString(cell.getStringCellValue(), times);
    }
}
