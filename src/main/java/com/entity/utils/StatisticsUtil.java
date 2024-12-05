package com.entity.utils;

import com.WHilbertUtil;
import com.entity.coor.Box;
import com.entity.coor.Coordinate;
import com.entity.property.PropertyRange;
import com.entity.VO.MixedQueryVO;
import com.entity.VO.SinglePropertyQueryVO;
import com.entity.VO.SpaceQueryVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AllArgsConstructor
@Getter
@Setter
public class StatisticsUtil {
    private List<SpaceQueryVO> spaceQueryVOS;
    private List<SinglePropertyQueryVO> singlePropertyQueryVOS;
    private List<MixedQueryVO> mixedQueryVOS;
    private List<Long> WHCodes;
    private List<Integer> level;
    private List<Long> WTimes;
    private List<Long> OctreeTimes;
    private List<Long> VdbTimes;
    private List<Long> GeoHashTimes;
    private List<Integer> WResultSize;
    private List<Integer> OctreeResultSize;
    private List<Integer> VdbResultSize;
    private List<Integer> GeoHashResultSize;
    private List<Integer> WIOs;
    private List<Integer> OctreeIOs;
    private List<Integer> VdbIOs;
    private List<Integer> GeoHashIOs;

    private static final String[] PROPERTY_NAMES = {"Pb", "TS", "Ag", "地层"};
    private static final float PB_MIN = 0.0f;
    private static final float PB_MAX = 19.9975f;
    private static final float AG_MIN = 0.0f;
    private static final float AG_MAX = 42.5f;
    private static final float TS_MIN = 0.0f;
    private static final float TS_MAX = 13.6f;
    private static final float STRATA_MIN = 1.0f;
    private static final float STRATA_MAX = 29.0f;


    public StatisticsUtil() {
        spaceQueryVOS = new ArrayList<>();
        singlePropertyQueryVOS = new ArrayList<>();
        mixedQueryVOS = new ArrayList<>();
        WHCodes = new ArrayList<>();
        level = new ArrayList<>();
        WTimes = new ArrayList<>();
        OctreeTimes = new ArrayList<>();
        VdbTimes = new ArrayList<>();
        GeoHashTimes = new ArrayList<>();
        WResultSize = new ArrayList<>();
        OctreeResultSize = new ArrayList<>();
        VdbResultSize = new ArrayList<>();
        GeoHashResultSize = new ArrayList<>();
        WIOs = new ArrayList<>();
        OctreeIOs = new ArrayList<>();
        VdbIOs = new ArrayList<>();
        GeoHashIOs = new ArrayList<>();
    }

    public static MixedQueryVO randomMixedQueryVO(int corRange){
        SpaceQueryVO spaceQueryVO = randomSpaceQueryVO(corRange);
        SinglePropertyQueryVO singlePropertyQueryVO = randomStratumPropertyQueryVO();
        return new MixedQueryVO(spaceQueryVO, singlePropertyQueryVO);
    }

    public static SinglePropertyQueryVO randomStratumPropertyQueryVO(){
        SinglePropertyQueryVO queryVO = new SinglePropertyQueryVO();
        queryVO.setPropertyName("地层");
        queryVO.setPropertyRanges(generateRandomPropertyRanges(queryVO.getPropertyName()));
        return queryVO;
    }

    public static List<PropertyRange> generateRandomPropertyRanges(String propertyName) {
        List<PropertyRange> ranges = new ArrayList<>();
        Random random = new Random();

        float min = 0.0f;
        float max = 0.0f;

        switch (propertyName) {
            case "Pb":
                min = PB_MIN;
                max = PB_MAX;
                break;
            case "TS":
                min = TS_MIN;
                max = TS_MAX;
                break;
            case "Ag":
                min = AG_MIN;
                max = AG_MAX;
                break;
            case "地层":
                min = STRATA_MIN;
                max = STRATA_MAX;
                break;
        }

        // 生成随机范围个数
        int numRanges = random.nextInt(3) + 1; // Randomly choose 1 to 3 ranges

        // 生成范围
        while (ranges.size() < numRanges) {
            float start = min + random.nextFloat() * (max - min);
            float end = min + random.nextFloat() * (max - min);

            // 确保范围不交叉且不包含
            if (start < end && !hasOverlapOrContainment(ranges, start, end)) {
                ranges.add(new PropertyRange(start, end));
            }
        }

        return ranges;
    }

    // 检查新生成的范围与已有范围是否有交叉或包含关系
    public static boolean hasOverlapOrContainment(List<PropertyRange> ranges, float newStart, float newEnd) {
        for (PropertyRange range : ranges) {
            if ((newStart >= range.getPropertyMin() && newStart <= range.getPropertyMax()) ||
                    (newEnd >= range.getPropertyMin() && newEnd <= range.getPropertyMax()) ||
                    (range.getPropertyMin() >= newStart && range.getPropertyMin() <= newEnd) ||
                    (range.getPropertyMax() >= newStart && range.getPropertyMax() <= newEnd)) {
                return true;
            }
        }
        return false;
    }

