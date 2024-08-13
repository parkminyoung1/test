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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * A class that describes dependencies on assemblies that come with the .NET
 * framework itself.
 *
 * @author sviridov
 */
@XmlRootElement(name = "frameworkAssembly", namespace = Nuspec.NUSPEC_XML_NAMESPACE_2011)
@XmlAccessorType(XmlAccessType.NONE)
public class FrameworkAssembly implements Serializable
{

    @XmlAttribute(name = "assemblyName")
    private String assemblyName;

    @XmlAttribute(name = "targetFramework")
    @XmlJavaTypeAdapter(AssemblyTargetFrameworkAdapter.class)
    private EnumSet<Framework> targetFrameworks;

    public String getAssemblyName()
    {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName)
    {
        this.assemblyName = assemblyName;
    }

    /**
     * @return The frameworks for which the assembly is intended
     */
    public EnumSet<Framework> getTargetFrameworks()
    {
        if (targetFrameworks == null)
        {
            targetFrameworks = EnumSet.allOf(Framework.class);
        }
        return targetFrameworks;
    }

    /**
     * @param targetFrameworks
     *            the frameworks for which the assembly is intended
     */
    public void setTargetFrameworks(EnumSet<Framework> targetFrameworks)
    {
        this.targetFrameworks = targetFrameworks;
    }
}
