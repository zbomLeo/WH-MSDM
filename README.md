# WH-MSDM

WH-MSDM is a W-Hilbert curve based multiscale data model for spatial indexing and management of 3D geological blocks in Digital Earth applications.

This repository contains the source code for the paper "WH-MSDM: a W-Hilbert curve-based multiscale data model for spatial indexing and management of 3D geological blocks in Digital Earth applications" by **Genshen Chen et al.** 

Corresponding author: **Prof. Gang Liu (liugang@cug.edu.cn)**

Affiliation: School of Computer Science, China University of Geosciences(Wuhan)

Date: Nov. 17, 2025

## Project structure

```
WH-MSDM/:
├── datas/: [data set]
│    ├── orig/: [original data]
│   	 ├── 32-3Pb-Ts-Ag-fault-stratum.a3d
│   	 ├── 64-3Pb-Ts-Ag-fault-stratum.a3d
│   	 ├── 128-3Pb-Ts-Ag-fault-stratum.a3d
│   	 ├── 256-3Pb-Ts-Ag-fault-stratum.rar
│   	 ├── 512-3Pb-Ts-Ag-fault-stratum.rar
│   	 └── 1024-3Pb-Ts-Ag-fault-stratum.rar
│    ├── genf/: [integrated data]
│    └── stat/: [result statiistics]
├── src/: [Complete program, entity classes, index structures, data models, and test functions]
│    ├── main/com/: [src/main/java]
│    	 ├── entity/:
│    	 ├── process/:
│    	 ├── hilbert/:
│    	 ├── geohash/:
│    	 ├── octree/:
│    	 ├── whmsdm/:
│    	 ├── vdbTree/:
│    	 └── WHilbertUtil.java
│    └── test/com/: [src/test/java]
│	 	 ├── experiment_4_3/: [Benchmark experiments]
│    		 ├── experiment_4_3_1/: [Effect of attribute counts]
│    			 ├── createFileTest.java
│    			 ├── spaceTest.java
│    			 ├── propertyTest.java
│    			 ├── mixedTest.java
│    			 ├── parentTest.java
│    			 ├── childTest.java
│    		 ├── experiment_4_3_2/: [Effect of data size]
│    			 ├── createFileTest.java
│    			 ├── spaceTest.java
│    			 ├── propertyTest.java
│    			 ├── mixedTest.java
│    			 ├── parentTest.java
│    			 ├── childTest.java
│    		 ├── experiment_4_3_3/: [Effect of Lmax]
│    			 ├── createFileTest.java
│    			 ├── spaceTest.java
│    			 ├── propertyTest.java
│    			 ├── mixedTest.java
│    			 ├── parentTest.java
│    			 ├── childTest.java
│    	 └── experiment_4_4/: [Comparison with other methods]
│    		 ├── createFileTest.java
│    		 ├── spaceTest.java
│    		 ├── propertyTest.java
│    		 ├── mixedTest.java
│    		 ├── parentTest.java
│    		 ├── childTest.java
├── images/: [Image set related to theory introduction and experimental results analysis]
├── result/: [Code for processing result datas]
│    └── processing.py
├── pom.xml
└── README.md
```

## Environment settings

The experimental data were derived from a 1:250,000 3D geological structural model of the research area southwest of Guizhou Province. The model was processed through voxel dissection and attribute interpolation to generate 3D geologic block model datasets at four scales, with the software ***QuantyView*** from company named ***Wuhan Dida Quanty Science & Technology Co., Ltd.***, as shown in Figure 1. Table 1 shows total and valid block counts and percentages per scale. Equally partitioning the x, y, and z axes ensures uniform data distribution, reducing biases for fair and comparable experimental results and accurately assessing performance in efficiency, accuracy, and resource usage. All the data used for the experiment is stored under "./datas/orig/". The multiscale integrated dataset generated based on the experimental parameter settings will be stored under "./datas/genf/". All performance test result datasets will be stored under "./datas/stat/". The performance test result datasets will be further processed in "./result/dataprocessing.py" and then used to generate statistical charts using ***Origin*** software.

<img src=".\images\(10) 3D-geological-block-models-at-four-scales.jpg" />

**Figure 1.** 3D geological block models at four scales: (a) Surface model, (b) D32, (c) D64, (d) D128, (e) D256

Experiments measured the impact of attribute dimensions, block sizes, and $L_{max}$ on WH-MSDM's organization and access efficiency. A case study tested WH-MSDM's multiscale 3D geologic block organization across four scales, comparing it with VDB, Geohash, and Octree. The experimental setup includes an Intel(R) Core (TM) i7-9700 CPU @3.00 GHz (8 cores), 64GB RAM, and an NVIDIA Quadro P620 GPU running on Windows 10. The code is implemented in Java using JDK 1.8. 

