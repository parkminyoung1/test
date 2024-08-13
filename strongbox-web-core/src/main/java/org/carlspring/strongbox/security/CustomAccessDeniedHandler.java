package org.carlspring.strongbox.security;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author Przemyslaw Fusik
 */
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler
{

    @Inject
    private ObjectMapper objectMapper;


    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException
    {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println(objectMapper.writeValueAsString(new ErrorResponseEntityBody("forbidden")));
        response.flushBuffer();
    }
}
