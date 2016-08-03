import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

public class TimedPutTakeTest extends PutTakeTest {

  private BarrierTimer timer;
  
  public TimedPutTakeTest(int capacity, int nPairs, int nTrails) {
    super(capacity, nPairs, nTrails);
    timer = new BarrierTimer();
    barrier = new CyclicBarrier(2 * nPairs + 1, timer);
  }

  @Override
  public void run() {
    try {
      pool = Executors.newCachedThreadPool();
      timer.clear();

      for (int i = 0; i < nPairs; i++) {
        pool.submit(new Producer());
        pool.submit(new Consumer());
      }
      
      // wait all thread to start
      barrier.await();
      // cyclic barrier, wait all thread to end
      barrier.await();
      
      long nsPerItem = timer.getTime() / (nPairs * nTrails);
      System.out.println("Total time : " + timer.getTime() + " ns.");
      System.out.println("Total operation : " + (nPairs * nTrails));
      System.out.println("Average latency : " + nsPerItem + " ns.");
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      pool.shutdown();
    }
  }
  
  public static void main(String[] args) throws Exception {
    int opPerThread = 1000000;
    for (int capacity = 10; capacity <= 10000; capacity *= 10) {
      for (int pairs = 1; pairs <= 128; pairs *= 2) {
        System.out.println("Capacity : " + capacity);
        System.out.println("Pairs : " + pairs);
        TimedPutTakeTest t = new TimedPutTakeTest(capacity, pairs, opPerThread);
        t.run();
        System.out.println();
        Thread.sleep(1000);
        t.run();
        System.out.println();
      }
    }
  }
  
}

class BarrierTimer implements Runnable {

  private boolean started;
  private long startTime, endTime;
  
  @Override
  public void run() {
    long t = System.nanoTime();
    if (!started) {
      started = true;
      startTime = t;
    } else {
      endTime = t;
    }
  }
  
  public synchronized void clear() {
    started = false;
  }
  
  public long getTime() {
    return endTime - startTime;
  }
  
}