**Table 1.** Detailed specifications of 3D geological block model datasets at four scales

| Datasets | Num of blocks | Valid blocks | Valid ratio(%) |
| -------- | ------------- | ------------ | -------------- |
| D32      | 32x32x32      | 16406        | 50.06          |
| D64      | 64x64x64      | 126067       | 48.09          |
| D128     | 128x128x128   | 983021       | 46.87          |
| D256     | 256x256x256   | 7743278      | 46.15          |

## Setting the file path 

```java
/* Original data storage location, please change the directory to the original data storage
 * location, please note that the "/" after the directory cannot be omitted 
**/
public static final String A3D_FILE_PATH = "./datas/orig/";


/* The generated file storage location, please make sure the path exists,
 * and note that the "/" after the directory cannot be omitted
**/
public static final String FILE_PATH = "./datas/genf/";

/* The generated statistics file storage location,
 * note that the "\\" after the directory cannot be omitted
**/
public static final String RESULT_PATH = "./data/stat/"
```

## Execution of benchmark experiments

### Effect of attribute counts

1. **Run the `createFileTest` test class** under `com.experiment_4_3.experiment_4_3_1`.  
   Execute `fivePropertyTest()` and `fourPropertyTest()` to generate the following files:  
   - **Files with five properties**:  
     - `W-32-64-128.a3d`, `GeoHash-32-64-128.a3d`, `VdbTree-32-64-128.a3d`, `Octree-32-64-128.a3d`  
   - **Files with four properties**:  
     - `4-W-32-64-128.a3d`, `4-GeoHash-32-64-128.a3d`, `4-VdbTree-32-64-128.a3d`, `4-Octree-32-64-128.a3d`  
   - Index generation times will be printed in the console (corresponding to **Figure 2(a)**).  
   - View the file sizes in the `FILE_PATH` directory (data for **Figure 2(b)**).

2. **Run the `spaceTest` test class** and execute `fiveSpaceQueryTest()` and `fourPropertyTest()` to generate:  
   - **Files**:  
     - `5-spaceQueryStatistics.xlsx`, `4-spaceQueryStatistics.xlsx`  
   - These correspond to the data in **Figures 2(c)(d)**.

3. **Run the `propertyTest` test class** and execute `fiveSpaceQueryTest()` and `fourPropertyTest()` to generate:  
   - **Files**:  
     - `5-singlePropertyQueryStatistics.xlsx`, `4-singlePropertyQueryStatistics.xlsx`  
   - These correspond to the data in **Figures 2(e)(f)**.

4. **Run the `mixedTest` test class** and execute `fiveSpaceQueryTest()` and `fourPropertyTest()` to generate:  
   - **Files**:  
     - `5-mixedQueryStatistics.xlsx`, `4-mixedQueryStatistics.xlsx`  
   - These correspond to the data in **Figures 2(g)(h)**.

5. **Run the `parentTest` test class** and execute `fiveSpaceQueryTest()` and `fourPropertyTest()` to generate:  
   - **Files**:  
     - `5-parentBlockQueryStatistics.xlsx`, `4-parentBlockQueryStatistics.xlsx`  
   - These correspond to the **Table 2 Q<sub>CP</sub>** section.

6. **Run the `childTest` test class** and execute `fiveSpaceQueryTest()` and `fourPropertyTest()` to generate:  
   - **Files**:  
     - `5-childBlockQueryStatistics.xlsx`, `4-childBlockQueryStatistics.xlsx`  
   - These correspond to the **Table 2 Q<sub>CC</sub>** section.
7. The result data is processed in `./result/benchmark.py` and visualized in Origin software.

<img src=".\images\(11) Effect-of-attribute-counts-on-organization-and-access.jpg" />

**Figure 2.** Effect of attribute counts on organization and access: **(a)** Organization time, **(b)** Storage space,
**(c)** Spatial query time, **(d)** Spatial query I/O, **(e)** Attribute query time, **(f)** Attribute query I/O, (g) Hybrid
query time, **(h)** Hybrid query I/O

**Table 2.** Effect of attribute counts on cross-scale query

