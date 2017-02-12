package org.GroupClassAcceptabilities.xmcda;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupClassAcceptabilitiesCLI {
    public static void main(String[] args) throws Exception
    {
        final ArrayList<String> argsList = new ArrayList<String>(Arrays.asList(args));
        if ( argsList.remove("--v2") )
        {
            GroupClassAcceptabilitiesCLI_XMCDAv2.main((String[]) argsList.toArray(new String[]{}));
        }
        else if ( argsList.remove("--v3") )
        {
            GroupClassAcceptabilitiesCLI_XMCDAv3.main((String[]) argsList.toArray(new String[]{}));
        }
        else
        {
            System.err.println("missing mandatory option --v2 or --v3");
            System.exit(-1);
        }
    }
}

