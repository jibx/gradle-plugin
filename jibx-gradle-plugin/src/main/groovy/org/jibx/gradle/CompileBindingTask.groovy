/*
 * Copyright (c) 2004-2005, Dennis M. Sosnoski All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.gradle

import org.gradle.api.Project

/**
 * Runs the JiBX binding compiler.
 *
 * @author                        <a href="mailto:mail@andreasbrenk.com">Andreas Brenk</a>
 * @author                        <a href="mailto:frankm.os@gmail.gom">Frank Mena</a>
 * @author                        <a href="mailto:don@tourgeek.com">Don Corley</a>
 * @author                        <a href="mailto:shutyaev@gmail.com">Alexander Shutyaev</a>
 */
public class CompileBindingTask extends AbstractBindingTask {

    /**
     * Returns the name of the configuration whose dependencies will be searched for bindings
     */
    String getConfigurationNameForBindings() {
        return 'compile'
    }

    /**
     * Returns the build output directory of the given project.
     */
	protected Set<File> getProjectCompileClasspathElements(Project project) {
            HashSet<File> result = new HashSet<File>()
            result.addAll(project.sourceSets.main.compileClasspath.files)
            result.addAll(project.sourceSets.main.output.files)
            return result
    }

    /**
     * Get the binding files directory.
     * @return The binding files directory.
     */
    @Override
    protected String getDefaultSchemaBindingDirectory()
    {
    	File bindingDir = new File(getFullPath("src/main/config"));
    	if (!bindingDir.exists())
    	{
    		bindingDir = new File(getFullPath("build/generated-sources"));
        	if (bindingDir.exists())
        		return "build/generated-sources";
    	}
    	return "src/main/config";
    }
}
