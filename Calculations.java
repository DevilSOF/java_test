import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    List<String> uniquePairs = new ArrayList<String>();

    for (int i = 1; i < rows; i++) {
      uniquePairs.add(csvData[0][i] + csvData[1][i]);
    }

    uniquePairs = uniquePairs.stream().distinct().collect(Collectors.toList());

    int mm  = 0;
    for (String pair : uniquePairs) {

      List<List<String>> tmpData = new ArrayList<List<String>>();

      for (int i = 0; i < cols; i++) {
        List<String> subList = new ArrayList<String>();
        subList.add(csvData[i][0]);
        tmpData.add(subList);
      }

      for (int i = 1; i < rows; i++) {
        if (pair.equals(csvData[0][i] + csvData[1][i])) {
          for (int j = 0; j < cols; j++) {
            tmpData.get(j).add(csvData[j][i]);
          }
        }
      }

      int tmpCols = tmpData.size();

      Thread[] statInfoThreads = new Thread[5];
      StatInfo[] myRunnableStatInfos = new StatInfo[5];
      for (int j = 2; j < tmpCols; j++) {
        myRunnableStatInfos[j - 2] = new StatInfo(tmpData.get(j), tmpData.get(0).get(1), tmpData.get(1).get(1));
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

  /**
   * write array to csv file.
   * @param dataOut input array of data.
   */
  private static void arrayToCsv(final String[] dataOut) {
    BufferedWriter br;
    String[] headers = {"well", "stratum", "median", "minValue", "maxValue", "sumValue", "column"};
    File csvFileOut = new File(csvOutFileName);
    boolean headersWrite = false;

    if (!csvFileOut.exists()) {
      headersWrite = !headersWrite;
    }

    String delimiter = ";";
    try {
      br = new BufferedWriter(new FileWriter(csvOutFileName, true));

      if (headersWrite) {
        for (int i = 0; i < headers.length; i++) {
          delimiter = (i == headers.length - 1) ? "\n" : delimiter;
          br.write(headers[i] + delimiter);
        }
      }

      delimiter = ";";
      for (int i = 0; i < dataOut.length; i++) {
        delimiter = (i == dataOut.length - 1) ? "\n" : delimiter;
        br.write(dataOut[i].replace(".", ",") + delimiter);
      }

      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * write csv file to array 2D.
   * @param path path to csv file.
   * @return array2d in variable.
   */
  private static String[][] csvToArray2d(final String path) {
    BufferedReader br;
    String line;
    String delimiter = ";";
    List<List<String>> list = new ArrayList<List<String>>();

    try {
      br = new BufferedReader(new FileReader(path));
      line = br.readLine();
      String[] headers = line.split(delimiter);

      for (String header : headers) {
        List<String> subList = new ArrayList<String>();
        subList.add(header);
        list.add(subList);
      }

      while ((line = br.readLine()) != null) {
        String[] elems = line.split(";");
        for (int i = 0; i < elems.length; i++) {
          list.get(i).add(elems[i]);
        }
      }

      br.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    int cols = list.size();
    int rows = list.get(0).size();
    String[][] array2d = new String[cols][rows];

    for (int col = 0; col < cols; col++) {
      for (int row = 0; row < rows; row++) {
        array2d[col][row] = list.get(col).get(row).replace(",", ".");
      }
    }

    return array2d;
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
  private List<String> data;

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
  public StatInfo(final List<String> dataIn, final String wellIn, final String stratumIn) {
    this.data = dataIn;
    this.well = wellIn;
    this.stratum = stratumIn;
  }

  /**
   * procedure for median, min, max, sum metods.
   */
  public void run() {
    double tmpValue = Double.valueOf(data.get(1));
    double minValue = 0;
    double maxValue = 0;
    BigDecimal sumValue = BigDecimal.valueOf(0);
    BigDecimal median = BigDecimal.valueOf(0);
    int count = 0;

    for (int i = 1; i < data.size(); i++) {
      if (data.get(i).replace(" ", "").equals("0")) {
        continue;
      }

      count++;

      tmpValue = Double.valueOf(data.get(i));

      sumValue = sumValue.add(BigDecimal.valueOf(tmpValue));

      if (tmpValue < minValue || minValue == 0) {
        minValue = tmpValue;
      }

      if (tmpValue > maxValue) {
        maxValue = tmpValue;
      }
    }

    median = (count != 0 ? (sumValue.divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP)) : BigDecimal.valueOf(0));

    results[0] = well;
    results[1] = stratum;

    results[2] = String.valueOf(median);
    results[3] = (minValue != 0.0 ? String.valueOf(minValue) : "0");
    results[4] = (maxValue != 0.0 ? String.valueOf(maxValue) : "0");
    results[5] = String.valueOf(sumValue);

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

    String column = data.get(0);
    results[6] = column;
  }

  private static boolean valueInCharArray(final char[] charArray, final int length) {
    return (String.valueOf(charArray[length - 2]).equals(".")
            && String.valueOf(charArray[length - 1]).equals("0"));
  }
}
