package com.adaptionsoft.games.trivia;

import approvaltest.BeyondCompareMacReporter;
import com.adaptionsoft.games.trivia.runner.GameRunner;
import org.approvaltests.legacycode.LegacyApprovals;
import org.approvaltests.reporters.UseReporter;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@UseReporter(BeyondCompareMacReporter.class)
public class GoldenMasterTest {


	@Test
	public void lock_down() throws Exception {
		String[] names = {
				"name01"
		};
		LegacyApprovals.LockDown(this, "processItem", names);
	}

	public String processItem(String name) {

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		ByteArrayOutputStream errContent = new ByteArrayOutputStream();

		PrintStream originalOutStream = System.out;
		PrintStream originalErrStream = System.err;

		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));

		GameRunner.main(new String[] {});

		System.setOut(originalOutStream);
		System.setErr(originalErrStream);

		System.out.println(outContent.toString());
		System.out.println(errContent.toString());

		return outContent.toString() + errContent.toString();
	}
}
