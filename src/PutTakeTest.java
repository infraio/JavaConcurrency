import static org.junit.Assert.assertEquals;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PutTakeTest {
  ExecutorService pool;
  AtomicInteger putSum = new AtomicInteger(0);
  AtomicInteger takeSum = new AtomicInteger(0);
  
  BoundedBuffer<Integer> bb;
  int nTrails;
  int nPairs;
  CyclicBarrier barrier;
  
  PutTakeTest(int capacity, int nPairs, int nTrails) {
    bb = new BoundedBuffer<Integer>(capacity);
    this.nTrails = nTrails;
    this.nPairs = nPairs;
    barrier = new CyclicBarrier(2 * nPairs + 1);
  }

  void run() {
    try {
      pool = Executors.newCachedThreadPool();

      for (int i = 0; i < nPairs; i++) {
        pool.submit(new Producer());
        pool.submit(new Consumer());
      }
      
      // wait all thread to start
      barrier.await();
      // cyclic barrier, wait all thread to end
      barrier.await();
      assertEquals(putSum.get(), takeSum.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      pool.shutdown();
    }
  }

  static int xorShift(int y) {
    y ^= (y << 6);
    y ^= (y >>> 21);
    y ^= (y << 7);
    return y;
  }

  class Producer implements Runnable {
    @Override
    public void run() {
      try {
        int seed = this.hashCode() ^ (int) System.nanoTime();
        int sum = 0;

        barrier.await();
        for (int i = nTrails; i > 0; i--) {
          bb.put(seed);
          sum += seed;
          seed = xorShift(seed);
        }
        putSum.getAndAdd(sum);
        barrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  class Consumer implements Runnable {
    @Override
    public void run() {
      try {
        barrier.await();
        int sum = 0;
        for (int i = nTrails; i > 0; i--) {
          sum += bb.take();
        }
        takeSum.getAndAdd(sum);
        barrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
