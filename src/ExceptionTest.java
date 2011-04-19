/**
 * <p>
 * Measures the cost of exceptions in a specific recursive re-throwing scenario
 * (see {@link #doTest(int)}) with various exception handling strategies (see
 * {@link ExceptionHandler}).
 * </p>
 * <p>
 * Accepts optional command-line arguments <#-tests-per-strategy>
 * <recursion-depth>.
 * 
 * @author Alois Reitbauer
 * @author Irfan Adilovic
 */
public class ExceptionTest {
	public static void main(String... args) {
		System.out.println("It is strongly suggested to redirect standard"
				+ " output to '/dev/null' (or 'nul' on windows) or you"
				+ " may need to let the program run for a day or two.");

		int count = 10000;
		int maxLevel = 20;

		// horrible programming practice, just for fun.
		try {
			count = Integer.parseInt(args[0]);
			maxLevel = Integer.parseInt(args[1]);
		} catch (Throwable t) {
		}
		// end horrible programming practice.

		for (ExceptionHandler exHandler : ExceptionHandler.values()) {
			System.err.printf("%14s - %s\n", exHandler, exHandler.desc);
			ExceptionTest exTest = new ExceptionTest(exHandler, maxLevel);
			double start = System.currentTimeMillis();
			for (int i = 0; i < count; i++)
				try {
					exTest.doTest(0);
				} catch (Exception ex) {
					exHandler.handle(ex);
				}
			double diff = System.currentTimeMillis() - start;
			System.err.printf("%23.4fms\n\n", diff / count);

			/*
			 * defensively try to reclaim memory, hoping that the JVM will
			 * refrain from using 'fast' exceptions (reusing old ones, setting
			 * an empty stack trace). See also
			 * http://www.javaspecialists.eu/archive/Issue187.html
			 */
			System.gc();
			System.gc();
		}
	}

	// non-static stuff

	private final ExceptionHandler exHandler;
	private final int maxLevel;

	private ExceptionTest(ExceptionHandler exHandler, int maxLevel) {
		this.exHandler = exHandler;
		this.maxLevel = maxLevel;
	}

	/**
	 * Calls itself exactly <code>MAX_LEVEL - level<code> times and then throws
	 * a {@link java.lang.RuntimeException}.
	 * 
	 * @param level
	 *            The recursive nesting level of this function.
	 */
	private void doTest(int level) {
		if (level < maxLevel)
			try {
				doTest(level + 1);
			} catch (Exception ex) {
				exHandler.handle(ex);
				throw new RuntimeException("Level " + level, ex);
			}
		else
			throw new RuntimeException("Level " + level);
	}
}
