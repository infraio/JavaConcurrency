import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MapPerformance {

  public static final int RANGE = 1000;
  public static final int THREADS_NUMBER = 4;
  public static final int DURATION = 10;

  public static void main(String[] args) throws InterruptedException {

    Map<Integer, Integer> map;

    if (args.length < 1) {
      map = new ConcurrentHashMap<>();
    } else {
      if (args[0].equals("HashMap")) {
        Map<Integer, Integer> hashMap = new HashMap<>();
        map = Collections.synchronizedMap(hashMap);
      } else if (args[0].equals("TreeMap")) {
        Map<Integer, Integer> treeMap = new TreeMap<>();
        map = Collections.synchronizedMap(treeMap);
      } else if (args[0].equals("ConcurrentSkipListMap")) {
        map = new ConcurrentSkipListMap<>();
      } else {
        map = new ConcurrentHashMap<>();
      }
    }

    Worker[] workers = new Worker[THREADS_NUMBER];

    for (int i = 0; i < THREADS_NUMBER; i++) {
      workers[i] = new Worker(map);
      Thread t = new Thread(workers[i]);
      t.start();
    }

    for (int i = 0; i < DURATION; i++) {
      Thread.sleep(1000);
      int count = 0;
      for (int j = 0; j < THREADS_NUMBER; j++) {
        count += workers[j].count();
      }
      System.out.println("After " + i + " seconds, " + count + " operations");
    }

    System.exit(0);
  }

}

class Worker implements Runnable {

  Map<Integer, Integer> map;
  Random random;
  int counter;

  Worker(Map<Integer, Integer> map) {
    this.map = map;
    this.random = new Random();
    this.counter = 0;
  }

  @Override
  public void run() {
    while (true) {
      int num = random.nextInt(MapPerformance.RANGE);
      Integer value = map.get(num);
      if (value == null) {
        map.put(num, num);
      } else {
        map.remove(num);
      }
      this.counter++;
    }
  }

  public int count() {
    return this.counter;
  }
}