    public static SpaceQueryVO randomSpaceQueryVO(int corRange){
        SpaceQueryVO spaceQueryVO = new SpaceQueryVO();
        Random random = new Random();
        Coordinate coordinateMin = new Coordinate();
        Coordinate coordinateMax = new Coordinate();
        int[] min = new int[3];
        int[] max = new int[3];
        for (int i = 0; i < 3; i++) {
            int a = random.nextInt(corRange);
            int b = random.nextInt(corRange);
            if(a > b){
                int temp = a;
                a = b;
                b = temp;
            }
            min[i] = a;
            max[i] = b;
        }
        coordinateMin.setX(min[0]);
        coordinateMin.setY(min[1]);
        coordinateMin.setZ(min[2]);
        coordinateMax.setX(max[0]);
        coordinateMax.setY(max[1]);
        coordinateMax.setZ(max[2]);
        spaceQueryVO.setBox(new Box(coordinateMin,coordinateMax));
        return spaceQueryVO;
    }

    //多个层级均等概率
    public static long randomWHCode(int Lmax, int N, int LOffset){
        Random random = new Random();
        int level = random.nextInt(Lmax + 1);
        int num = (int) Math.pow(2,Lmax + LOffset);
        long[] coordinate = new long[N];
        for (int i = 0; i < N; i++) {
            long a = random.nextInt(num);
            coordinate[i] = a;
        }
        return WHilbertUtil.encode(Lmax,level,N,coordinate,LOffset);
    }

    public void writeExcel(String queryType, String path, MethodUseRecorder recorder, String fileNamePrefix) {
        String sheetStr;
        String queryRangeStr;
        String excelNameStr;
        int size;
        switch (queryType) {
            case "SpaceQuery" :
                sheetStr = "空间查询";
                queryRangeStr = "Space Query Range";
                excelNameStr = "spaceQueryStatistics.xlsx";
                size = this.spaceQueryVOS.size();
                break;
            case "SinglePropertyQuery":
                sheetStr = "属性查询";
                queryRangeStr = "Single Property Query Range";
                excelNameStr = "singlePropertyQueryStatistics.xlsx";
                size = this.singlePropertyQueryVOS.size();
                break;
            case "MixedQuery":
                sheetStr = "混合查询";
                queryRangeStr = "Mixed Query Range";
                excelNameStr = "mixedQueryStatistics.xlsx";
                size = this.mixedQueryVOS.size();
                break;
            case "ParentBlockQuery":
                sheetStr = "父块查询";
                queryRangeStr = "Parent Block Query Range";
                excelNameStr = "parentBlockQueryStatistics.xlsx";
                size = this.WHCodes.size();
                break;
            case "ChildBlockQuery":
                sheetStr = "子块查询";
                queryRangeStr = "Child Block Query Range";
                excelNameStr = "childBlockQueryStatistics.xlsx";
                size = this.WHCodes.size();
                break;
            default:
                return;
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetStr);

        //写入表头
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue(queryRangeStr);
        int colNum = 1;
        for(String key : recorder.methodNames) {
            headerRow.createCell(colNum++).setCellValue(key+"Time");
            headerRow.createCell(colNum++).setCellValue(key+"ResultSize");
            headerRow.createCell(colNum++).setCellValue(key+"IOs");
        }

        // 写入数据
        int rowNum = 1;

        for (int i = 0; i < size; i++) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            String queryStr;
            switch (queryType) {
                case "SpaceQuery" :
                    queryStr = this.spaceQueryVOS.get(i).toString();
                    break;
                case "SinglePropertyQuery":
                    queryStr = this.singlePropertyQueryVOS.get(i).toString();
                    break;
                case "MixedQuery":
                    queryStr = this.mixedQueryVOS.get(i).toString();
                    break;
                case "ParentBlockQuery":
                    queryStr = this.WHCodes.get(i).toString();
                    break;
                case "ChildBlockQuery":
                    queryStr = this.WHCodes.get(i).toString();
                    break;
                default:
                    return;
            }
            row.createCell(0).setCellValue(queryStr);
            int col = 1;
            for (String key : recorder.methodNames) {
                switch (key){
                    case "WH-MSDM":
                        row.createCell(col++).setCellValue(this.WTimes.get(i));
                        row.createCell(col++).setCellValue(this.WResultSize.get(i));
                        row.createCell(col++).setCellValue(this.WIOs.get(i));
                        break;
                    case "Octree":
                        row.createCell(col++).setCellValue(this.OctreeTimes.get(i));
                        row.createCell(col++).setCellValue(this.OctreeResultSize.get(i));
                        row.createCell(col++).setCellValue(this.OctreeIOs.get(i));
                        break;
                    case "VDB":
                        row.createCell(col++).setCellValue(this.VdbTimes.get(i));
                        row.createCell(col++).setCellValue(this.VdbResultSize.get(i));
                        row.createCell(col++).setCellValue(this.VdbIOs.get(i));
                        break;
                    case "GeoHash":
                        row.createCell(col++).setCellValue(this.GeoHashTimes.get(i));
                        row.createCell(col++).setCellValue(this.GeoHashResultSize.get(i));
                        row.createCell(col++).setCellValue(this.GeoHashIOs.get(i));
                        break;
                }
            }

        }

        // 保存工作簿到文件
        try (FileOutputStream fileOut = new FileOutputStream(path + fileNamePrefix + excelNameStr))  {
            workbook.write(fileOut);
            // 关闭工作簿
            workbook.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static long getQueryVOByExcel(String path, int i) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(i + 1);
        Cell cell = row.getCell(0);
        return Long.parseLong(cell.getStringCellValue());
    }
}
