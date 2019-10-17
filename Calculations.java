import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Set;

public class Calculations {

  /**
   * csvData - temporary variable.
   */
  private static String[][] csvData;

  /**
   * csvOutFileName - out csv file name variable.
   */
  private static String csvOutFileName;

  /**
   * Main program.
   *
   * @param args can be add two in and out files with .csv extensions.
   * @throws Exception - for Threads join.
   */
  public static void main(final String[] args) throws Exception {
    String csvInFileName;
    String inputParamsErrorMessage = "Only in and out csv files";
    String inputParamContains = ".csv";

    if (args.length == 0) {
      csvInFileName = "test_data.csv";
      csvOutFileName = "test_data_out.csv";
    } else {
      if (args.length == 2) {
        for (String arg : args) {
          if (!arg.contains(inputParamContains)) {
            System.out.println(inputParamsErrorMessage);
            return;
          }
        }
        csvInFileName = args[0];
        csvOutFileName = args[1];
      } else {
        System.out.println(inputParamsErrorMessage);
        return;
      }
    }

    csvData = csvToArray2d(csvInFileName);

    File csvFileOut = new File(csvOutFileName);

    if (csvFileOut.exists()) {
      csvFileOut.delete();
    }

    int rows = csvData[0].length;
    int cols = csvData.length;
    long timerStart = System.currentTimeMillis();
    String[] uniquePairs = new String[rows];


    for (int i = 0; i < rows; i++) {
      uniquePairs[i] = csvData[0][i] + csvData[1][i];
    }

    uniquePairs = uniqueArrayHash(uniquePairs);
    System.out.println(System.currentTimeMillis() - timerStart + " ms unique");
    int mm  = 0;
    for (int n = 1; n < uniquePairs.length; n++) {
      String[][] tmpData;

      int tmpCount = 0;
      for (int i = 1; i < rows; i++) {
        if (uniquePairs[n].equals(csvData[0][i] + csvData[1][i])) {
          tmpCount++;
        }
      }

      tmpData = new String[cols][tmpCount + 1];

      for (int k = 0; k < cols; k++) {
        tmpData[k][0] = csvData[k][0];
      }

      int tmpArrayRow = 1;
      for (int i = 1; i < rows; i++) {
        if (uniquePairs[n].equals(csvData[0][i] + csvData[1][i])) {
          for (int j = 0; j < cols; j++) {
            tmpData[j][tmpArrayRow] = csvData[j][i];
          }
          tmpArrayRow++;
        }
      }

      int tmpCols = tmpData.length;

      Thread[] statInfoThreads = new Thread[5];
      StatInfo[] myRunnableStatInfos = new StatInfo[5];
      for (int j = 2; j < tmpCols; j++) {
        myRunnableStatInfos[j - 2] = new StatInfo(tmpData[j], tmpData[0][1], tmpData[1][1]);
        statInfoThreads[j - 2] = new Thread(myRunnableStatInfos[j - 2]);
        statInfoThreads[j - 2].start();
      }

      for (Thread thread : statInfoThreads) {
        thread.join();
      }

      for (StatInfo myRunnableStatInfo : myRunnableStatInfos) {
        arrayToCsv(myRunnableStatInfo.getResults());
      }
      mm++;
    }

    System.out.println("\n" + mm + " all massives");

    long timerEnd = System.currentTimeMillis();
    System.out.println("ms = " + (timerEnd - timerStart));
  }

  private static boolean isUnique(String[] array, String num) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(num)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tested results 9-20 ms.
   * @param arrays - input array.
   * @return arraysToString.
   */
  private static String[] uniqueArrayHash(final String[] arrays) {
    Set<String> uniqueArray = new LinkedHashSet<String>();

    for (String array : arrays) {
      uniqueArray.add(array);
    }

    String[] arraysToString = new String[uniqueArray.size()];

    int count = 0;
    for (String unique : uniqueArray) {
      arraysToString[count++] = unique;
    }

    return arraysToString;
  }

  /**
   * Very slow solution tested 50-70 ms vs uniqueArrayHash 10-20 ms.
   * @param arrays - input massive.
   * @return uniqueArray.
   */
  private static String[] uniqueArray(final String[] arrays) {
    String[] elements = new String[arrays.length];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = "";
    }

    int count = 0;
    for (int i = 0; i < arrays.length; i++) {
      if (isUnique(elements, arrays[i])) {
        elements[count++] = arrays[i];
      }
    }

    String[] uniqueArray = new String[count];

    for (int i = 0; i < uniqueArray.length; i++) {
      uniqueArray[i] = elements[i];
    }