<table>
  <thead>
    <tr>
      <th rowspan="3">Algorithms</th>
      <th colspan="4">Q<sub>CP</sub></th>
      <th colspan="4">Q<sub>CC</sub></th>
    </tr>
    <tr>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
    </tr>
    <tr>
      <th>A4</th>
      <th>A5</th>
      <th>A4</th>
      <th>A5</th>
      <th>A4</th>
      <th>A5</th>
      <th>A4</th>
      <th>A5</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>WH-MSBM</td>
      <td>0.14</td>
      <td>0.15</td>
      <td>5.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.03</td>
      <td>0.67</td>
      <td>0.80</td>
    </tr>
    <tr>
      <td>VDB</td>
      <td>30.27</td>
      <td>30.12</td>
      <td>19.34</td>
      <td>19.44</td>
      <td>3.98</td>
      <td>3.98</td>
      <td>2.58</td>
      <td>2.58</td>
    </tr>
    <tr>
      <td>GeoHash</td>
      <td>0.07</td>
      <td>0.07</td>
      <td>5.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.02</td>
      <td>0.66</td>
      <td>0.80</td>
    </tr>
    <tr>
      <td>Octree</td>
      <td>1280.71</td>
      <td>1278.37</td>
      <td>14795.31</td>
      <td>15360.27</td>
      <td>21.70</td>
      <td>21.88</td>
      <td>1388.58</td>
      <td>1421.45</td>
    </tr>
  </tbody>
</table>

### Effect of data size on organization and access

1. **Run `createFileTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()` to generate the corresponding index files.
   - The **index files** generated are as follows:
     - For `D32-64`: 
       - `W-32-64.a3d`, `GeoHash-32-64.a3d`, `VdbTree-32-64.a3d`, `Octree-32-64.a3d`
     - For `D64-128`: 
       - `W-64-128.a3d`, `GeoHash-64-128.a3d`, `VdbTree-64-128.a3d`, `Octree-64-128.a3d`
   - The **index file generation time** will be printed on the console, which corresponds to the data in **Figure 12(a)** of the document.
   - The **index files** can be found in the `FILE_PATH` directory.
     - You can view the **space occupied** by each index file, which corresponds to the data in **Figure 12(b)** of the document.
2. **Run `spaceTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()`.
   - The **files** generated will be:
     - `32-spaceQueryStatistics.xlsx`
     - `64-spaceQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 3(c)(d)** of the document.
3. **Run `propertyTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()`.
   - The **files** generated will be:
     - `32-singlePropertyQueryStatistics.xlsx`
     - `64-singlePropertyQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 3(e)(f)** of the document.
4. **Run `mixedTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()`.
   - The **files** generated will be:
     - `32-mixedQueryStatistics.xlsx`
     - `64-mixedQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 3(g)(h)** of the document.
5. **Run `parentTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()`.
   - The **files** generated will be:
     - `32-parentBlockQueryStatistics.xlsx`
     - `64-parentBlockQueryStatistics.xlsx`
   - These files correspond to the **Q<sub>CP</sub>** section in **Table 3** of the document.
6. **Run `childTest`** in `com.experiment_4_3.experiment_4_3_2`:
   - Execute methods `Test_32_64()` and `Test_64_128()`.
   - The **files** generated will be:
     - `32-childBlockQueryStatistics.xlsx`
     - `64-childBlockQueryStatistics.xlsx`
   - These files correspond to the **Q<sub>CC</sub>** section in **Table 3** of the document.
7. The result data is processed in `./result/benchmark.py` and visualized in Origin software.

<img src=".\images\(12) Effect-of-data-size-on-organization-and-access.jpg" />

**Figure 3.** Effect of data size on organization and access: **(a)** Organization time, **(b)** Storage space, **(c)** Spatial query time, (d) Spatial query I/O, **(e)** Attribute query time, **(f)** Attribute query I/O, **(g)** Hybrid query time, **(h)** Hybrid query I/O

**Table 3.** Effect of Data size on cross-scale query

<table>
  <thead>
    <tr>
      <th rowspan="3">Algorithms</th>
      <th colspan="4">Q<sub>CP</sub></th>
      <th colspan="4">Q<sub>CC</sub></th>
    </tr>
    <tr>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
    </tr>
    <tr>
      <th>32-64</th>
      <th>64-128</th>
      <th>32-64</th>
      <th>64-128</th>
      <th>32-64</th>
      <th>64-128</th>
      <th>32-64</th>
      <th>64-128</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>WH-MSBM</td>
      <td>0.08</td>
      <td>0.07</td>
      <td>6.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.02</td>
      <td>0.80</td>
      <td>0.77</td>
    </tr>
    <tr>
      <td>VDB</td>
      <td>26.35</td>
      <td>187.57</td>
      <td>19.25</td>
      <td>123.23</td>
      <td>3.49</td>
      <td>25.26</td>
      <td>2.50</td>
      <td>17.19</td>
    </tr>
    <tr>
      <td>GeoHash</td>
      <td>0.07</td>
      <td>0.07</td>
      <td>6.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.02</td>
      <td>0.80</td>
      <td>0.77</td>
    </tr>
    <tr>
      <td>Octree</td>
      <td>320.55</td>
      <td>2382.78</td>
      <td>2056.81</td>
      <td>14766.12</td>
      <td>6.08</td>
      <td>44.39</td>
      <td>262.70</td>
      <td>2059.59</td>
    </tr>
  </tbody>
</table>

### Effect of L<sub>max</sub> on organization and access

1. **Generate data for Figures 13(a)(b)** from the results obtained in experiments 4_3_1 and 4_3_2.

2. **Run `spaceTest`** in `com.experiment_4_3.experiment_4_3_3`:
   - Execute methods `Test_3()` and `Test_2()`.
   - The **files** generated will be:
     - `3-spaceQueryStatistics.xlsx`
     - `2-spaceQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 4(c)(d)** of the document.

