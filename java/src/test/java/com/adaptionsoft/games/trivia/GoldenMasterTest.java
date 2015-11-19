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
                new Players("Chet", "Pat", "Sue", "Mark", "Anthony", "Greg"),
        };
        LegacyApprovals.LockDown(this, "runGame", seeds, players);
    }


    //~~~ Helpers

    public String runGame(Integer seed, Players players) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(outContent);
        PrintStream errStream = new PrintStream(errContent);


        try {
            PrintStream originalOutStream = System.out;
            PrintStream originalErrStream = System.err;

            System.setOut(outStream);
            System.setErr(errStream);

            GameRunner.runWith(seed, players);

            System.setOut(originalOutStream);
            System.setErr(originalErrStream);

            System.out.println(outContent.toString());
            System.out.println(errContent.toString());
        } catch (Exception e) {
            errStream.println(e.toString());
        }

        return outContent.toString() + errContent.toString();
    }
}
