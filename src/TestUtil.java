import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestUtil {

  int getHeapSize() throws IOException {
    int threadNum = getTestThreadNumber();
    if (threadNum != -1) {
      String jmapShell = "jmap -heap " + threadNum;
      Process process = Runtime.getRuntime().exec(jmapShell);
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line = "";
      while ((line = input.readLine()) != null) {
        System.out.println(line);
      }
    }
    return 0;
  }

  int getTestThreadNumber() throws IOException {
    Process process = Runtime.getRuntime().exec("jps");
    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = "";
    while ((line = input.readLine()) != null) {
      String[] array = line.split(" ");
      if (array.length >= 1 && array[1].equals("RemoteTestRunner")) {
        return Integer.valueOf(array[0]);
      }
    }
    return -1;
  }
  
  class BigObject {
    double[] data = new double[100000];
  }
}
