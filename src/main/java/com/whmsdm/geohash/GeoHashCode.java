package com.whmsdm.geohash;

import com.whmsdm.WHilbertUtil;
import com.whmsdm.process.util.ProcessUtil;

public class GeoHashCode {
    /*编码*/
    public static String encode(int L, long[] cor) {
        String[] binaryStrings = new String[3];

        // 将每个 long 转换为二进制字符串并用零填充
        for (int i = 0; i < 3; i++) {
            String binary = Long.toBinaryString(cor[i]);
            binaryStrings[i] = String.format("%" + (L + ProcessUtil.LOffset) + "s", binary).replace(' ', '0');
        }

        StringBuilder combinedString = new StringBuilder();

        // 按特定的顺序组合字符串
        for (int i = 0; i < binaryStrings[0].length(); i++) {
            combinedString.append(binaryStrings[2].charAt(i));
            combinedString.append(binaryStrings[1].charAt(i));
            combinedString.append(binaryStrings[0].charAt(i));
        }

        StringBuilder decimalString = new StringBuilder();

        // 将二进制字符串转化为十进制字符串
        int length = combinedString.length();
        for (int i = 0; i < length; i += 3) {
            String tripleBinary = combinedString.substring(i, i + 3);
            int decimalValue = Integer.parseInt(tripleBinary, 2);
            decimalString.append(decimalValue);
        }

        return decimalString.toString();
    }


    /*解码*/
    public static long[] decode(String geoHashCode) {
        StringBuilder combinedBinary = new StringBuilder();

        // 将十进制字符串拆分为二进制字符串
        for (char ch : geoHashCode.toCharArray()) {
            String binaryString = Integer.toBinaryString(Character.getNumericValue(ch));
            combinedBinary.append(String.format("%3s", binaryString).replace(' ', '0'));
        }

        // 初始化xyz二进制字符串
        StringBuilder x = new StringBuilder();
        StringBuilder y = new StringBuilder();
        StringBuilder z = new StringBuilder();

        // 拆分二进制字符串，赋值给xyz
        for (int i = 0; i < combinedBinary.length(); i += 3) {
            z.append(combinedBinary.charAt(i));
            y.append(combinedBinary.charAt(i + 1));
            x.append(combinedBinary.charAt(i + 2));
        }

        // 将二进制字符串转化为长整型
        long[] cor = new long[3];
        cor[0] = Long.parseLong(x.toString(), 2);
        cor[1] = Long.parseLong(y.toString(), 2);
        cor[2] = Long.parseLong(z.toString(), 2);

        return cor;
    }


    /*计算层级*/
    public static int calculateLevel(String geoHashCode){
        return geoHashCode.length() - ProcessUtil.LOffset;
    }


    /*W-hilbert编码转geoHash编码*/
    public static String WHCodeToGeoHash(long WHCode, int Lmax){
        long[] cor = WHilbertUtil.decode(Lmax, WHCode, 3,ProcessUtil.LOffset);
        int L = WHilbertUtil.getLevel(Lmax, WHCode, 3);
        return GeoHashCode.encode(L, cor);
    }


    /*geoHash编码转存储位置loc*/
    public static long getLoc(String geoHashCode){
        int level = GeoHashCode.calculateLevel(geoHashCode);
        long result = 0;
        for (int i = 0; i <= ProcessUtil.Lmax; i++) {
            if(i < level){
                String code = geoHashCode.substring(0, i + ProcessUtil.LOffset);
                result += Integer.parseInt(code, 8);
                if(geoHashCode.charAt(i + ProcessUtil.LOffset) > '3') result++;
            }
            else if(i == level) result += (Integer.parseInt(geoHashCode, 8) + 1);
            else {
                String code = ProcessUtil.padWithZeros(geoHashCode, i + ProcessUtil.LOffset);
                result += Integer.parseInt(code, 8) + ((long)Math.pow(8, i - level) / 2);
            }
        }
        return result - 1;
    }


    /*loc转GeoHash编码,level固定传0*/
    public static String getCodeByLoc(long loc, int level){
        if(level == ProcessUtil.Lmax){
            return String.valueOf(loc);
        }
        else {
            long dividend = ((long)Math.pow(8, ProcessUtil.Lmax - level + 1) - 1) / 7;
            long quotient = loc / dividend;
            long remainder = loc % dividend;
            if(remainder == (dividend - 1) / 2){
                if(level == 0) return String.format("%" + ProcessUtil.LOffset + "s", Long.toOctalString(quotient)).replace(' ', '0');
                return Long.toOctalString(quotient);
            }
            else {
                if(remainder > (dividend - 1) / 2) remainder--;
                String s = getCodeByLoc(remainder, level + 1);
                if(level == 0) return String.format("%" + ProcessUtil.LOffset + "s", Long.toOctalString(quotient)).replace(' ', '0') + s;
                return Long.toOctalString(quotient) + s;
            }
        }
    }


    /*获取块体在PL层级的父块编码*/
    public static String getParentCode(String geoHashCode, int PL){
        int level = GeoHashCode.calculateLevel(geoHashCode);
        if(level == 0) return null;
        return geoHashCode.substring(0, PL + ProcessUtil.LOffset);
    }


    /*获取块体在CL层级的子块编码*/
    public static String[] getChildCode(String geoHashCode, int CL){
        int level = GeoHashCode.calculateLevel(geoHashCode);
        if(level == ProcessUtil.Lmax) return null;
        int dif = (int)Math.pow(8, CL - level);
        String[] result = new String[dif];
        for (int i = 0; i < dif; i++) {
            String octalString = Integer.toOctalString(i);
            octalString = String.format("%" + (CL - level) + "s", octalString).replace(' ', '0');
            result[i] = geoHashCode + octalString;
        }
        return result;
    }
}
