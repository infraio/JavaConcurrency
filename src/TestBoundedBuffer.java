import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TestBoundedBuffer {

  public static final int LOOKUP_DETECT_TIMEOUT = 1000;

  @Test
  public void testIsEmptyWhenConstructed() {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    assertTrue(bb.isEmpty());
    assertFalse(bb.isFull());
  }
  
  @Test
  public void testIsFullAfterPuts() throws InterruptedException {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    for (int i = 0; i < 10; i++) {
      bb.put(i);
    }
    assertTrue(bb.isFull());
    assertFalse(bb.isEmpty());
  }
  
  @Test
  public void testTableBlocksWhenEmpty() {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    Thread taker = new Thread() {
      public void run() {
        try {
          int unused = bb.take();
          fail();
        } catch (InterruptedException success) {
        }
      }
    };
    try {
      taker.start();
      Thread.sleep(LOOKUP_DETECT_TIMEOUT);
      taker.interrupt();
      taker.join(LOOKUP_DETECT_TIMEOUT);
      assertFalse(taker.isAlive());
    } catch (Exception unexpected) {
      fail();
    }
  }
  
  @Test
  public void testPutTake() {
    new PutTakeTest(100, 100000, 16).run();
  }

}

class PutTakeTest {
  
  ExecutorService pool = Executors.newCachedThreadPool();
  AtomicInteger putSum = new AtomicInteger(0);
  AtomicInteger takeSum = new AtomicInteger(0);
  
  BoundedBuffer<Integer> bb;
  int nTrails;
  int nPairs;
  CyclicBarrier startBarrier;
  CyclicBarrier endBarrier;
  
  PutTakeTest(int capacity, int nTrails, int nPairs) {
    bb = new BoundedBuffer<Integer>(capacity);
    this.nTrails = nTrails;
    this.nPairs = nPairs;
    startBarrier = new CyclicBarrier(2 * nPairs + 1);
    endBarrier = new CyclicBarrier(2 * nPairs + 1);
  }

  void run() {
    try {
      for (int i = 0; i < nPairs; i++) {
        pool.submit(new Producer());
        pool.submit(new Consumer());
      }
      
      // wait all thread to start
      startBarrier.await();
      // wait all thread to end
      endBarrier.await();
      assertEquals(putSum.get(), takeSum.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
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

        startBarrier.await();
        for (int i = nTrails; i > 0; i--) {
          bb.put(seed);
          sum += seed;
          seed = xorShift(seed);
        }
        putSum.getAndAdd(sum);
        endBarrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  class Consumer implements Runnable {
    @Override
    public void run() {
      try {
        startBarrier.await();
        int sum = 0;
        for (int i = nTrails; i > 0; i--) {
          sum += bb.take();
        }
        takeSum.getAndAdd(sum);
        endBarrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

}
