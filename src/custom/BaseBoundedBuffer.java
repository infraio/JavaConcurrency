package custom;

public abstract class BaseBoundedBuffer<V> {
  
  private final V[] buffer;
  private int head;
  private int tail;
  private int count;

  protected BaseBoundedBuffer(int capacity) {
    buffer = (V[]) new Object[capacity];
  }
  
  protected synchronized final void doPut(V v) {
    buffer[tail] = v;
    if (++tail == buffer.length) {
      tail = 0;
    }
    count++;
  }
  
  protected synchronized final V doTake() {
    V v = buffer[head];
    if (++head == buffer.length) {
      head = 0;
    }
    count--;
    return v;
  }
  
  public synchronized final boolean isFull() {
    return count == buffer.length;
  }
  
  public synchronized final boolean isEmpty() {
    return count == 0;
  }
}
