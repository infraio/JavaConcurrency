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
 * Object.wait atomically releases the lock and asks the OS to suspend the current thread,
 * allowing other threads to acquire the lock and therefore modify the object state.
 * Upon waking (waking by notify or notifyAll), it reacquires the lock before returning.
 */

public class BoundedBuffer<V> extends BaseBoundedBuffer<V> {

  public BoundedBuffer(int capacity) {
    super(capacity);
  }
  
  /**
   * Blocking operation.
   * @param v
   * @throws InterruptedException
   */
  public synchronized void put(V v) throws InterruptedException {
    while (isFull()) {
      wait();
    }
    doPut(v);
    notifyAll();
  }
  
  /**
   * Blocking operation.
   * @return
   * @throws InterruptedException
   */
  public synchronized V take() throws InterruptedException {
    while (isEmpty()) {
      wait();
    }
    V v = doTake();
    notifyAll();
    return v;
  }
  
  public static void main(String[] args) throws InterruptedException {
    BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);
    
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
