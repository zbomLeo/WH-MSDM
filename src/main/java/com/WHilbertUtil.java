package com.;
import com.entity.block.Value;
import com.hilbert.HilbertCurve;
import com.hilbert.SmallHilbertCurve;
import com.process.util.ProcessUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WHilbertUtil {
    
    private static SmallHilbertCurve hilbertCurve = HilbertCurve.small().bits(21).dimensions(3);

    public static long encode(int Lmax, int L, int N, long[] cor, int LOffset) {
        // 1、初始化Hilbert曲线的参数
        hilbertCurve.setBits(L + LOffset);
        hilbertCurve.setLength(N * (L + LOffset));
        // 2、计算单尺度Hilbert编码
        long hCode = hilbertCurve.index(cor);
        // 3、计算L层间隔及第一个编码
        long interval = getInterval(Lmax, L, N);
        long firstCode = getFirstCode(Lmax, L, N);
        // 4、计算多尺度w编码
        return hCode * interval + firstCode;
    }

    public static long[] decode(int Lmax, long WHCode, int N, int LOffset) {
        // 1、计算层级L
        int L = getLevel(Lmax, WHCode, N);
        // 2、初始化Hilbert曲线的参数
        hilbertCurve.setBits(L + LOffset);
        hilbertCurve.setLength(N * (L + LOffset));
        // 3、转换为单尺度Hilbert编码
        long HCode = (WHCode - getFirstCode(Lmax, L, N)) >> (N * (Lmax - L) + 1);
        // 4、转化为三维坐标
        long[] point = hilbertCurve.point(HCode);
        return point;
    }

    public static long getInterval(int Lmax, int L, int N) {
        return 2L * (1L << (N * (Lmax - L)));
    }

    public static long getFirstCode(int Lmax, int L, int N) {
        return (1L << (N * (Lmax - L))) - 1;
    }

    public static int getLevel(int Lmax, long WHCode, int N) {
        if ((WHCode & 1) == 0) {
            return Lmax;
        }
        long code = (WHCode - 1) ^ (WHCode + 1);
        int i = 0;
        while ((code >> i) != 1) {
            i++;
        }
        return (N * Lmax + 1 - i) / N;
    }

    public static long getParent(int Lmax, long WHCode,int PL, int N) {
        // 1.计算WHCode在第PL层的偏移量
        int n = (N * (Lmax - PL)) + 1;
        long offset = WHCode >> n << n;
        // 2.计算父码 firstPL + offset
        return WHilbertUtil.getFirstCode(Lmax, PL, N) + offset;
    }

    public static long[] getChildren(int Lmax, long WHCode,int CL, int N) {
        // 1.计算层级L
        int L = WHilbertUtil.getLevel(Lmax, WHCode, N);
        // 2.计算L层和CL层首位编码差值
        long offset = WHilbertUtil.getFirstCode(Lmax, L, N) - WHilbertUtil.getFirstCode(Lmax, CL, N);
        // 3.计算子码范围
        long min = WHCode - offset;
        long max = WHCode + offset;
        // 4.计算子码
//        long[] children = new long[1 << (N * (L - CL))];
        long[] children = new long[1 << (N * (CL - L))];
//        long interval = WHilbertUtil.getInterval(Lmax, L, N);
        long interval = WHilbertUtil.getInterval(Lmax, CL, N);
        int k = 0;
        for (long i = min; i <= max ; i+=interval) {
            children[k++] = i;
        }
        return children;
    }
    private final static int[] neb = new int[]{-1, 0 ,1};

    public static long[] getNeighbor(int Lmax, long WHCode, int N, int LOffset) {
        // 1.计算层级L
        int L = WHilbertUtil.getLevel(Lmax, WHCode, N);
        // 2.解码为坐标
        long[] cor = WHilbertUtil.decode(Lmax, WHCode, N, LOffset);
        // 3.计算邻居坐标
        long[] nebCodes = new long[(int) (Math.pow(3,N) - 1)];
        List<long[]> nebCors = new ArrayList<>();
        dfs(cor, N, nebCors, 0, 0);
        // 4.对坐标编码
        int i = 0;
        for (long[] nebCor : nebCors) {
            nebCodes[i++] = WHilbertUtil.encode(Lmax, L, N, nebCor, LOffset);
        }
        return nebCodes;
    }

    public static void dfs(long[] cor, int N, List<long[]> ans, int i, int sum) {
        if (i >= N) {
            if (sum != 0) {
                ans.add(Arrays.copyOf(cor, cor.length));
            }
            return;
        }
        for (int k : neb) {
            cor[i] += k;
            dfs(cor, N, ans, i + 1, sum + Math.abs(k));
            cor[i] -= k;
        }

        // for (int j = 0; j < neb.length; j++) {
        //     cor[i] += neb[j];
        //     dfs(cor, N, ans, i+1, sum + Math.abs(neb[j]));
        //     cor[i] -= neb[j];
        // }
    }

    public static boolean isWHCode(long WHCode, int Lmax, int N) {
        if((WHCode & 1) == 0){
            return true;
        }
        for (int i = 0; i <= Lmax; i++) {
            if(WHCode >= getFirstCode(Lmax,i,N)){
                if(((WHCode - getFirstCode(Lmax,i,N)) % getInterval(Lmax,i,N)) == 0){
                    return true;
                }
            }
        }
        return false;
    }

    public static long getLocByCode(long WHCode, int Lmax, int N){
        long loc = 0;
        for (int i = 0; i <= Lmax; i++) {
            long firstCode = getFirstCode(Lmax, i, N);
            long interval = getInterval(Lmax, i, N);
            if(WHCode >= firstCode){
                loc += ((WHCode - firstCode) / interval) + 1;
            }
        }
        return loc - 1;
    }

    public static long getCodeByLoc(long loc, int Lmax, int N, int level){
        if(level == Lmax){
            return 2L * loc;
        }
        else {
            long num = (long) Math.pow(2, N);  // 相邻尺度下，一个父块划分为子块数
            long dividend = ((long)Math.pow(num, Lmax - level + 1) - 1) / (num - 1);
            long quotient = loc / dividend;
            long remainder = loc % dividend;
            if(remainder == (dividend - 1) / 2){
                long firstCode = getFirstCode(Lmax, level,N);
                long code = firstCode + quotient * 2 * (long)Math.pow(num, Lmax - level);
                return code;
            }
            else {
                if(remainder > (dividend - 1) / 2) remainder--;
                long code = getCodeByLoc(remainder, Lmax, N, level + 1);
                return code + quotient * 2 * (long)Math.pow(num,Lmax - level);
            }
        }
    }

    public static long getIdByCode(long code, int LOffset, int Lmax, int N){
        long[] cor = decode(Lmax, code, N, LOffset);
        int level = getLevel(Lmax,code, N);
        long num = (long) Math.pow(2,level + LOffset);
        return cor[0] + cor[1] * num + cor[2] * num * num;
    }

    public static long getCodeById(long id, int LOffset, int Lmax, int N, int level){
        long num = (long) Math.pow(2, level + LOffset);
        long[] cor = new long[3];
        cor[0] = (id % (num * num)) % num;
        cor[1] = (id % (num * num)) / num;
        cor[2] = id / (num * num);
        return encode(Lmax, level, N, cor, LOffset);
    }

    public static long getCodeByHilbertAndLevel(long hCode, int L, int Lmax, int N){
        long interval = getInterval(Lmax, L, N);
        long firstCode = getFirstCode(Lmax, L, N);
        return hCode * interval + firstCode;
    }

    public static long getCodeByUCode(long UCode, int Lmax, int N, int LOffset){
        int level = 0;
        long n = 0L;
        long hCode;
        while(true){
            long num = (long) Math.pow(2, level + LOffset);  // 当前层级，一个方向上的块体数量
            n += (long) Math.pow(num, N);  // 当前层级，块体总数
            if(UCode < n){
                hCode = UCode - (n - (long) Math.pow(num, N));
                break;
            }
            level++;
        }
        return getCodeByHilbertAndLevel(hCode, level, Lmax, N);
    }

    public static long getHCodeByCodeAndLevel(long WHCode, int L, int Lmax, int N){
        return (WHCode - getFirstCode(Lmax, L, N)) >> (N * (Lmax - L) + 1);
    }

    public static long getIdxLocByCode(long WHCode, int Lmax, int N){
        long loc = 0;
        for (int i = 0; i < Lmax; i++) {
            long firstCode = getFirstCode(Lmax, i, N);
            long interval = getInterval(Lmax, i, N);
            if(WHCode >= firstCode){
                loc += ((WHCode - firstCode) / interval) + 1;
            }
        }
        return loc - 1;
    }

    public static Value getCodeAndLevelById(int id, int level){
        if(level == ProcessUtil.Lmax){
            return new Value(level, 2 * id);
        }
        else {
            int dividend = ((int)Math.pow(8, ProcessUtil.Lmax - level + 1) - 1) / 7;
            int quotient = id / dividend;
            int remainder = id % dividend;
            if(remainder == (dividend - 1) / 2){
                long firstCode = WHilbertUtil.getFirstCode(ProcessUtil.Lmax,level,3);
                long code = firstCode + quotient * 2 * (long)Math.pow(8,ProcessUtil.Lmax - level);
                return new Value(level, code);
            }
            else {
                if(remainder > (dividend - 1) / 2) remainder--;
                Value value = getCodeAndLevelById(remainder,level + 1);
                long code = value.getCode() + quotient * 2 * (long)Math.pow(8,ProcessUtil.Lmax - level);
                return new Value(value.getLevel(), code);
            }
        }
    }
}
