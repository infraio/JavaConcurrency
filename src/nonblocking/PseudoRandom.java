package nonblocking;

/**
 * Implement caclulateNext() by Random.java in jdk 1.8_65. 
 */
public abstract class PseudoRandom {

  private static final long multiplier = 0x5DEECE66DL;
  private static final long addend = 0xBL;
  private static final long mask = (1L << 48) - 1;
  private static final int bits = 32;

  /**
   * Generates the next pseudorandom number. 
   * 
   * The general contract of next is that it returns an int value and if the argument
   * bits is between 1 and 32 (inclusive), then that many low-order bits of the returned value will
   * be (approximately) independently chosen bit values, each of which is (approximately) equally
   * likely to be 0 or 1. 
   * 
   * The method next is implemented by class Random by atomically updating the
   * seed to 
   * (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)
   * and returning
   * (int)(seed >>> (48 - bits)).
   * 
   * This is a linear congruential pseudorandom number generator, as defined by D. H. Lehmer
   * and described by Donald E. Knuth in The Art of Computer Programming, Volume 3: Seminumerical
   * Algorithms, section 3.2.1.
   * 
   * @param oldseed
   * @return
   */
  protected int calculateNext(long oldseed) {
    long nextseed = (oldseed * multiplier + addend) & mask;
    return (int) (nextseed >>> (48 - bits));
  }
  
  protected abstract int nextInt(int n);
}
