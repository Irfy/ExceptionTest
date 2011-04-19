import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExceptionTest {

	public long maxLevel = 20;

	enum ExceptionHandler {
		NONE {
			void handle(Throwable t) {
			}
		},
		PRINT_STDOUT {
			void handle(Throwable t) {
				t.printStackTrace(System.out);
			}
		},
		PRINT_NULL {
			private final OutputStream nullos = new OutputStream() {
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
			private final PrintStream nullps = new PrintStream(nullos);

			void handle(Throwable t) {
				t.printStackTrace(nullps);
			}
		},
		PRINT_ARRAY {
			private final ByteArrayOutputStream baos = new ByteArrayOutputStream(
					1000000);
			private final PrintStream baps = new PrintStream(baos);

			void handle(Throwable t) {
				t.printStackTrace(baps);
			}
		};
		abstract void handle(Throwable t);
	}

	public static void main(String... args) {
		ExceptionTest test = new ExceptionTest();

		for (ExceptionHandler eh : ExceptionHandler.values()) {
			long start = System.currentTimeMillis();
			int count = 10000;
			for (int i = 0; i < count; i++)
				try {
					test.doTest(2, 0, eh);
				} catch (Exception ex) {
					eh.handle(ex);
				}
			long diff = System.currentTimeMillis() - start;
			System.err.println(String.format("Average time for invocation: %1$.5f",
					(double) diff / count));
		}
	}

	public void doTest(int i, int level, ExceptionHandler eh) {
		if (level < maxLevel)
			try {
				doTest(i, ++level, eh);
			} catch (Exception ex) {
				eh.handle(ex);
				throw new RuntimeException("UUUPS", ex);
			}
		else if (i > 1)
			throw new RuntimeException("Ups".substring(0, 3));
	}
}
