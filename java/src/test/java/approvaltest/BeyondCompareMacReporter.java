package approvaltest;

import org.approvaltests.reporters.GenericDiffReporter;

import java.text.MessageFormat;

public class BeyondCompareMacReporter extends GenericDiffReporter {

    private static final String DIFF_PROGRAM = "/usr/local/bin/bcomp";
    static final String MESSAGE      = MessageFormat.format("Unable to find bcomp at {0}",
            DIFF_PROGRAM);
    public static final BeyondCompareMacReporter INSTANCE   = new BeyondCompareMacReporter();
    public BeyondCompareMacReporter()
    {
        super(DIFF_PROGRAM, "%s %s", MESSAGE, GenericDiffReporter.TEXT_FILE_EXTENSIONS);
    }


}
