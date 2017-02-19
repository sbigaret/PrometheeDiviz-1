package org.GroupClassAcceptabilities.xmcda;

import org.GroupClassAcceptabilities.GroupClassAcceptabilities;
import org.xmcda.ProgramExecutionResult;
import org.xmcda.XMCDA;
import org.xmcda.converters.v2_v3.XMCDAConverter;
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser;
import org.xmcda.utils.ValueConverters;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class GroupClassAcceptabilitiesCLI_XMCDAv2 {
    public static void main(String[] args) throws Utils.InvalidCommandLineException, ValueConverters.ConversionException {
        // Parsing the options
        final Utils.Arguments params = Utils.parseCmdLineArguments(args);

        final String indir = params.inputDirectory;
        final String outdir = params.outputDirectory;

        final File prgExecResultsFile = new File(outdir, "messages.xml");

        final ProgramExecutionResult executionResult = new ProgramExecutionResult();

        final XMCDA xmcda;

        // Load XMCDA v2.2.1 inputs
        org.xmcda.v2.XMCDA xmcda_v2 = new org.xmcda.v2.XMCDA();

        Utils.loadXMCDAv2(xmcda_v2, new File(indir, "alternatives.xml"), true,
                executionResult, "alternatives");
        Utils.loadXMCDAv2(xmcda_v2, new File(indir, "categories.xml"), false,
                executionResult, "categories");
        Utils.loadXMCDAv2(xmcda_v2, new File(indir, "categories_values.xml"), true,
                executionResult, "categoriesValues");

        for(int i = 1; i < 11; i++) {
            String decidentAssignments = "assignments_" + i + ".xml";
            Utils.loadXMCDAv2(xmcda_v2, new File(indir, decidentAssignments), false,
                    executionResult, "alternativesAffectations");
        }

        // We have problems with the inputs, its time to stop
        if (!(executionResult.isOk() || executionResult.isWarning())) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            // previous statement terminates the execution
        }

        // Convert that to XMCDA v3
        try {
            xmcda = XMCDAConverter.convertTo_v3(xmcda_v2);
        } catch (Throwable t) {
            executionResult.addError(Utils.getMessage("Could not convert inputs to XMCDA v3, reason: ", t));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            // previous statement terminates the execution
            return; // just to make the compiler happy about xmcda being final and potentially not initialized below
        }

        // Let's check the inputs and convert them into our own structures
        final InputsHandler.Inputs inputs = InputsHandler.checkAndExtractInputs(xmcda, executionResult);

        if (!(executionResult.isOk() || executionResult.isWarning()) || inputs == null) {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            // previous statement terminates the execution
        }

        // Here we know that everything was loaded as expected

        // Now let's call the calculation method
        final Object[] results;
        try {
            results = GroupClassAcceptabilities.calculateGroupClassAcceptabilities(inputs);
        } catch (Throwable t) {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", t));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            // previous statement terminates the execution
            return; // just to make the compiler happy about executionResults being final and potentially not initialized below
        }

        // Fine, now let's put the results into XMCDA structures
        final Map<String, XMCDA> x_results = OutputsHandler.convert(inputs, results, executionResult);

        // and finally, write them onto the appropriate files
        org.xmcda.v2.XMCDA results_v2;
        for (String outputName : x_results.keySet()) {
            File outputFile = new File(outdir, String.format("%s.xml", outputName));
            try {
                results_v2 = XMCDAConverter.convertTo_v2(x_results.get(outputName));
                if (results_v2 == null)
                    throw new IllegalStateException("Conversion from v3 to v2 returned a null value");
            } catch (Throwable t) {
                final String err = String.format("Could not convert %s into XMCDA_v2, reason: ", outputName);
                executionResult.addError(Utils.getMessage(err, t));
                continue; // try to convert & save as much as we can
            }
            try {
                XMCDAParser.writeXMCDA(results_v2, outputFile, OutputsHandler.xmcdaV2Tag(outputName));
            } catch (Throwable t) {
                final String err = String.format("Error while writing %s.xml, reason: ", outputName);
                executionResult.addError(Utils.getMessage(err, t));
                // Whatever the error is, clean up the file: we do not want to leave an empty or partially-written file
                outputFile.delete();
            }
        }

        if (!executionResult.isError()) {
            executionResult.addDebug("Success");
        }

        Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
        // previous statement terminates the execution
    }
}
