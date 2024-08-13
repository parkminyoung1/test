package org.carlspring.strongbox.providers.layout;

import java.util.Date;

import org.carlspring.strongbox.npm.metadata.PackageVersion;

public class NpmPackageDesc
{

    private PackageVersion npmPackage;
    private Date releaseDate;
    private boolean lastVersion;

    public PackageVersion getNpmPackage()
    {
        return npmPackage;
    }

    public void setNpmPackage(PackageVersion npmPackage)
    {
        this.npmPackage = npmPackage;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public boolean isLastVersion()
    {
        return lastVersion;
    }

    public void setLastVersion(boolean lastVersion)
    {
        this.lastVersion = lastVersion;
    }

}
