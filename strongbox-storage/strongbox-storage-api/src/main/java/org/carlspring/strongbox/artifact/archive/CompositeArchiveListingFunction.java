package org.carlspring.strongbox.artifact.archive;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public class CompositeArchiveListingFunction
        implements ArchiveListingFunction
{

    private final Set<ArchiveListingFunction> leafs;

    public CompositeArchiveListingFunction(final Set<ArchiveListingFunction> leafs)
    {
        Objects.requireNonNull(leafs, "Set of archive listing functions should not be null");
        this.leafs = leafs;
    }

    @Override
    public Set<String> listFilenames(final RepositoryPath path)
            throws IOException
    {
        final Set<String> result = new HashSet<>();
        for (final ArchiveListingFunction leaf : leafs)
        {
            if (leaf.supports(path))
            {
                result.addAll(leaf.listFilenames(path));
            }
        }
        return result;
    }

    @Override
    public boolean supports(final RepositoryPath path)
    {
        return leafs.stream().filter(leaf -> leaf.supports(path)).findFirst().isPresent();
    }

    @Override
    public String toString()
    {
        return "[" + getClass().getName() + "] leafs {" + Objects.toString(leafs) + "}";
    }
}
