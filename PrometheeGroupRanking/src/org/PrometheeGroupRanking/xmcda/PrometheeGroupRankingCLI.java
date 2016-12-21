package org.PrometheeGroupRanking.xmcda;

import org.PrometheeGroupRanking.PrometheeGroupRanking;
import org.xmcda.ProgramExecutionResult;
import org.xmcda.XMCDA;
import org.xmcda.utils.ValueConverters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class PrometheeGroupRankingCLI {
    public static void main(String[] args) throws Exception
    {
        final ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
        if ( argsList.remove("--v2") )
        {
            PrometheeGroupRankingCLI_XMCDAv2.main((String[]) argsList.toArray(new String[]{}));
        }
        else if ( argsList.remove("--v3") )
        {
            PrometheeGroupRankingCLI_XMCDAv3.main((String[]) argsList.toArray(new String[]{}));
        }
        else
        {
            System.err.println("missing mandatory option --v2 or --v3");
            System.exit(-1);
        }
    }
}
