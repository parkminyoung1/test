package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SnippetGenerator
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    public List<CodeSnippet> generateSnippets(String layout,
                                              ArtifactCoordinates coordinates)
    {
        Map<String, DependencySynonymFormatter> implementations = compatibleDependencyFormatRegistry.getProviderImplementations(layout);

        List<CodeSnippet> snippets = new ArrayList<>();

        // Get the snippet for the default provider and make sure it's first in the list
        DependencySynonymFormatter defaultFormatter = implementations.get(layout);
        CodeSnippet defaultSnippet = new CodeSnippet(layout,
                                                     defaultFormatter.getDependencySnippet(coordinates));
        snippets.add(defaultSnippet);

        // Add the rest of the synonyms
        for (String compatibleDependencyFormat : implementations.keySet())
        {
            if (compatibleDependencyFormat.equals(layout))
            {
                // We've already added this, before this loop.
                continue;
            }

            DependencySynonymFormatter formatter = implementations.get(compatibleDependencyFormat);

            CodeSnippet snippet = new CodeSnippet(compatibleDependencyFormat, formatter.getDependencySnippet(coordinates));
            snippets.add(snippet);
        }

        return snippets;
    }

}