3. **Run `propertyTest`** in `com.experiment_4_3.experiment_4_3_3`:
   - Execute methods `Test_3()` and `Test_2()`.
   - The **files** generated will be:
     - `3-singlePropertyQueryStatistics.xlsx`
     - `2-singlePropertyQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 4(e)(f)** of the document.

4. **Run `mixedTest`** in `com.experiment_4_3.experiment_4_3_3`:
   - Execute methods `Test_3()` and `Test_2()`.
   - The **files** generated will be:
     - `3-mixedQueryStatistics.xlsx`
     - `2-mixedQueryStatistics.xlsx`
   - These files correspond to the data in **Figures 4(g)(h)** of the document.

5. **Run `parentTest`** in `com.experiment_4_3.experiment_4_3_3`:
   - Execute methods `Test_3()` and `Test_2()`.
   - The **files** generated will be:
     - `3-parentBlockQueryStatistics.xlsx`
     - `2-parentBlockQueryStatistics.xlsx`
   - These files correspond to the **Q<sub>CP</sub>** section in **Table 5** of the document.

6. **Run `childTest`** in `com.experiment_4_3.experiment_4_3_3`:
   - Execute methods `Test_3()` and `Test_2()`.
   - The **files** generated will be:
     - `3-childBlockQueryStatistics.xlsx`
     - `2-childBlockQueryStatistics.xlsx`
   - These files correspond to the **Q<sub>CC</sub>** section in **Table 4** of the document.
7. The result data is processed in `./result/benchmark.py` and visualized in Origin software.

<img src=".\images\(13) Effect-of-Lmax-on-organization-and-access.jpg" />

**Figure 4.** Effect of L<sub>max</sub> on organization and access: **(a)** Organization time, **(b)** Storage space, **(c)** Spatial query time, (d) Spatial query I/O, **(e)** Attribute query time, **(f)** Attribute query I/O, **(g)** Hybrid query time, **(h)** Hybrid query I/O

**Table 4.** Effect of L<sub>max</sub> on cross-scale query

<table>
  <thead>
    <tr>
      <th rowspan="3">Algorithms</th>
      <th colspan="4">Q<sub>CP</sub></th>
      <th colspan="4">Q<sub>CC</sub></th>
    </tr>
    <tr>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
      <th colspan="2">Time cost (ms)</th>
      <th colspan="2">I/O cost</th>
    </tr>
    <tr>
      <th>64-128</th>
      <th>32-128</th>
      <th>64-128</th>
      <th>32-128</th>
      <th>64-128</th>
      <th>32-128</th>
      <th>64-128</th>
      <th>32-128</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>WH-MSBM</td>
      <td>0.07</td>
      <td>0.15</td>
      <td>6.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.03</td>
      <td>0.77</td>
      <td>0.80</td>
    </tr>
    <tr>
      <td>VDB</td>
      <td>187.57</td>
      <td>30.12</td>
      <td>123.23</td>
      <td>19.44</td>
      <td>25.26</td>
      <td>3.98</td>
      <td>17.19</td>
      <td>2.58</td>
    </tr>
    <tr>
      <td>GeoHash</td>
      <td>0.07</td>
      <td>0.07</td>
      <td>6.00</td>
      <td>6.00</td>
      <td>0.02</td>
      <td>0.02</td>
      <td>0.77</td>
      <td>0.80</td>
    </tr>
    <tr>
      <td>Octree</td>
      <td>2382.78</td>
      <td>1278.37</td>
      <td>14766.12</td>
      <td>15360.27</td>
      <td>44.39</td>
      <td>21.88</td>
      <td>2059.56</td>
      <td>1421.45</td>
    </tr>
  </tbody>
</table>

## Comparison with other methods

### Sequentially execute the following tests:

**Run `createFileTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **index files** generated will be:
  - `W-32-64-128-256.a3d`
  - `GeoHash-32-64-128-256.a3d`
  - `VdbTree-32-64-128-256.a3d`
  - `Octree-32-64-128-256.a3d`
