package com.adaptionsoft.games.trivia;

import approvaltest.BeyondCompareMacReporter;
import com.adaptionsoft.games.trivia.runner.GameRunner;
import com.adaptionsoft.games.trivia.runner.Players;
import org.approvaltests.legacycode.LegacyApprovals;
import org.approvaltests.reporters.UseReporter;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@UseReporter(BeyondCompareMacReporter.class)
public class GoldenMasterTest {


	@Test
	public void lock_down() throws Exception {
		Integer[] seeds = {
				111, 121, 321, 4323
		};
		Players[] players = new Players[]{
				new Players(),
				new Players("Chet"),
				new Players("Chet", "Pat", "Sue"),
		};
		LegacyApprovals.LockDown(this, "runGame", seeds, players);
	}


	//~~~ Helpers

	public String runGame(Integer seed, Players players) {

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		ByteArrayOutputStream errContent = new ByteArrayOutputStream();

		PrintStream originalOutStream = System.out;
		PrintStream originalErrStream = System.err;

		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));

		GameRunner.runWith(seed, players);

		System.setOut(originalOutStream);
		System.setErr(originalErrStream);

		System.out.println(outContent.toString());
		System.out.println(errContent.toString());

		return outContent.toString() + errContent.toString();
	}
}
