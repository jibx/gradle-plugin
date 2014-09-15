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

import org.jibx.binding.Compile

/**
 * Runs the JiBX binding compiler.
 *
 * @author                        <a href="mailto:mail@andreasbrenk.com">Andreas Brenk</a>
 * @author                        <a href="mailto:frankm.os@gmail.gom">Frank Mena</a>
 * @author                        <a href="mailto:don@tourgeek.com">Don Corley</a>
 * @author                        <a href="mailto:shutyaev@gmail.com">Alexander Shutyaev</a>
 */
abstract class AbstractBaseBindingTask extends AbstractJibxTask {

    //~ Instance fields ------------------------------------------------------------------------------------------------

    /**
     * Control flag for test loading generated/modified classes.
     */
    protected boolean load;

    /**
     * Control flag for test loading generated/modified classes.
     */
    protected boolean validate = true;

    /**
     * Control flag for verbose processing reports.
     */
    protected boolean verbose;

    /**
     * Control flag for verifying generated/modified classes with BCEL.
     */
    protected boolean verify;

    //~ Methods --------------------------------------------------------------------------------------------------------

    /**
     * Determines if running in single- or multi-module mode, collects all bindings and finally runs the binding
     * compiler.
     */
    public void run() {
        checkConfiguration();

        String mode;
        String[] bindings;
        String[] classpaths;

        if (isMultiModuleMode()) {
            if (isRestrictedMultiModuleMode()) {
                mode = "restricted multi-module";
            } else {
                mode = "multi-module";
            }
            bindings = getMultiModuleBindings();
            classpaths = getMultiModuleClasspaths();
        } else {
            mode = "single-module";
            bindings = getSingleModuleBindings();
            classpaths = getSingleModuleClasspaths();
        }
        getBaseBindings(bindings);	// This is not a mistake. I call getDependentBindingPaths to unzip the archives
        	// But I do not add the bindings to my list (since they are base binding.. already included in my binding file)

        if (bindings.length == 0) {
            logger.info("Not running JiBX binding compiler (" + mode + " mode) - no binding files");
        } else {
            logger.info("Running JiBX binding compiler (" + mode + " mode) on " + bindings.length
                          + " binding file(s)");
            compile(classpaths, bindings);
        }
    }

    /**
     * Verifies the plugins configuration and sets default values if needed.
     * Note: Remember to call inherited methods first.
     */
    protected void checkConfiguration() {
    	super.checkConfiguration();

    	if ((excludeSchemaBindings.size() == 0) && (includeSchemaBindings.size() == 0))
    		includeSchemaBindings.add(DEFAULT_INCLUDE_BINDINGS);
    }

    /**
     * Creates and runs the JiBX binding compiler.
     */
    private void compile(String[] classpaths, String[] bindings) {
        Compile compiler = new Compile();
        compiler.setLoad(this.load);
        compiler.setSkipValidate(!this.validate);
        compiler.setVerbose(this.verbose);
        compiler.setVerify(this.verify);
        compiler.compile(classpaths, bindings);
    }

    /**
     * Get the binding files directory.
     * @return The binding files directory.
     */
    @Override
    protected String getSchemaBindingDirectory()
    {
    	if (super.schemaBindingDirectory != null)
    		return super.schemaBindingDirectory;
    	return super.getSchemaBindingDirectory();
    }

}
