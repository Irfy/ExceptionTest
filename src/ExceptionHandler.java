import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

/**
 * Exception handling strategies. The enum declares an abstract
 * {@link #handle(Throwable)} function and all enum instances implement their
 * own version of it.
 * 
 * @author Irfan Adilovic
 */
enum ExceptionHandler {
	Ignore("The exception is completely ignored.") {
		@Override
		void handle(Throwable t) {
		}
	},

	GetMessage("The exception message is evaluated.") {
		@Override
		void handle(Throwable t) {
			t.getMessage();
		}
	},

	GetStackTrace("The stack trace is evaluated (effectively cloned).") {
		@Override
		void handle(Throwable t) {
			t.getStackTrace();
		}
	},

	PrintNull("The stack trace is printed to a stream that ignores input.") {

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

		@Override
		void handle(Throwable t) {
			t.printStackTrace(nullps);
		}
	},

	PrintByteArray("The stack trace is printed to a byte array in memory.") {

		private final ByteArrayOutputStream baos = new ByteArrayOutputStream(
				1000000);
		private final PrintStream baps = new PrintStream(baos);

		@Override
		void handle(Throwable t) {
			t.printStackTrace(baps);
			baos.reset(); // next handle invocation overwrites
		}
	},

	PrintStdout("The stack trace is printed to the standard output.") {

		@Override
		void handle(Throwable t) {
			t.printStackTrace(System.out);
		}
	},

	PrintFile("The stack trace is printed to a file.") {

		/*
		 * For obvious performance reasons, I create the file and its associated
		 * streams once, and never close them. This should never be done in
		 * production.
		 * 
		 * As a negative side-effect, not closing the FileOutputStream causes
		 * the file to never be deleted under Windows.
		 */

		FileOutputStream fileos;
		PrintStream fileps;
		FileChannel filech;

		{ // no ctors for enum instances - instance initializers FTW!
			try {
				File temp = File.createTempFile("ExceptionTestOutput_", null);
				temp.deleteOnExit(); // doesn't work on windows, stream open
				fileos = new FileOutputStream(temp);
				filech = fileos.getChannel();
				fileps = new PrintStream(fileos);
			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}

		@Override
		void handle(Throwable t) {
			t.printStackTrace(fileps);
			try {
				filech.position(0); // prevent huge temporary files
			} catch (IOException e) {
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
	};

	public final String desc;

	private ExceptionHandler(String desc) {
		this.desc = desc;
	}

	/**
	 * Handles a throwable with the specific strategy of this enum.
	 * 
	 * @param t
	 *            Throwable to handle.
	 */
	abstract void handle(Throwable t);
}