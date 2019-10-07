import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

  /**
   * csvData - temporary variable.
   */
  static String[][] csvData;

  /**
   * Main program.
   *
   * @param args .
   */
  public static void main(final String[] args) {
    String csvInFileName = "test_data.csv";
    String csvOutFileName = "test_data_out.csv";

    csvData = csvToArray2d(csvInFileName);

    File csvFileOut = new File(csvOutFileName);

    if (csvFileOut.exists()) {
      csvFileOut.delete();
    }

    int rows = csvData[0].length;
    int cols = csvData.length;
    int count = 0;
    long timerStart = System.currentTimeMillis();
    List<String> uniquePairs = new ArrayList<String>();

    for (int i = 1; i < rows; i++) {
      uniquePairs.add(csvData[0][i] + csvData[1][i]);
      count += 1;
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
      int tmpRows = tmpData.get(0).size();

      System.out.println("pair " + pair + " " + tmpRows + " rows" + " " + tmpCols + " cols");

      for (int j = 2; j < tmpCols; j++) {
        System.out.println(tmpData.get(j).get(0)
                          + " "
                          + Arrays.toString(statInfo(tmpData.get(j),
                                                      tmpData.get(0).get(1),
                                                      tmpData.get(1).get(1))));

        arrayToCsv(statInfo(tmpData.get(j),
                               tmpData.get(0).get(1),
                               tmpData.get(1).get(1)));
      }

      mm++;
      System.out.println();
    }

    System.out.println(mm + " all massives");

    long timerEnd = System.currentTimeMillis();

    System.out.println("ms = " + (timerEnd - timerStart));
    System.out.println("count = " + count);
    System.out.println(rows + "\n" + cols);
  }

  /**
   * Stat info.
   * @param data incoming massive.
   * @param well id well.
   * @param stratum id stratum.
   * @return  results array of data.
   */
  public static String[] statInfo(final List<String> data, final String well, final String stratum) {
    double tmpValue = Double.valueOf(data.get(1));
    String[] results = new String[7];

    double minValue = 0;
    double maxValue = 0;
    double sumValue = 0;
    double median;
    int count = 0;

    for (int i = 1; i < data.size(); i++) {
      count++;
      tmpValue = Double.valueOf(data.get(i));
      sumValue += tmpValue;

      if (tmpValue != 0 && tmpValue < minValue || minValue == 0) {
        minValue = tmpValue;
      }

      if (tmpValue > maxValue) {
        maxValue = tmpValue;
      }
    }

    median = (sumValue != 0 ? (sumValue / count) : sumValue);

    results[0] = well;
    results[1] = stratum;
    results[2] = String.valueOf(median);
    results[3] = String.valueOf(minValue);
    results[4] = String.valueOf(maxValue);
    results[5] = String.valueOf(sumValue);

    String column = data.get(0);
    results[6] = column;

    return results;
  }

  /**
   * write array to csv file.
   * @param dataOut input array of data.
   */
  public static void arrayToCsv(final String[] dataOut) {
    BufferedWriter br;
    String csvOutFileName = "test_data_out.csv";
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
  public static String[][] csvToArray2d(final String path) {
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

    // rows and cols names are shuffled
    int rows = list.size();
    int cols = list.get(0).size();
    String[][] array2d = new String[rows][cols];
    // System.out.println(rows + " - rows" + "\n" + cols + " - cols" + "\n");
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        array2d[row][col] = list.get(row).get(col).replace(",", ".");
      }
    }

    return array2d;
  }
}
