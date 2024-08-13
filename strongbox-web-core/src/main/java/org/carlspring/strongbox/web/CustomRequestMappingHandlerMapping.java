package org.carlspring.strongbox.web;

import org.carlspring.strongbox.configuration.StoragesConfigurationManager;
import org.carlspring.strongbox.interceptors.ArtifactRequestInterceptor;
import org.carlspring.strongbox.interceptors.RepositoryRequestInterceptor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author sbespalov
 * @author Przemyslaw Fusik
 */
public class CustomRequestMappingHandlerMapping
        extends RequestMappingHandlerMapping
{

    @Inject
    private StoragesConfigurationManager configurationManager;
    
    @Override
    protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors)
    {
        mappedInterceptors.add(new RepositoryRequestInterceptor());
        mappedInterceptors.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(obtainApplicationContext(),
                                                                                 ArtifactRequestInterceptor.class, true,
                                                                                 false)
                                                  .values());
    }

    @Override
    protected RequestCondition<ExposableRequestCondition> getCustomTypeCondition(Class<?> handlerType)
    {
        return Optional.ofNullable(AnnotationUtils.findAnnotation(handlerType, LayoutRequestMapping.class))
                       .map(m -> new LayoutRequestCondition(configurationManager, m.value()))
                       .orElse(null);
    }

    @Override
    protected void handleMatch(RequestMappingInfo info,
                               String lookupPath,
                               HttpServletRequest request)
    {
        if (info.getCustomCondition() instanceof ExposableRequestCondition)
        {
            ((ExposableRequestCondition) info.getCustomCondition()).expose(request);
        }
        super.handleMatch(info, lookupPath, request);
    }

}
