
package edu.stanford.smi.protegex.owl.swrl.bridge.builtins;

import edu.stanford.smi.protegex.owl.swrl.bridge.SWRLRuleEngineBridge;

/** An interface that must be used by any class defining SWRL built-in methods. See <a
 *  href="http://protege.cim3.net/cgi-bin/wiki.pl?SWRLBuiltInBridge">here</a> for documentation on defining SWRL built-in methods.
 */
public interface SWRLBuiltInLibrary 
{
    void initialize(SWRLRuleEngineBridge bridge);
} // SWRLBuiltInLibrary