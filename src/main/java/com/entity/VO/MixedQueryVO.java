package com.entity.VO;

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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MixedQueryVO {
    private SpaceQueryVO spaceQueryVO;
    private SinglePropertyQueryVO singlePropertyQueryVO;

    public String toString(){

        return "SpaceQueryVO: " + spaceQueryVO.toString() + "---" +
                "SinglePropertyQueryVO: " + singlePropertyQueryVO.toString();
    }

    public static MixedQueryVO fromString(String str, float times) {
        String[] parts = str.split("---");
        if (parts.length != 2) {
            throw new IllegalArgumentException("String format is invalid");
        }

        String spaceQueryVOStr = parts[0].substring("SpaceQueryVO: ".length());
        String singlePropertyQueryVOStr = parts[1].substring("SinglePropertyQueryVO: ".length());

        SpaceQueryVO spaceQueryVO = SpaceQueryVO.fromString(spaceQueryVOStr, times);
        SinglePropertyQueryVO singlePropertyQueryVO = SinglePropertyQueryVO.fromString(singlePropertyQueryVOStr);

        return new MixedQueryVO(spaceQueryVO, singlePropertyQueryVO);
    }

    public static MixedQueryVO getQueryVOByExcel(String path, int i, float times) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(i + 1);
        Cell cell = row.getCell(0);
        return fromString(cell.getStringCellValue(), times);
    }
}
