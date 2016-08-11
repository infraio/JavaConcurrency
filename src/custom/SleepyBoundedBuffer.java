package custom;

/**
 * Structure of blocking state-dependent actions.
 *   
 *   acquire lock on object state
 *   while (precondition does not hold) {
 *     release lock
 *     wait until precondition might hold
 *     optionally fail if interrupted or timeout expires
 *     reacquire lock
 *   }
 *   perform action
 *   release lock
 *
 */

public class SleepyBoundedBuffer<V> extends BaseBoundedBuffer<V> {
  
  public static final int SLEEP_GRANULARITY = 10;
  
  public SleepyBoundedBuffer(int capacity) {
    super(capacity);
  }

  /**
   * Blocking operation.
   * @param v
   * @throws InterruptedException
   */
  public void put(V v) throws InterruptedException {
    while (true) {
      synchronized (this) {
        if (!isFull()) {
          doPut(v);
          return;
        }
      }
      Thread.sleep(SLEEP_GRANULARITY);
    }
  }

  /**
   * Blocking operation.
   * @return
   * @throws InterruptedException
   */
  public V take() throws InterruptedException {
    while (true) {
      synchronized (this) {
        if (!isEmpty()) {
          return doTake();
        }
      }
      Thread.sleep(SLEEP_GRANULARITY);
    }
  }

  public static void main(String[] args) throws InterruptedException {
    SleepyBoundedBuffer<Integer> buffer = new SleepyBoundedBuffer<>(10);
    
    Thread producer = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          try {
            buffer.put(i);
            System.out.println("put " + i);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    });
    
    Thread consumer = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          try {
            int v = buffer.take();
            System.out.println("take " + v);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    });
    
    producer.start();
    consumer.start();
    producer.join();
    consumer.join();
  }

}
