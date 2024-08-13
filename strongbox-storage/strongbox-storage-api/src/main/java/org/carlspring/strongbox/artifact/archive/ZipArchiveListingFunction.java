package org.carlspring.strongbox.artifact.archive;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

/**
 * @author Przemyslaw Fusik
 */
public enum ZipArchiveListingFunction
        implements ArchiveListingFunction
{

    INSTANCE;

    @Override
    public Set<String> listFilenames(final RepositoryPath path)
            throws IOException
    {
        try (InputStream is = Files.newInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(is);
             ArchiveInputStream ais = new ZipArchiveInputStream(bis))
        {
            return getEntryNames(ais);
        }
    }

    @Override
    public boolean supports(final RepositoryPath path)
    {
        final Path fileName = path.getFileName();
        return fileName != null && fileName.toString().endsWith("zip");
    }
}
