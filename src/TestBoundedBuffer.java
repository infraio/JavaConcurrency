import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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