    return uniqueArray;
  }

  /**
   * write array to csv file.
   * @param dataOut input array of data.
   */
  private static void arrayToCsv(final String[] dataOut) {
    BufferedWriter bw = null;
    String[] headers = {"well", "stratum", "median", "minValue", "maxValue", "sumValue", "column"};
    File csvFileOut = new File(csvOutFileName);
    boolean headersWrite = false;

    if (!csvFileOut.exists()) {
      headersWrite = !headersWrite;
    }

    String delimiter = ";";
    try {
      bw = new BufferedWriter(new FileWriter(csvOutFileName, true));

      if (headersWrite) {
        for (int i = 0; i < headers.length; i++) {
          delimiter = (i == headers.length - 1) ? "\r\n" : delimiter;
          bw.write(headers[i] + delimiter);
        }
      }

      delimiter = ";";
      for (int i = 0; i < dataOut.length; i++) {
        delimiter = (i == dataOut.length - 1) ? "\r\n" : delimiter;
        bw.write(dataOut[i].replace(".", ",") + delimiter);
      }

      bw.close();
    } catch (Exception e) {
      System.out.println("Write error or file not found.");
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          System.out.println("Reader close error.");
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
  }

  private static String[][] inputArray;

  private static void setInputArraySize(final int[] value) {
    inputArray = new String[value[0]][value[1]];
  }

  /**
   * write csv file to array 2D.
   * @param path path to csv file.
   * @return array2d in variable.
   */
  private static String[][] csvToArray2d(final String path) {
    BufferedReader br = null;
    String line;
    String delimiter = ";";

    setInputArraySize(takeArraySize(path, delimiter));

    int count = 0;
    try {
      br = new BufferedReader(new FileReader(path));

      line = br.readLine();
      if (line != null) {
        setArrayVariables(line, delimiter, count);
        count++;
      }

      while ((line = br.readLine()) != null) {
        setArrayVariables(line, delimiter, count);
        count++;
      }

    } catch (Exception e) {
      System.out.println("Read error or file not found.");
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          System.out.println("Reader close error.");
          e.printStackTrace();
          System.exit(1);
        }
      }
    }

    return inputArray;
  }

  private static void setArrayVariables(final String line, String delimiter, int count) {
    String[] elements = line.split(delimiter);

    for (int i = 0; i < elements.length; i++) {
      inputArray[i][count] = elements[i].replace(",", ".");
    }
  }

  private static int[] takeArraySize(final String path, final String delimiter) {
    BufferedReader br = null;
    int[] size = {0, 0};
    String line;

    try {
      br = new BufferedReader(new FileReader(path));

      line = br.readLine();

      if (line != null) {
        String[] elements = line.split(delimiter);

        for (int i = 0; i < elements.length; i++) {
          size[0]++;
        }

        size[1]++;
      }

      while ((line = br.readLine()) != null) {
        size[1]++;
      }

    } catch (Exception e) {
      System.out.println("Read error or file not found (array length).");
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          System.out.println("Reader close error (array length).");
          e.printStackTrace();
          System.exit(1);
        }
      }
    }

    return size;
  }

}

class StatInfo implements Runnable {

  /**
   * results - array of all calculations.
   */
  private String[] results = new String[7];

  /**
   * data - incoming data for calculations.
   */
  // private List<String> data;
  private String[] data;

  /**
   * well - current well column name.
   */
  private String well;

  /**
   * statum - current stratum column name.
   */
  private String stratum;

  public String[] getResults() {
    return results;
  }

  /**
   * Runnable setter for variables.
   * @param dataIn    input data for thread.
   * @param wellIn    input well name for thread.
   * @param stratumIn input well name for stratum.
   */
  public StatInfo(final String[] dataIn, final String wellIn, final String stratumIn) {
    this.data = dataIn;
    this.well = wellIn;
    this.stratum = stratumIn;
  }

  /**
   * procedure for median, min, max, sum metods.
   */
  public void run() {
    double tmpValue = Double.valueOf(data[1]);
    double minValue = 0;
    double maxValue = 0;
    double sumValue = 0;
    double median = 0;
    int count = 0;
    int dotMaxLength = 0;
    int dotTempValue = 0;

    for (int i = 1; i < data.length; i++) {
      if (data[i].replace(" ", "").equals("0")) {
        continue;
      }

      count++;

      dotTempValue = dotValueLength(String.valueOf(data[i]));

      if (dotMaxLength < dotTempValue) {
        dotMaxLength = dotTempValue;
      }

      tmpValue = Double.valueOf(data[i]);

      sumValue += tmpValue;

      if (tmpValue < minValue || minValue == 0) {
        minValue = tmpValue;
      }

      if (tmpValue > maxValue) {
        maxValue = tmpValue;
      }
    }

    median = (count != 0 ? (sumValue / count) : 0);

    results[0] = well;
    results[1] = stratum;

    DecimalFormat df = new DecimalFormat(setPattern(dotMaxLength));
    df.setRoundingMode(RoundingMode.HALF_UP);
    results[2] = zeroCheckReplace(df.format(median));

    results[3] = zeroCheckReplace(String.valueOf(minValue));
    results[4] = zeroCheckReplace(String.valueOf(maxValue));

    results[5] = zeroCheckReplace(df.format(sumValue));

    String[] tempResults = new String[4];
    for (int i = 0; i < tempResults.length; i++) {
      tempResults[i] = results[i + 2];

      String tempStringValue = tempResults[i];
      char[] tempStringChars = tempStringValue.toCharArray();
      int maxLength = tempStringChars.length;

      if (maxLength < 2) {
        continue;
      }

      if (valueInCharArray(tempStringChars, maxLength)) {
        results[i + 2] = tempStringValue.replace(".0", "");
      } else {
        results[i + 2] = tempStringValue;
      }
    }

    String column = data[0];
    results[6] = column;

  }

  private static String zeroCheckReplace(final String value) {
    if (!value.equals("0.0") && !value.equals("0,")) {
      return value;
    } else {
      return "0";
    }
  }

  private static String setPattern(final int value) {
    String pattern = "#.";

    for (int i = 0; i < value; i++) {
      pattern += "#";
    }

    return pattern;
  }

  private static int dotValueLength(final String value) {
    int dotLength = value.length() - (value.indexOf(".") + 1);

    return dotLength;
  }

  private static boolean valueInCharArray(final char[] charArray, final int length) {
    return (String.valueOf(charArray[length - 2]).equals(".")
            && String.valueOf(charArray[length - 1]).equals("0"));
  }
}
