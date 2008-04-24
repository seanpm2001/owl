
/* Internally, the bridge uses Info objects to store generic representations of all rules and relevant OWL knowledge. This representation is
** used to bridge between SWRL rules and OWL knowledge and the representation used by the target rule engine. It stores only knowledge about
** entities that are relevant to rule engine execution. This class is specialized into classes representing SWRL rules and OWL knowledge
** relevant to the execution of those rules.
*/
package edu.stanford.smi.protegex.owl.swrl.bridge;

import edu.stanford.smi.protegex.owl.model.*;

import java.util.*;

public abstract class Info 
{
} // Info

