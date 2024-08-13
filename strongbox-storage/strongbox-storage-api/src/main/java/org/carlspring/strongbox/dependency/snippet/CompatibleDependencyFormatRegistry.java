package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.providers.AbstractMappedProviderRegistryWithNestedMap;

import javax.annotation.PostConstruct;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class CompatibleDependencyFormatRegistry
        extends AbstractMappedProviderRegistryWithNestedMap<DependencySynonymFormatter>
{

    private static final Logger logger = LoggerFactory.getLogger(CompatibleDependencyFormatRegistry.class);


    public CompatibleDependencyFormatRegistry()
    {
    }
    
    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the dependency snippet provider registry.");

        providers = new TreeMap<>();
    }

}
