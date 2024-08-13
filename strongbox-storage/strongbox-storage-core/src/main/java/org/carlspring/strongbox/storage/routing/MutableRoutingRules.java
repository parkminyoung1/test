package org.carlspring.strongbox.storage.routing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public class MutableRoutingRules
        implements Serializable
{

    private List<MutableRoutingRule> rules = new ArrayList<>();

    public List<MutableRoutingRule> getRules()
    {
        return rules;
    }

    public void setRules(List<MutableRoutingRule> rules)
    {
        this.rules = rules;
    }
}
