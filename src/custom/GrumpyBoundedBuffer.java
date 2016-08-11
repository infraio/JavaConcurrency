package custom;

public class GrumpyBoundedBuffer<V> extends BaseBoundedBuffer<V> {

  public static final int SLEEP_GRANULARITY = 10;

  public GrumpyBoundedBuffer(int capacity) {
    super(capacity);
  }

  /**
   * Don't block.
   * @param v
   * @throws BufferFullException
   */
  public synchronized void put(V v) throws BufferFullException {
    if (isFull()) {
      throw new BufferFullException("Can't put value to a full buffer");
    }
    doPut(v);
  }

  /**
   * Don't block.
   * @return
   * @throws BufferEmptyException
   */
  public synchronized V take() throws BufferEmptyException {
    if (isEmpty()) {
      throw new BufferEmptyException("Can't take value from a empty buffer");
    }
    return doTake();
  }

  public static void main(String[] args) throws InterruptedException {
    GrumpyBoundedBuffer<Integer> buffer = new GrumpyBoundedBuffer<>(10);
    
    Thread producer = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          while (true) {
            try {
              buffer.put(i);
              break;
            } catch (BufferFullException e) {
              try {
                Thread.sleep(SLEEP_GRANULARITY);
              } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
              }
            }
          }
          System.out.println("put " + i);
        }
      }
    });

    Thread consumer = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          int v;
          while (true) {
            try {
              v = buffer.take();
              break;
            } catch (BufferEmptyException e) {
              try {
                Thread.sleep(SLEEP_GRANULARITY);
              } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
              }
            }
          }
          System.out.println("take " + v);
        }
      }
    });
    
    producer.start();
    consumer.start();
    producer.join();
    consumer.join();
  }

}

class BufferFullException extends Exception {
  BufferFullException(String msg) {
    super(msg);
  }
}

class BufferEmptyException extends Exception {
  BufferEmptyException(String msg) {
    super(msg);
  }
}