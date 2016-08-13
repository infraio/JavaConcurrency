package custom;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionBoundedBuffer<V> extends BaseBoundedBuffer<V> {

  private final Lock lock = new ReentrantLock();
  private final Condition notFull = lock.newCondition();
  private final Condition notEmpty = lock.newCondition();
  
  public ConditionBoundedBuffer(int capacity) {
    super(capacity);
  }

  public void put(V v) throws InterruptedException {
    lock.lock();
    try {
      while (isFull()) {
        notFull.await();
      }
      doPut(v);
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }
  
  public V take() throws InterruptedException {
    lock.lock();
    try {
      while (isEmpty()) {
        notEmpty.await();
      }
      V v = doTake();
      notFull.signal();
      return v;
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    ConditionBoundedBuffer<Integer> buffer = new ConditionBoundedBuffer<>(10);
    
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
