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

import org.jibx.schema.codegen.CodeGen

/**
 * Generates Java sources from XSD schemas.
 * @author                        <a href="mailto:jerome.bernard@elastic-grid.com">Jerome Bernard</a>
 * @author                        <a href="mailto:don@tourgeek.com">Don Corley</a>
 * @author                        <a href="mailto:shutyaev@gmail.com">Alexander Shutyaev</a>
 */
abstract class AbstractCodeGenTask extends AbstractJibxTask {

	/**
	 * Control flag for verbose processing reports.
	 */
	protected boolean verbose

	/**
	 * Default package for code generated from schema definitions with no namespace.
	 */
	protected String defaultPackage

	/**
	 * Include pattern for customization files.
	 */
	protected ArrayList<String> customizations = new ArrayList<String>()
	
	AbstractCodeGenTask customization(String customization) {
		customizations.add(customization)
		return this
	}
	
	/**
	 * The directory or web location which contains XSD files.
	 * This can be the schema directory, url, or the base url for a list of
	 * 'includes' schema (<a href="schema-codegen.html">See example</a>).
	 * Defaults to "src/main/config" (or "src/test/config" for test cases).
	 */
	protected String schemaLocation

	/**
	 * Namespace applied in code generation when no-namespaced schema definitions are found (to generate
	 * no-namespaced schemas as though they were included in a particular namespace)
	 */
	protected String defaultNamespace

	/**
	 * Exclude pattern for schema files.
	 */
	protected ArrayList<String> excludeSchemas = new ArrayList<String>()
	
	AbstractCodeGenTask excludeSchema(String excludeSchema) {
		excludeSchemas.add(excludeSchema)
		return this
	}
	
	/**
	 * Include pattern for schema files.<br/>
	 * <b>Note: </b>Uses the standard filter format described in the plexus
	 * <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/DirectoryScanner.html">DirectoryScanner</a>.<br/>
	 * <b>Defaults value is:</b> *.xsd.
	 */
	protected ArrayList<String> includeSchemas = new ArrayList<String>()
	
	AbstractCodeGenTask includeSchema(String includeSchema) {
		includeSchemas.add(includeSchema)
		return this
	}
	
	/**
	 * Extra options to be given for customization via CLI.<p/>
	 * Enter extra customizations or other command-line options.<br/>
	 * The extra customizations are described on the 
	 * <a href="/fromschema/codegen-customs.html">CodeGen customizations page</a><br/>
	 * The single character CodeGen commands may also be supplied here.<br/>
	 * For example, to include a base binding file (-i) and prefer-inline code, supply the following options:<br/>
	 * <code>
	 * options = [i: 'base-binding.xml', prefer-inline: 'true']
	 * </code>
	 */
	protected Map<String,String> options = new HashMap<String,String>()
	
	AbstractCodeGenTask option(Map<String, String> options) {
		this.options.putAll(options)
		return this
	}
	
	public void run() {
		checkConfiguration();

		List<String> args = new ArrayList<String>();
		for (Map.Entry<String,String> entry : options.entrySet()) {
			String option = "--" + entry.getKey() + "=" + entry.getValue();
			if ((entry.getKey().toString().length() == 1) && (Character.isLowerCase(entry.getKey().toString().charAt(0)))) {
				logger.debug("Adding option : -" + entry.getKey() + " " + entry.getValue());
				args.add("-" + entry.getKey());
				args.add(entry.getValue());
			}
			else {
				logger.debug("Adding option: " + option);
				args.add(option);
			}
		}
		if (verbose)
			args.add("-v");
		if (defaultPackage != null) {
			args.add("-n");
			args.add(defaultPackage);
		}
		args.add("-t");
		args.add(getFullPath(getSchemaBindingDirectory()));
		if (customizations.size() > 0) {
			args.add("-c");
			for (String customization : customizations) {
				args.add(customization);
			}
		}
		if (defaultNamespace != null) {
			args.add("-u");
			args.add(defaultNamespace);
		}

		String allBindings = "";
		String mode;
		String[] bindings = new String[0];
		String[] classpaths;
		if (isMultiModuleMode()) {
			if (isRestrictedMultiModuleMode()) {
				mode = "restricted multi-module";
			} else {
				mode = "multi-module";
			}
			classpaths = getMultiModuleClasspaths();
		} else {
			mode = "single-module";
			classpaths = getSingleModuleClasspaths();
		}

		bindings = getBaseBindings(bindings);	// Based bindings
		for (String binding : bindings) {
			if (allBindings.length() > 0)
				allBindings = allBindings + ",";
			allBindings = allBindings + binding;
		}
		if (allBindings.length() > 0)
		{
			args.add("-i");
			args.add(allBindings);
		}

		List<String> schemas = getSchemas(getFullPath(getSchemaLocation()));
		for (String schema  : schemas) {
			File file = new File(schema);
			if (file.exists())
				args.add(new File(schema).toURI().toString());
			else
			{	// Not a file, try a URL
				try {
					args.add(new URL(schema).toURI().toString());
				} catch (URISyntaxException e) {
					logger.warn("Target schema is not a valid file or URL - Passing location as is");
					args.add(schema);
				} catch (MalformedURLException e) {
					logger.warn("Target schema is not a valid file or URL - Passing location as is");
					args.add(schema);
				}
			}
		}

		logger.debug("Adding " + getSchemaBindingDirectory() + " as source directory...");
		project.sourceSets.getByName(getSourceSetName()).java.srcDir getFullPath(getSchemaBindingDirectory())

		logger.info("Generating Java sources in " + getSchemaBindingDirectory() + " from schemas available in " + getSchemaLocation() + "...");
		CodeGen.main((String[]) args.toArray(new String[args.size()]));
	}

	abstract String getSourceSetName()

	/**
	 * Get the binding path name for a single module binding.
	 * @param basedir
	 * @param includeBinding
	 * @return
	 */
	public String getSingleModuleBindingPath(String basedir, String includeBinding)
	{
		if (!includeBinding.contains(","))
		{	// Possible relative path
			File file = new File(includeBinding);
			if (!file.isAbsolute())
			{	// Possible relative path
				try {
					file = new File(addToPath(basedir, includeBinding));
					if (file.exists())
						includeBinding = addToPath(basedir, includeBinding);
				} catch (Exception e) {
					// Exception = use relative path
				}
			}
		}
		return includeBinding;
	}
	/**
	 * Verifies the plugins configuration and sets default values if needed.
	 * Note: Remember to call inherited methods first.
	 */
	protected void checkConfiguration() {
		super.checkConfiguration();

		if (this.includeSchemas.size() == 0) {
			this.includeSchemas.add(DEFAULT_INCLUDES);
		}
	}

	/**
	 * Returns all bindings in the given directory according to the configured include/exclude patterns.
	 */
	private List<String> getSchemas(String path) {
		return this.getIncludedFiles(path, this.includeSchemas, this.excludeSchemas);
	}

	/**
	 * Get the schema files directory.
	 * @return The binding files directory.
	 */
	protected String getSchemaLocation()
	{
		if (schemaLocation != null)
			return schemaLocation;
		return getDefaultSchemaLocation();
	}

	protected void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation
	}

	/**
	 * Get the schema files directory.
	 * @return The binding files directory.
	 */
	abstract String getDefaultSchemaLocation();

	/**
	 * Get the binding files directory.
	 * @return The binding files directory.
	 */
	protected String getSchemaBindingDirectory()
	{
		if (super.schemaBindingDirectory != null)
			return super.schemaBindingDirectory;
		return super.getSchemaBindingDirectory();	// This is not used for code-gen, but the binding compiler should be looking here
	}

}