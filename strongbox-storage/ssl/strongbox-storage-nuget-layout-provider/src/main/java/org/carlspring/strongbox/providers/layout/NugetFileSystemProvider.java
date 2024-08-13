package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.io.LayoutOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class NugetFileSystemProvider extends LayoutFileSystemProvider
{

    private static final Logger logger = LoggerFactory.getLogger(NugetFileSystemProvider.class);

    @Inject
    private NugetLayoutProvider layoutProvider;

    public NugetFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);
    }

    @Override
    protected AbstractLayoutProvider getLayoutProvider()
    {
        return layoutProvider;
    }

    @Override
    protected LayoutOutputStream decorateStream(RepositoryPath path,
                                                OutputStream os)
        throws NoSuchAlgorithmException,
        IOException
    {
        LayoutOutputStream result = super.decorateStream(path, os);
        result.setDigestStringifier(layoutProvider::toBase64);
        return result;
    }
}
