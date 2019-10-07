import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;
import static java.util.stream.Collectors.toList;

public class Test{
  static String[][] csv_data;
  public static void main(String[] args) {
    String csv_in = "test_data.csv";
    String csv_out = "test_data_out.csv";

    csv_data = csv_to_array2d(csv_in);

    File csv_file_out = new File(csv_out);

    if (csv_file_out.exists()) csv_file_out.delete();

    int rows = csv_data[0].length;
    int cols = csv_data.length;
    int count = 0;
    long t_start = System.currentTimeMillis();
    List<String> unique_pairs = new ArrayList<String>();

    for (int i = 1; i < rows; i++) {
      unique_pairs.add(csv_data[0][i] + csv_data[1][i]);
      count += 1;
    }

    unique_pairs = unique_pairs.stream().distinct().collect(Collectors.toList());

    int mm  = 0;
    for (String pair : unique_pairs) {

      List<List<String>> tmp_data = new ArrayList<List<String>>();

      for (int i = 0; i < cols; i++) {
        List<String> subList = new ArrayList<String>();
        subList.add(csv_data[i][0]);
        tmp_data.add(subList);
      }

      for (int i = 1; i < rows; i++) {
        if (pair.equals(csv_data[0][i] + csv_data[1][i])){
          for (int j = 0; j < cols; j++) {
            tmp_data.get(j).add(csv_data[j][i]);
          }
        }
      }

      int tmp_cols = tmp_data.size();
      int tmp_rows = tmp_data.get(0).size();

      System.out.println("pair " + pair + " " + tmp_rows + " rows" + " " + tmp_cols + " cols");

      for (int j = 2; j < tmp_cols; j++) {
       System.out.println(tmp_data.get(j).get(0)
                          + " "
                          + Arrays.toString(stat_info(tmp_data.get(j),
                                                          tmp_data.get(0).get(1),
                                                          tmp_data.get(1).get(1))));

       array_to_csv(stat_info(tmp_data.get(j),
                                  tmp_data.get(0).get(1),
                                  tmp_data.get(1).get(1)));
      }
        mm++;
        System.out.println();
    }

    System.out.println(mm + " all massives");

    long t_end = System.currentTimeMillis();

    System.out.println("ms = " + (t_end - t_start));
    System.out.println("count = " + count);
    System.out.println(rows + "\n" + cols);
  } 

  public static String[] stat_info(List<String> data, String well, String stratum) {
    double tmp_value = Double.valueOf(data.get(1));
    String[] results = new String[7];
    String column = data.get(0);
    double min_value = 0, max_value = 0, sum_value = 0, median;
    int count = 0;
    
    for (int i = 1; i < data.size(); i++) {
      count++;
      tmp_value = Double.valueOf(data.get(i));
      sum_value += tmp_value;

      if (tmp_value != 0 && tmp_value < min_value || min_value == 0) {
        min_value = tmp_value;
      }

      if (tmp_value > max_value) {
        max_value = tmp_value;
      }
    }

    median = (sum_value != 0 ? (sum_value / count) : sum_value);

    results[0] = well;
    results[1] = stratum;
    results[2] = String.valueOf(median);
    results[3] = String.valueOf(min_value);
    results[4] = String.valueOf(max_value);
    results[5] = String.valueOf(sum_value);
    results[6] = column;
    
    return results;
  }

  public static void array_to_csv(String[] data_out) {
    BufferedWriter br;
    String csv_out = "test_data_out.csv";
    String[] headers = {"well", "stratum", "median", "min_value", "max_value", "sum_value", "column"};
    File csv_file_out = new File(csv_out);
    boolean headers_write = false;

    if (!csv_file_out.exists()){
     headers_write = !headers_write;
    }

    String delimiter = ";";
    try {
      br = new BufferedWriter(new FileWriter(csv_out, true));

      if (headers_write){
        for (int i = 0; i < headers.length; i++) {
          delimiter = (i == headers.length - 1) ? "\n" : delimiter;
          br.write(headers[i] + delimiter);
        }
      }

      delimiter = ";";
      for (int i = 0; i < data_out.length; i++) {
        delimiter = (i == data_out.length - 1) ? "\n" : delimiter;
        br.write(data_out[i].replace(".", ",") + delimiter);
      }

      br.close();
      } catch (
        FileNotFoundException e)
        {
          e.printStackTrace();
        } catch (
          IOException e)
          {
            e.printStackTrace();
          }
    }

  public static String[][] csv_to_array2d(String path) {
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
