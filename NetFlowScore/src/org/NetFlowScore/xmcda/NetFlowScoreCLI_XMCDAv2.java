package org.NetFlowScore.xmcda;

import org.NetFlowScore.NetFlowScore;
import org.xmcda.ProgramExecutionResult;
import org.xmcda.XMCDA;
import org.xmcda.converters.v2_v3.XMCDAConverter;
import org.xmcda.parsers.xml.xmcda_v2.XMCDAParser;
import org.xmcda.utils.ValueConverters;

import java.io.File;
import java.util.Map;


public class NetFlowScoreCLI_XMCDAv2 {

    public static void main(String[] args) throws Utils.InvalidCommandLineException, ValueConverters.ConversionException, ClassNotFoundException {

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
        Utils.loadXMCDAv2(xmcda_v2, new File(indir, "preferences.xml"), true,
                executionResult, "alternativesComparisons");
        Utils.loadXMCDAv2(xmcda_v2, new File(indir, "parameters.xml"), true,
                executionResult, "methodParameters");

        if ( ! (executionResult.isOk() || executionResult.isWarning() ) )
        {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
        }

        try
        {
            xmcda = XMCDAConverter.convertTo_v3(xmcda_v2);
        }
        catch (Throwable t)
        {
            executionResult.addError(Utils.getMessage("Could not convert inputs to XMCDA v3, reason: ", t));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            return;
        }

        final InputsHandler.Inputs inputs = InputsHandler.checkAndExtractInputs(xmcda, executionResult);

        if ( ! ( executionResult.isOk() || executionResult.isWarning() ) || inputs == null )
        {
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
        }

        final Map<String, Double> results ;
        try
        {
            results = NetFlowScore.calculateNetFlowScore(inputs, executionResult);
        }
        catch (Throwable t)
        {
            executionResult.addError(Utils.getMessage("The calculation could not be performed, reason: ", t));
            Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
            return;
        }

        final Map<String, XMCDA> x_results = OutputsHandler.convert(results, executionResult);

        org.xmcda.v2.XMCDA results_v2;
        for ( String outputName : x_results.keySet() )
        {
            File outputFile = new File(outdir, String.format("%s.xml", outputName));
            try
            {
                results_v2 = XMCDAConverter.convertTo_v2(x_results.get(outputName));
                if ( results_v2 == null )
                    throw new IllegalStateException("Conversion from v3 to v2 returned a null value");
            }
            catch (Throwable t)
            {
                final String err = String.format("Could not convert %s into XMCDA_v2, reason: ", outputName);
                executionResult.addError(Utils.getMessage(err, t));
                continue;
            }
            try
            {
                XMCDAParser.writeXMCDA(results_v2, outputFile, OutputsHandler.xmcdaV2Tag(outputName));
            }
            catch (Throwable t)
            {
                final String err = String.format("Error while writing %s.xml, reason: ", outputName);
                executionResult.addError(Utils.getMessage(err, t));
                outputFile.delete();
            }
        }
        Utils.writeProgramExecutionResultsAndExit(prgExecResultsFile, executionResult, Utils.XMCDA_VERSION.v2);
    }
}