- The generation time of the index files will be printed in the console.
- The results are processed in `./result/comparison.py` and visualized in Origin software, as shown in **Figure 5**.

<img src=".\images\(14a) Comparison-in-data-organization-performance-time-cost.jpg" style="zoom:13%;" width="450" height="300" />!<img src=".\images\(14b) Comparison-in-data-organization-performance-io-cost.jpg" style="zoom:13%;" width="450" height="300" />

​										**(a) Integration time cost													(b) Integration file size**

**Figure 5.** Comparison in data organization performance

**Table 5.** Comparison in data organization efficiency

| Algorithms  | 32-64 | 32-128 | 32-256 | 32-64 | 32-128 | 32-256 |
| ----------- | ----- | ------ | ------ | ----- | ------ | ------ |
| **WH-MSDM** | 6305  | 40176  | 316716 | 6049  | 49057  | 393121 |
| **VDB**     | 6572  | 47878  | 345504 | 8210  | 65213  | 512768 |
| **Geohash** | 6673  | 68871  | 561118 | 6049  | 49057  | 393121 |
| **Octree**  | 9130  | 44900  | 337907 | 15538 | 120411 | 945404 |

**Run `spaceTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **file** generated will be:
  - `spaceQueryStatistics.xlsx`
- The results are processed in `./result/comparison.py` and visualized in Origin software, as shown in **Figure 6**.


<img src=".\images\(15a) Comparison-in-spatial-query-performance-time-cost.jpg" style="zoom:12%;"  width="450" height="300" />!<img src=".\images\(15b) Comparison-in-spatial-query-performance-io-cost.jpg" style="zoom:12%;" width="450" height="300" />

​										**(a) Q<sub>S </sub> time cost																		  (b) Q<sub>S</sub> I/O cost**

**Figure 6.** Comparison in spatial query performance

**Run `propertyTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **file** generated will be:
  - `singlePropertyQueryStatistics.xlsx`
- The results are processed in `./result/comparison.py` and visualized in Origin software, as shown in **Figure 7**.

<img src=".\images\(16a) Comparison-in-attribute-query-performance-time-cost.jpg" style="zoom:12%;" width="450" height="300" /><img src=".\images\(16b) Comparison-in-attribute-query-performance-io-cost.jpg" style="zoom:12%;" width="450" height="300" />

​										**(a) Q<sub>A</sub> time cost																	(b) Q<sub>A</sub> I/O cost**

**Figure 7.** Comparison in attribute query performance

**Run `mixedTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **file** generated will be:
  - `mixedQueryStatistics.xlsx`
- The results are processed in `./result/comparison.py` and visualized in Origin software, as shown in **Figure 8**.

<img src=".\images\(17a) Comparison-in-hybrid-query-performance-time-cost.jpg" style="zoom:13%;" width="450" height="300" /><img src=".\images\(17b) Comparison-in-hybrid-query-performance-io-cost.jpg" style="zoom:13%;" width="450" height="300" />

​										**(a) Q<sub>H</sub> time cost																		(b) Q<sub>H</sub> I/O cost**

**Figure 8.** Comparison in hybrid query performance

**Run `parentTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **file** generated will be:
  - `parentBlockQueryStatistics.xlsx`
- This file corresponds to the **Q<sub>CP</sub>** section in **Table 6** of the document.

**Run `childTest`** in `com.experiment_4_4`:

- Execute the `test()` method.
- The **file** generated will be:
  - `childBlockQueryStatistics.xlsx`
- This file corresponds to the **Q<sub>CC</sub>** section in **Table 6** of the document.

**Table 6.** Comparison in cross-scale query performance

| Algorithms  | Q<sub>CP</sub>(ms) | I/O      | Q<sub>CC</sub>(ms) | I/O     |
| ----------- | ------------------ | -------- | ------------------ | ------- |
| **WH-MSBM** | 0.06               | 6.01     | 0.02               | 0.91    |
| **VDB**     | 31.48              | 22.45    | 3.86               | 2.99    |
| **GeoHash** | 0.06               | 6.01     | 0.02               | 0.91    |
| **Octree**  | 5568.76            | 99100.96 | 100.33             | 7618.33 |

