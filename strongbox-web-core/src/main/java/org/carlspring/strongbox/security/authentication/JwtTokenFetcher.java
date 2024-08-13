package org.carlspring.strongbox.security.authentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public interface JwtTokenFetcher
{

    Logger logger = LoggerFactory.getLogger(JwtTokenFetcher.class);

    String AUTHORIZATION_HEADER = "Authorization";

    String AUTHORIZATION_COOKIE = "access_token";

    String BEARER_AUTHORIZATION_PREFIX = "Bearer";

    Pattern BEARER_PATTERN = Pattern.compile("Bearer (.*)");

    default Optional<String> getToken(HttpServletRequest request)
    {
        // give priority to header based authentication, because it is more likely to be present
        String tokenHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(tokenHeader))
        {
            Matcher headerMatcher = BEARER_PATTERN.matcher(tokenHeader);

            if (headerMatcher.matches())
            {
                String token = headerMatcher.group(1);
                logger.debug("Bearer Authorization header found with token {}", token);
                return Optional.of(token);
            }
        }

        // fallback - check if a cookie is present (necessary for EventSource; check gh#1046).
        Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
                                             .filter(c -> c.getName()
                                                           .equals(AUTHORIZATION_COOKIE)).findFirst();
        if (tokenCookie.isPresent())
        {
            String token = tokenCookie.get().getValue();
            logger.debug("Bearer Authorization found in cookie with token {}", token);
            return Optional.of(token);
        }

        return Optional.empty();
    }

}
