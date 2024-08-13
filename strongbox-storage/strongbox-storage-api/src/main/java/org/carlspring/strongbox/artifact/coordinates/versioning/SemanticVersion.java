/**
 * Copyright 2012-2014, Julien Eluard and contributors
 * Copyright 2019, Carlspring Consulting & Development and contributors
 * <p>
 * This project includes software developed by Julien Eluard: https://github.com/jeluard/
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carlspring.strongbox.artifact.coordinates.versioning;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Version following semantic defined by <a href="http://semver.org/">Semantic Versioning</a> document.
 *
 * We have copied this class from the original project, as it's no longer actively developed, or maintained and since
 * we didn't want to have to maintain yet another fork.
 *
 * Notes on the modification of the original class:
 * - The original class was called Version, which we have now renamed to the more concrete SemanticVersion.
 * - The original package name has been changed to a more appropriate one for our project.
 * - Applied the coding convention of the project.
 *
 * @author Julien Eluard and contributors
 */
@Immutable
public class SemanticVersion
        implements Comparable<SemanticVersion>
{

    /**
     * {@link SemanticVersion} element. From most meaningful to less meaningful.
     */
    public enum Element
    {
        MAJOR,
        MINOR,
        PATCH,
        SPECIAL
    }

    private static final String FORMAT = "(\\d+)\\.(\\d+)(?:\\.)?(\\d*)(\\.|-|\\+)?([0-9A-Za-z-.]*)?";

    private static final Pattern PATTERN = Pattern.compile(SemanticVersion.FORMAT);

    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    private static final String SNAPSHOT_VERSION_SUFFIX = "SNAPSHOT";

    private final int major;

    private final int minor;

    private final int patch;

    private final String separator;

    private final Special special;


    public SemanticVersion(@Nonnegative final int major, @Nonnegative final int minor, @Nonnegative final int patch)
    {
        this(major, minor, patch, null, null);
    }

    public SemanticVersion(@Nonnegative final int major,
                           @Nonnegative final int minor,
                           @Nonnegative final int patch,
                           @Nullable final String separator,
                           @Nullable final String special)
    {
        if (major < 0)
        {
            throw new IllegalArgumentException(Element.MAJOR + " must be positive");
        }
        if (minor < 0)
        {
            throw new IllegalArgumentException(Element.MINOR + " must be positive");
        }
        if (patch < 0)
        {
            throw new IllegalArgumentException(Element.PATCH + " must be positive");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.separator = separator;
        this.special = parseSpecial(special);
    }

    private Special parseSpecial(String specialString)
    {
        if (specialString == null)
        {
            return null;
        }

        Special special = new Special(specialString);
        if (special.ids.length == 0)
        {
            return null;
        }

        return special;
    }

    /**
     *
     * Creates a Version from a string representation. Must match Version#FORMAT.
     *
     * @param version
     * @return
     */
    public static SemanticVersion parse(@Nonnull final String version)
    {
        final Matcher matcher = SemanticVersion.PATTERN.matcher(version);
        if (!matcher.matches())
        {
            throw new IllegalArgumentException("<" + version + "> does not match format " + SemanticVersion.FORMAT);
        }

        final int major = Integer.valueOf(matcher.group(1));
        final int minor = Integer.valueOf(matcher.group(2));
        final int patch;
        final String patchMatch = matcher.group(3);

        if (StringUtils.isNotEmpty(patchMatch))
        {
            patch = Integer.valueOf(patchMatch);
        }
        else
        {
            patch = 0;
        }

        final String separator = matcher.group(4);
        final String special = matcher.group(5);

        return new SemanticVersion(major, minor, patch, separator, "".equals(special) ? null : special);
    }

    /**
     * @param element
     * @return next {@link SemanticVersion} regarding specified {@link SemanticVersion.Element}
     */
    public SemanticVersion next(@Nonnull final SemanticVersion.Element element)
    {
        if (element == null)
        {
            throw new IllegalArgumentException("null element");
        }

        switch (element)
        {
            case MAJOR:
                if (special == null || this.minor != 0 || this.patch != 0)
                {
                    return new SemanticVersion(this.major + 1, 0, 0);
                }
                else
                {
                    return new SemanticVersion(this.major, 0, 0);
                }
            case MINOR:
                if (special == null || this.patch != 0)
                {
                    return new SemanticVersion(this.major, this.minor + 1, 0);
                }
                else
                {
                    return new SemanticVersion(this.major, this.minor, 0);
                }
            case PATCH:
                if (special == null)
                {
                    return new SemanticVersion(this.major, this.minor, this.patch + 1);
                }
                else
                {
                    return new SemanticVersion(this.major, this.minor, this.patch);
                }
            default:
                throw new IllegalArgumentException("Unknown element <" + element + ">");
        }
    }

    /**
     * if this is a pre-release version, returns the corresponding release
     * return the same version if already a release
     * @return a release version
     */
    public SemanticVersion toReleaseVersion()
    {
        return new SemanticVersion(major, minor, patch);
    }

    public boolean isInDevelopment()
    {
        return this.major == 0;
    }

    public boolean isStable()
    {
        return !isInDevelopment();
    }

    public boolean isSnapshot()
    {
        return this.special != null && this.special.isSnapshot();
    }

    /**
     * @param version version to check with
     * @return {@code true}, if supplied version is compatible with this version, {@code false} - otherwise
     */
    public boolean isCompatible(SemanticVersion version)
    {
        return version != null && this.major == version.major;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 43 * hash + this.major;
        hash = 43 * hash + this.minor;
        hash = 43 * hash + this.patch;
        hash = 43 * hash + (this.special != null ? this.special.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(@Nullable final Object object)
    {
        if (!(object instanceof SemanticVersion))
        {
            return false;
        }

        final SemanticVersion other = (SemanticVersion) object;
        if (other.major != this.major || other.minor != this.minor || other.patch != this.patch)
        {
            return false;
        }

        return (this.special == null) ? other.special == null : this.special.equals(other.special);
    }


    private static SpecialId parseSpecialId(String id)
    {
        Matcher matcher = DIGITS_ONLY.matcher(id);
        if (matcher.matches())
        {
            return new IntId(Integer.parseInt(id));
        }
        else
        {
            return new StringId(id);
        }
    }

    abstract private static class SpecialId
            implements Comparable<SpecialId>
    {

        abstract public boolean isSnapshot();

        abstract public int compareTo(IntId other);

        abstract public int compareTo(StringId other);

        abstract public int hashCode();

        abstract public boolean equals(Object obj);

    }

    private static class StringId
            extends SpecialId
    {

        private final String id;

        private StringId(String id)
        {
            this.id = id;
        }

        @Override
        public boolean isSnapshot()
        {
            return id.endsWith(SNAPSHOT_VERSION_SUFFIX);
        }

        @Override
        public int compareTo(SpecialId other)
        {
            return -other.compareTo(this);
        }

        @Override
        public String toString()
        {
            return id;
        }

        @Override
        public int compareTo(IntId other)
        {
            // Numeric identifiers always have lower precedence than non-numeric identifiers.
            return 1;
        }

        @Override
        public int compareTo(StringId other)
        {
            if (equals(other))
            {
                return 0;
            }

            return id.compareTo(other.id);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if (obj == null)
            {
                return false;
            }

            if (getClass() != obj.getClass())
            {
                return false;
            }

            StringId other = (StringId) obj;
            if (id == null)
            {
                if (other.id != null)
                {
                    return false;
                }
            }
            else if (!id.equals(other.id))
            {
                return false;
            }

            return true;
        }
    }

    private static class IntId
            extends SpecialId
    {

        private final int id;

        public IntId(int id)
        {
            this.id = id;
        }

        @Override
        public boolean isSnapshot()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return String.valueOf(id);
        }

        @Override
        public int compareTo(SpecialId other)
        {
            return -other.compareTo(this);
        }

        @Override
        public int compareTo(IntId other)
        {
            if (equals(other))
            {
                return 0;
            }

            return id - other.id;
        }

        @Override
        public int compareTo(StringId other)
        {
            //Numeric identifiers always have lower precedence than non-numeric identifiers.
            return -1;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            IntId other = (IntId) obj;
            if (id != other.id)
            {
                return false;
            }
            return true;
        }

    }

    private static class Special
            implements Comparable<Special>
    {

        private final SpecialId[] ids;

        Special(String s)
        {
            String[] split = s.split("\\.");
            ids = new SpecialId[split.length];
            for (int i = 0; i < split.length; i++)
            {
                ids[i] = parseSpecialId(split[i]);
            }
        }

        public SpecialId last()
        {
            return ids[ids.length - 1];
        }

        public boolean isSnapshot()
        {
            return last().isSnapshot();
        }

        @Override
        public int compareTo(Special other)
        {
            if (equals(other))
            {
                return 0;
            }

            int min = Math.min(other.ids.length, ids.length);
            for (int i = 0; i < min; i++)
            {
                int c = ids[i].compareTo(other.ids[i]);
                if (c != 0)
                {
                    return c;
                }
            }
            int max = Math.max(other.ids.length, ids.length);
            if (max != min)
            {
                if (ids.length > other.ids.length)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
            return 0;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(ids);

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if (obj == null)
            {
                return false;
            }

            if (getClass() != obj.getClass())
            {
                return false;
            }

            Special other = (Special) obj;
            if (!Arrays.equals(ids, other.ids))
            {
                return false;
            }

            return true;
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < ids.length; i++)
            {
                SpecialId s = ids[i];
                if (i != 0)
                {
                    builder.append(".");
                }

                builder.append(s);
            }

            return builder.toString();
        }
    }

    @Override
    public int compareTo(final SemanticVersion other)
    {
        if (equals(other))
        {
            return 0;
        }

        if (this.major < other.major)
        {
            return -1;
        }
        else if (this.major == other.major)
        {
            if (this.minor < other.minor)
            {
                return -1;
            }
            else if (this.minor == other.minor)
            {
                if (this.patch < other.patch)
                {
                    return -1;
                }
                else if (this.patch == other.patch)
                {
                    if (this.special != null && other.special != null)
                    {
                        return this.special.compareTo(other.special);
                    }
                    else if (other.special != null)
                    {
                        return 1;
                    }
                    else if (this.special != null)
                    {
                        return -1;
                    } // else handled by previous equals check
                }
            }
        }
        return 1; //if this (major, minor or patch) is > than other
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.major).append(".").append(this.minor).append(".").append(this.patch);

        if (this.separator != null)
        {
            builder.append(this.separator);
        }

        if (this.special != null)
        {
            builder.append(this.special);
        }

        return builder.toString();
    }

}
