package nonblocking;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicPseudoRandom extends PseudoRandom {
  
  private AtomicInteger seed;
  
  AtomicPseudoRandom(int seed) {
    this.seed = new AtomicInteger(seed);
  }

  @Override
  public int nextInt(int n) {
    while (true) {
      int s = seed.get();
      int nextSeed = calculateNext(s);
      if (seed.compareAndSet(s, nextSeed)) {
        int remainder = s % n;
        return remainder > 0 ? remainder : remainder + n;
      }
    }
  }
}
