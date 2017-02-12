package org.PrometheeIIIFlow.xmcda;

import java.util.ArrayList;
import java.util.Arrays;


public class PrometheeIIIFlowCLI {
    public static void main(String[] args) throws Exception
    {
        final ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
        if ( argsList.remove("--v2") )
        {
            PrometheeIIIFlowCLI_XMCDAv2.main((String[]) argsList.toArray(new String[]{}));
        }
        else if ( argsList.remove("--v3") )
        {
            PrometheeIIIFlowCLI_XMCDAv3.main((String[]) argsList.toArray(new String[]{}));
        }
        else
        {
            System.err.println("missing mandatory option --v2 or --v3");
            System.exit(-1);
        }
    }
}
