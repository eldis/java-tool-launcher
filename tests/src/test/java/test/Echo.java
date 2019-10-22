package test;

/** Echoes its arguments to stdout, one at a time. */
public class Echo {
  public static void main(String[] args) {
    for (String arg : args) {
      System.out.println(arg);
    }
  }
}
