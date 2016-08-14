package nonblocking;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class PseudoRandomTest implements Runnable {

  public static final int WORKERS_NUM = 32;
  public static final int LOOP_RANGE = 1_000_000_000;
  public static final int OPERA_NUM = 1_000_000;
  public static final int RANDOM_BOUND = 1_000;
  public static final int LOOP_RANGE_LONG = 4096;

  CyclicBarrier barrier;
  private BarrierTimer timer;

  private int workersNum;
  private Thread[] workers;

  PseudoRandomTest(int workersNum, int loopRange, TestType type, boolean withLong) {
    this.workersNum = workersNum;
    timer = new BarrierTimer();
    barrier = new CyclicBarrier(workersNum + 1, timer);

    Random random = null;
    if (type == TestType.JAVARANDOM) {
      random = new Random(System.currentTimeMillis());
    } else if (type == TestType.REENTRANTLOCK) {
      random = new ReentrantLockPseudoRandom((int) System.currentTimeMillis());
    } else if (type == TestType.ATOMIC) {
      random = new AtomicPseudoRandom((int) System.currentTimeMillis());
    }

    workers = new Thread[workersNum];
    if (withLong) {
      for (int i = 0; i < workersNum; i++) {
        workers[i] = new Thread(new WorkerByCompareLong(OPERA_NUM, loopRange, random));
      }
    } else {
      for (int i = 0; i < workersNum; i++) {
        workers[i] = new Thread(new Worker(OPERA_NUM, loopRange, random));
      }
    }
  }

  private long getRunTimeInMillis() {
    return timer.getTimeInMillis();
  }

  @Override
  public void run() {
    try {
      for (int i = 0; i < workersNum; i++) {
        workers[i].start();
      }
      // wait all worker to start work
      barrier.await();
      // wait all worker done
      barrier.await();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    for (int i = 1; i <= WORKERS_NUM; i *= 2) {
      for (int j = 1_000_000; j <= LOOP_RANGE; j *= 10) {
        System.out.println("Workers number : " + i + ", Loop range : " + j);

        PseudoRandomTest test = new PseudoRandomTest(i, j, TestType.THREADLOCAL, false);
        Thread testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test ThreadLocalRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.JAVARANDOM, false);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test JavaRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.REENTRANTLOCK, false);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out
            .println("Test ReentrantLockPseudoRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.ATOMIC, false);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test AtomicPseudoRandom : " + test.getRunTimeInMillis() + " ms.");

        System.out.println();
      }
    }

    for (int i = 1; i <= WORKERS_NUM; i *= 2) {
      for (int j = 16; j <= LOOP_RANGE_LONG; j *= 4) {
        System.out.println("Worker by compare long");
        System.out.println("Workers number : " + i + ", Loop range : " + j);

        PseudoRandomTest test = new PseudoRandomTest(i, j, TestType.THREADLOCAL, true);
        Thread testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test ThreadLocalRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.JAVARANDOM, true);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test JavaRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.REENTRANTLOCK, true);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out
            .println("Test ReentrantLockPseudoRandom : " + test.getRunTimeInMillis() + " ms.");

        test = new PseudoRandomTest(i, j, TestType.ATOMIC, true);
        testThread = new Thread(test);
        testThread.start();
        testThread.join();
        System.out.println("Test AtomicPseudoRandom : " + test.getRunTimeInMillis() + " ms.");

        System.out.println();
      }
    }
  }

  enum TestType {
    THREADLOCAL, JAVARANDOM, REENTRANTLOCK, ATOMIC
  }

  class Worker implements Runnable {

    private int ops;
    protected int range;
    private Random random = null;

    Worker(int ops, int range, Random random) {
      this.ops = ops;
      this.range = range;
      this.random = random;
    }

    private void work() {
      int next = this.random.nextInt(RANDOM_BOUND);
      doSomething(next);
    }

    protected void doSomething(int next) {
      for (int i = 0; i < range; i++) {
      }
    }

    @Override
    public void run() {
      try {
        barrier.await();
        if (this.random == null) {
          this.random = ThreadLocalRandom.current();
        }
        for (int i = 0; i < ops; i++) {
          work();
        }
        barrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  class WorkerByCompareLong extends Worker {

    WorkerByCompareLong(int ops, int range, Random random) {
      super(ops, range, random);
    }

    @Override
    protected void doSomething(int next) {
      for (long i = 0; i < range; i++) {
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

  public long getTimeInMillis() {
    return getTime() / 1000000;
  }
}