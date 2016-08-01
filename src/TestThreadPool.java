import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TestThreadPool {

  @Test
  public void testPoolExpansion() throws InterruptedException {
    int MAX_SIZE = 10;
    TestThreadFactory threadFactory = new TestThreadFactory();
    ExecutorService exec = Executors.newFixedThreadPool(MAX_SIZE, threadFactory);
    
    for (int i = 0; i < 10 * MAX_SIZE; i++) {
      exec.submit(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(Long.MAX_VALUE);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      });
    }
    
    for (int i = 0; i < 10 * MAX_SIZE; i++) {
      assertTrue(threadFactory.getNumCreated() <= MAX_SIZE);
      Thread.sleep(100);
    }
    assertEquals(threadFactory.getNumCreated(), MAX_SIZE);
    exec.shutdownNow();
  }

}

class TestThreadFactory implements ThreadFactory {

  public final AtomicInteger numCreated = new AtomicInteger(0);
  private final ThreadFactory factory = Executors.defaultThreadFactory();

  @Override
  public Thread newThread(Runnable r) {
    numCreated.incrementAndGet();
    return factory.newThread(r);
  }

  public int getNumCreated() {
    return this.numCreated.get();
  }
}
