/*
 * Copyright 2019 Carlspring Consulting & Development Ltd.
 * Copyright 2014 Dmitry Sviridov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carlspring.strongbox.storage.metadata.nuget;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.text.MessageFormat.format;

/**
 * Represents .net framework of a nuspec field. 
 * This could be dependencies target framework or framework assemblies applicable framework version. 
 * 
 * @author Dmitry Sviridov
 */
public enum Framework
{

    /**
     * NET 1.0
     */
    net10(".NETFramework1.0",
          new String[] { "net10" },
          new String[] {}),

    /**
     * NET 1.1
     */
    net11(".NETFramework1.1",
          new String[] { "net11" },
          new String[] { "net10" }),

    /**
     * NET 2.0
     */
    net20(".NETFramework2.0",
          new String[] { "net20" },
          new String[] {}),

    /**
     * NET 3.0
     */
    net30(".NETFramework3.0",
          new String[] { "net30" },
          new String[] { "net20" }),

    /**
     * NET 3.5
     */
    net35(".NETFramework3.5",
          new String[] { "net35" },
          new String[] { "net30", "net20" }),

    /**
     * NET 4.0
     */
    net40(".NETFramework4.0",
          new String[] { "net40", "net40-Client" },
          new String[] { "net35", "net30", "net20" }),

    /**
     * NET 4.5
     */
    net45(".NETFramework4.5",
          new String[] { "net45", "win80" },
          new String[] { "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.5.1
     */
    net451(".NETFramework4.5.1",
           new String[] { "net451" },
           new String[] { "net45", "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.5.2
     */
    net452(".NETFramework4.5.2",
           new String[] { "net452" },
           new String[] { "net451", "net45", "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.6
     */
    net46(".NETFramework4.6",
          new String[] { "net46" },
          new String[] { "net452", "net451", "net45", "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.6.1
     */
    net461(".NETFramework4.6.1",
           new String[] { "net461" },
           new String[] { "net46", "net452", "net451", "net45", "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.6.2
     */
    net462(".NETFramework4.6.2",
           new String[] { "net462" },
           new String[] { "net461", "net46", "net452", "net451", "net45", "net40", "net35", "net30", "net20" }),

    /**
     * NET 4.6.3
     */
    net463(".NETFramework4.6.3",
           new String[] { "net463" },
           new String[] { "net462", "net461", "net46", "net452", "net451", "net45", "net40", "net35", "net30",
                          "net20" }),

    // TODO Add core frameworks according to :
    // https://github.com/NuGet/NuGet.Client/blob/dev/src/NuGet.Core/NuGet.Frameworks/FrameworkConstants.cs

    /**
     * .NETFramework4.5 Portable
     */
    portable_net45(".NETFramework4.5 Portable",
                   new String[] { "portable-net45" },
                   new String[] {}),

    /**
     * WinRT 4.5
     */
    winrt45("WinRT 4.5",
            new String[] { "winrt45" },
            new String[] {}),

    /**
     * SilverLight 2.0
     */
    sl20("SilverLight 2",
         new String[] { "sl2" },
         new String[] {}),

    /**
     * SilverLight 30
     */
    sl30("SilverLight 30",
         new String[] { "sl30", "sl3" },
         new String[] {}),

    /**
     * SilverLight 4
     */
    sl4("SilverLight 4",
        new String[] { "sl4", "sl40", "sl40-wp71" },
        new String[] {}),

    /**
     * SilverLight 5
     */
    sl5("SilverLight 5",
        new String[] { "sl5", "sl50" },
        new String[] {}),

    /**
     * WindowsPhone 7.1
     */
    wp71("WindowsPhone 7.1",
         new String[] { "wp71" },
         new String[] {}),

    /**
     * WindowsPhone 8.0
     */
    wp80("WindowsPhone 8",
         new String[] { "wp80" },
         new String[] {}),

    /**
     * Native application
     */
    nativeFile("Native",
               new String[] { "native" },
               new String[] {});

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(Framework.class);

    /**
     * Frameworks delimeter
     */
    public static final String QUERY_STRING_DELIMETER = "\\||\\+";

    /**
     * A set of framework names compatible with this
     */
    private final String[] fullCompatibilityStringSet;

    /**
     * A set of frameworks compatible with this
     */
    private volatile EnumSet<Framework> fullCompatibilitySet;

    /**
     * Full name
     */
    private final String fullName;

    /**
     * Short name
     */
    private final String[] shortNames;

    /**
     * @param fullName
     *            full name
     * @param shortNames
     *            all available short names
     * @param compabilityFrameworks
     *            frameworks compatible with this
     */
    private Framework(String fullName,
                      String[] shortNames,
                      String[] compabilityFrameworks)
    {
        this.shortNames = shortNames;
        this.fullName = fullName;
        fullCompatibilityStringSet = compabilityFrameworks;
    }

    /**
     * @return A set of frameworks compatible with this
     */
    public EnumSet<Framework> getFullCompatibilitySet()
    {
        if (fullCompatibilitySet == null)
        {
            synchronized (this)
            {
                if (fullCompatibilitySet == null)
                {
                    EnumSet<Framework> localSet = EnumSet.noneOf(Framework.class);
                    localSet.add(this);
                    for (String frameworkName : fullCompatibilityStringSet)
                    {
                        localSet.add(Framework.valueOf(frameworkName));
                    }

                    fullCompatibilitySet = localSet;
                }
            }
        }
        return fullCompatibilitySet;
    }

    /**
     * @return full name
     */
    public String getFullName()
    {
        return this.fullName;
    }

    /**
     * @return short name
     */
    public String getShortName()
    {
        return shortNames[0];
    }

    /**
     * Get frameworks collection from query string
     *
     * @param value
     *            query string
     * @return frameworks collection
     */
    public static EnumSet<Framework> parse(String value)
    {
        EnumSet<Framework> result;
        try
        {
            if (value != null && !value.isEmpty())
            {
                result = EnumSet.noneOf(Framework.class);
                String[] frameworkStrings = value.split(QUERY_STRING_DELIMETER);
                for (String frameworkString : frameworkStrings)
                {
                    Framework framework = getByShortName(frameworkString.toLowerCase());
                    if (framework == null)
                    {
                        logger.warn("Can not find framework for string : \"{}\"", new Object[] { frameworkString });
                        continue;
                    }
                    result.add(framework);
                }
            }
            else
            {
                result = EnumSet.allOf(Framework.class);
            }
        }
        catch (IllegalArgumentException e)
        {
            logger.warn("Can not find framework for string \"{}\" used default value", value, e);
            result = EnumSet.allOf(Framework.class);
        }
        return result;
    }

    /**
     * Find framework by full name
     *
     * @param fullName
     *            full name
     * @return framework or <b>null</b> if not found
     */
    public static Framework getByFullName(String fullName)
    {
        for (Framework framework : values())
        {
            if (framework.getFullName().equalsIgnoreCase(fullName))
            {
                return framework;
            }
        }
        return null;
    }

    /**
     * Find framework by short name
     *
     * @param shortName
     *            short name
     * @return framework or <b>null</b> if not found
     */
    public static Framework getByShortName(String shortName)
    {
        for (Framework framework : values())
        {
            for (String name : framework.shortNames)
            {
                if (name.equalsIgnoreCase(shortName))
                {
                    return framework;
                }
            }
        }
        return null;
    }
}
