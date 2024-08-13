package org.carlspring.strongbox.util;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.domain.PypiPackageInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import static org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates.SOURCE_EXTENSION;
import static org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates.WHEEL_EXTENSION;

/**
 * Class to handle parsing of PyPi filename string
 *
 * @author alecg956
 */
public class PypiArtifactCoordinatesUtils
{

    private static Pattern PACKAGE_VERSION_PATTERN = Pattern.compile(PypiPackageInfo.VERSION_FORMAT,
                                                                     Pattern.CASE_INSENSITIVE);

    private static Pattern PACKAGE_DISTRIBUTION_NAME_PATTERN = Pattern.compile(PypiPackageInfo.DISTRIBUTION_NAME_FORMAT,
                                                                               Pattern.CASE_INSENSITIVE);

    /**
     * If optional build parameter is not found in the wheel package filename the empty string is specified for build_tag
     * in the construction of a PypiArtifactCoordinates object
     * <p>
     * Format of Wheel: {distribution}-{version}(-{build tag})?-{python tag}-{abi tag}-{platform tag}.whl.
     * Format of source: {distribution}-{version}.tar.gz
     *
     * @param path The filename of the PyPi artifact
     * @return Returns a PypiArtifactCoordinate object with all coordinates in the filename set
     */
    public static PypiArtifactCoordinates parse(String path)
    {
        if (!path.endsWith(".tar.gz") && !path.endsWith(".whl"))
        {
            throw new IllegalArgumentException("The artifact packaging can be only 'tar.gz' or '.whl'");
        }

        String fileName = FilenameUtils.getName(path);

        return path.endsWith(".tar.gz") ? parseSourcePackage(fileName) :
               parseWheelPackage(fileName);
    }

    private static PypiArtifactCoordinates parseSourcePackage(String path)
    {
        try
        {
            String packageNameWithoutExtension = path.substring(0, path.lastIndexOf(".tar.gz"));
            String distribution = packageNameWithoutExtension.substring(0,
                                                                        packageNameWithoutExtension.lastIndexOf("-"));
            String version = packageNameWithoutExtension.substring(packageNameWithoutExtension.lastIndexOf("-") + 1);

            Matcher matcher = PACKAGE_VERSION_PATTERN.matcher(version);
            if (!matcher.matches())
            {
                throw new IllegalArgumentException("Invalid version for source package.");
            }

            matcher = PACKAGE_DISTRIBUTION_NAME_PATTERN.matcher(distribution);
            if (!matcher.matches())
            {
                throw new IllegalArgumentException("Invalid name for source package.");
            }

            return new PypiArtifactCoordinates(distribution, version, SOURCE_EXTENSION);
        }
        catch (IllegalArgumentException iae)
        {
            throw iae;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid source package name.");
        }
    }

    private static PypiArtifactCoordinates parseWheelPackage(String path)
    {
        String[] splitArray = path.split("-");

        // check for invalid file format
        if (splitArray.length != 5 && splitArray.length != 6)
        {
            throw new IllegalArgumentException("Invalid wheel package name specified");
        }

        String distribution = splitArray[0];
        String version = splitArray[1];
        String build = null;
        String languageImplementationVersion;
        String abi;
        String platform;

        // build tag not included
        if (splitArray.length == 5)
        {
            languageImplementationVersion = splitArray[2];
            abi = splitArray[3];
            platform = splitArray[4].substring(0, splitArray[4].indexOf(".whl"));

        }
        // build tag is included
        else
        {
            build = splitArray[2];
            languageImplementationVersion = splitArray[3];
            abi = splitArray[4];
            platform = splitArray[5].substring(0, splitArray[5].indexOf(".whl"));
        }

        return new PypiArtifactCoordinates(distribution,
                                           version,
                                           build,
                                           languageImplementationVersion,
                                           abi,
                                           platform, WHEEL_EXTENSION);
    }
}
