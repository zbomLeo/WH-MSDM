import os
import pandas as pd

pd.set_option('display.float_format', lambda x: '%.3f' % x)
def writefile(df, resultPath, sheetName):
    """
    Write a DataFrame to a specified Excel file and sheet.

    If the target Excel file already exists, append a new sheet to it.
    If the target Excel file does not exist, create a new Excel file and add a new sheet.

    Parameters:
    df (DataFrame): The DataFrame to be written to the Excel file.
    resultPath (str): The path to the target Excel file.
    sheetName (str): The name of the sheet to be created in the Excel file.
    
    Returns:
    None
    """
    # Check if the target Excel file exists
    if os.path.isfile(resultPath):
        # If the file exists, open it in append mode
        with pd.ExcelWriter(resultPath, mode='a', engine='openpyxl') as writer:
            # Write the DataFrame to the specified sheet
            df.to_excel(writer, sheet_name=sheetName, index=True)
    else:
        # If the file does not exist, write the DataFrame to a new Excel file
        df.to_excel(resultPath, sheet_name=sheetName, index=True)

def writefile(df, resultPath, sheetName):
    """
    Write a DataFrame to a specified Excel file and sheet.

    If the target Excel file already exists, append a new sheet to it.
    If the target Excel file does not exist, create a new Excel file and add a new sheet.

    Parameters:
    df (DataFrame): The DataFrame to be written to the Excel file.
    resultPath (str): The path to the target Excel file.
    sheetName (str): The name of the sheet to be created in the Excel file.
    
    Returns:
    None
    """
    # Check if the target Excel file exists
    if os.path.isfile(resultPath):
        # If the file exists, open it in append mode
        with pd.ExcelWriter(resultPath, mode='a', engine='openpyxl') as writer:
            # Write the DataFrame to the specified sheet
            df.to_excel(writer, sheet_name=sheetName, index=True)
    else:
        # If the file does not exist, write the DataFrame to a new Excel file
        df.to_excel(resultPath, sheet_name=sheetName, index=True)

def swap(a, b, df):
    # Check if a and b are within the valid range
    if not (0 <= a < len(df)) or not (0 <= b < len(df)):
        raise IndexError("Indices out of range")

    # Check if a and b are integers
    if not isinstance(a, int) or not isinstance(b, int):
        raise TypeError("Indices must be integers")

    try:
        # Get the current index list
        idx = df.index.tolist()
        
        # Swap the indices
        idx[a], idx[b] = idx[b], idx[a]
        
        # Reindex the DataFrame in place
        df = df.reindex(idx)
        
        # Return the modified DataFrame
        return df
    except Exception as e:
        # Exception handling, print error message
        print(f"An error occurred: {e}")
        return df  # Return the original DataFrame to avoid data loss

def cal(filepath, name):
    # Validate the file path to prevent path injection attacks
    if not filepath.endswith('.xlsx') and not filepath.endswith('.xls'):
        raise ValueError("Invalid file path: only .xlsx and .xls files are supported")
    
    try:
        # Read the Excel file
        df = pd.read_excel(filepath, sheet_name=0)
        
        # Filter rows where WResultSize is not zero
        df = df[df['WResultSize'] != 0]
        
        # Drop columns that are entirely NaN
        df = df.dropna(axis=1, how='all')
        
        # Rename columns for better readability
        column_mapping = {
            'Single Query Range': 'SQR', 
            'WTimes': 'WT',
            'OctreeTimes': 'OT',
            'VdbTimes': 'VDBT',
            'VdbWTimes': 'VDBWT',
            'GeoHashTimes': 'GHT',
            'WResultSize': 'WRS',
            'OctreeResultSize': 'ORS',
            'VdbResultSize': 'VDBRS',
            'VdbWResultSize': 'VDBWRS',
            'GeoHashResultSize': 'GHRS',
            'WIOs': 'WIO',
            'OctreeIOs': 'OIO',
            'VdbIOs': 'VDBIO',
            'VdbWIOs': 'VDBWIO',
            'GeoHashIOs': 'GHIO'
        }
        df = df.rename(columns=column_mapping)
        
        # Define the columns needed for mean calculation
        required_columns = ['WRS', 'WT', 'OT', 'VDBT', 'VDBWT', 'GHT', 'WIO', 'OIO', 'VDBIO', 'VDBWIO', 'GHIO']
        
        # Validate that all required columns are present in the DataFrame
        if not all(col in df.columns for col in required_columns):
            raise ValueError(f"Missing required columns: {set(required_columns) - set(df.columns)}")
        
        # Calculate the mean of the required columns
        df_mean = df[required_columns].mean()
        
        # Normalize the mean values by dividing by WRS
        df_mean = df_mean / df_mean['WRS']
        
        # Create the time result DataFrame
        time_columns = ['WT', 'OT', 'VDBT', 'VDBWT', 'GHT']
        dfT = df_mean[time_columns].rename({
            'WT': 'WH-MSBM',
            'OT': 'Octree',
            'VDBT': 'VDB',
            'VDBWT': 'WHVDB',
            'GHT': 'GeoHash',
        }).to_frame(name=name)
        
        # Create the I/O result DataFrame
        io_columns = ['WIO', 'OIO', 'VDBIO', 'VDBWIO', 'GHIO']
        dfIO = df_mean[io_columns].rename({
            'WIO': 'WH-MSBM',
            'OIO': 'Octree',
            'VDBIO': 'VDB',
            'VDBWIO': 'WHVDB',
            'GHIO': 'GeoHash',
        }).to_frame(name=name)
        
        return dfT, dfIO
    
    except FileNotFoundError:
        raise FileNotFoundError(f"File not found: {filepath}")
    except pd.errors.EmptyDataError:
        raise pd.errors.EmptyDataError(f"File is empty: {filepath}")
    except Exception as e:
        raise Exception(f"An error occurred: {e}")

if __name__ = '__main__':
    # Example usage
    filepath4 = './datas/Attributes/4/spaceQueryStatistics.xlsx'
    df4T, df4IO = cal(filepath4,'A4')
    filepath5='./datas/Attributes/5/spaceQueryStatistics.xlsx'
    df5T, df5IO = cal(filepath5,'A5')
    dfT = pd.concat([df4T,df5T], axis=1)
    dfIO = pd.concat([df4IO,df5IO], axis=1)
    df = pd.concat([dfT,dfIO], axis=1)
    df = swap(1,2,df)
    df = swap(2,3,df)
    df = swap(2,4,df)
    writefile(df,'./AC.xlsx','QS')

    # Example usage
    filepathS='./datas/Attributes/4/singlePropertyQueryStatistics.xlsx'
    dfST, dfSIO = cal(filepathS,'A4')
    filepathB='./datas/Attributes/5/singlePropertyQueryStatistics.xlsx'
    dfBT, dfBIO = cal(filepathB,'A5')
    dfT = pd.concat([dfST,dfBT], axis=1)
    dfIO = pd.concat([dfSIO,dfBIO], axis=1)
    df = pd.concat([dfT,dfIO], axis=1)
    df = swap(1,2,df)
    df = swap(2,3,df)
    df = swap(2,4,df)
    writefile(df,'./AC.xlsx','QA')