import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExceptionTest {

	public long maxLevel = 20;
	private static final boolean DO_PRINT = true;

	private static final ByteArrayOutputStream baos = new ByteArrayOutputStream(
			1000000);
	private static final PrintStream baps = new PrintStream(baos);

	private static final OutputStream nullos = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}

		@Override
		public void write(byte[] b) throws IOException {
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
		};
	};
	private static final PrintStream nullps = new PrintStream(nullos);

	public static void main(String... args) {
		ExceptionTest test = new ExceptionTest();

		long start = System.currentTimeMillis();
		int count = 10000;
		for (int i = 0; i < count; i++)
			try {
				test.doTest(2, 0);
			} catch (Exception ex) {
				if (DO_PRINT) {
					ex.printStackTrace(nullps);
					baos.reset();
				} else {
					ex.getStackTrace();
				}
			}
		long diff = System.currentTimeMillis() - start;
		System.out.println(String.format("Average time for invocation: %1$.5f",
				(double) diff / count));
	}

	public void doTest(int i, int level) {
		if (level < maxLevel)
			try {
				doTest(i, ++level);
			} catch (Exception ex) {
				if (DO_PRINT) {
					ex.printStackTrace(nullps);
					baos.reset();
				} else {
					ex.getStackTrace();
				}
				throw new RuntimeException("UUUPS", ex);
			}
		else if (i > 1)
			throw new RuntimeException("Ups".substring(0, 3));
	}
}
