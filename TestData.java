import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class TestData {
  @Test
  public void main() throws Exception {
    Calculations testCalculations = new Calculations();
    String[] csvFileNames = { "TestData_in.csv", "TestData_out.csv"};

    testCalculations.main(csvFileNames);

    File inputFile = new File(csvFileNames[1]);
    File compareFile = new File("TestData_true_result.csv");

    assertTrue("The files are differ!", FileUtils.contentEquals(inputFile, compareFile));
  }
}
