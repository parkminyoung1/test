package org.carlspring.strongbox.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * Re-implements URL processing to let {path:.+} behaves like **. Also populates necessary path variables.
 *
 * @author Alex Oreshkevich
 */
@Component
public class CustomAntPathMatcher
        extends AntPathMatcher
{

    private static final Logger logger = LoggerFactory.getLogger(CustomAntPathMatcher.class);

    private String pathSeparator;

    // pattern that will be processed in the same way as **
    public static final String TWO_STARS_ANALOGUE = ".+";

    /**
     * Create a new instance with the {@link #DEFAULT_PATH_SEPARATOR}.
     */
    public CustomAntPathMatcher()
    {
        this.pathSeparator = DEFAULT_PATH_SEPARATOR;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Any URL pattern like {PATH_VARIABLE:.+} will be treated as **, it means like any subPath with any numbers of / in it.
     *
     * @param pattern   the pattern to match against
     * @param path      the path String to test
     * @param fullMatch whether a full pattern match is required (else a pattern match
     *                  as far as the given base path goes is sufficient)
     * @return {@code true} if the supplied {@code path} matched, {@code false} if it didn't
     */
    @Override
    public boolean doMatch(String pattern,
                           String path,
                           boolean fullMatch,
                           Map<String, String> uriTemplateVariables)
    {
        String pathVariableName = null;

        // pattern should ends with ':.+}' char sequence (but not with }.xml or }.json)
        if (pattern.endsWith(":" + TWO_STARS_ANALOGUE + "}") && pattern.lastIndexOf("}") == pattern.length() - 1)
        {

            // extract actual name of the path variable from pattern (and then replace :.+ with **)
            pathVariableName = getPathVariableName(pattern);
            pattern = pattern.replace("{" + pathVariableName + ":" + TWO_STARS_ANALOGUE + "}", "**");
        }

        // get pattern matching result from superclass (the default one)
        // if .+ was present it was replaced by **, so we could reuse base class implementation
        boolean defaultMatchResult = super.doMatch(pattern, path, fullMatch, uriTemplateVariables);

        if (pathVariableName != null && defaultMatchResult)
        {
            if (uriTemplateVariables == null)
            {
                uriTemplateVariables = new LinkedHashMap<>();
            }

            uriTemplateVariables.put(pathVariableName, getPathVariableValue(pattern, path));
        }

        logger.trace("[doMatch] pattern {}\n\tpath {}\n\tfullMatch {}\n\turiTemplateVariables {}\n\tdefaultMatchResult {}",
                     pattern, path, fullMatch, uriTemplateVariables, defaultMatchResult);

        return defaultMatchResult;
    }

    /**
     * Extract path variable name from actual pattern.
     *
     * @param pattern URL pattern to parse
     * @return path variable name
     */
    private String getPathVariableName(String pattern)
    {
        // get the rest of source path based on the path variables count and path prefix
        String[] patternDirs = pattern.split(pathSeparator);
        int subPathIndex = patternDirs.length;

        return patternDirs[subPathIndex - 1].substring(1, patternDirs[subPathIndex - 1].indexOf(":"));
    }

    /**
     * Extract path sub value that matches actual pattern.
     *
     * @param pattern URL pattern to parse
     * @param path    reduced URL path (without host)
     * @return path variable value
     */
    private String getPathVariableValue(String pattern,
                                        String path)
    {
        logger.trace("pattern {}", pattern);
        logger.trace("path {}", path);

        // get the rest of source path based on the path variables count and path prefix
        String[] pathDirs = path.split(pathSeparator);
        String[] patternDirs = pattern.split(pathSeparator);

        logger.trace("pathDirs {} {}", pathDirs.length, Arrays.deepToString(pathDirs));
        logger.trace("patternDirs {} {}", patternDirs.length, Arrays.deepToString(patternDirs));

        int pathSubDirCount = pathDirs.length;
        int subPathIndex = patternDirs.length;

        // for cases like /metadata/{storageId}/{repositoryId}/**  and  /metadata/storage0/releases/
        if (pathSubDirCount + 1 == subPathIndex)
        {
            return "";
        }

        int subPathLength = 0;

        final int pathSeparatorLength = pathSeparator.length();

        for (int i = 1; i < subPathIndex - 1; i++)
        {
            String subPath = pathDirs[i];

            logger.trace("Append subPath length {} for subPath {}", subPath.length(), subPath);

            subPathLength += subPath.length();
            subPathLength += pathSeparatorLength;
        }

        subPathLength += pathSeparatorLength; // include last path separator

        String pathVarValue = path.substring(subPathLength);

        logger.trace("subPathLength {}", subPathLength);
        logger.trace("pathVarValue {}", pathVarValue);

        return pathVarValue;
    }
}
