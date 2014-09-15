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

import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile

import org.apache.commons.io.FilenameUtils
import org.codehaus.plexus.util.DirectoryScanner
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.TaskAction

/**
 * Runs the JiBX binding compiler.
 *
 * @author                        <a href="mailto:mail@andreasbrenk.com">Andreas Brenk</a>
 * @author                        <a href="mailto:frankm.os@gmail.gom">Frank Mena</a>
 * @author                        <a href="mailto:don@tourgeek.com">Don Corley</a>
 * @author                        <a href="mailto:shutyaev@gmail.com">Alexander Shutyaev</a>
 */
public abstract class AbstractJibxTask extends DefaultTask {

    //~ Static fields/initializers -------------------------------------------------------------------------------------

    static final String DEFAULT_INCLUDE_BINDINGS = "binding.xml";
    static final String DEFAULT_INCLUDES = "*.xsd";

    /**
     * A list of modules to search for binding files in the format: groupID:artifactID
     */
    protected HashSet<String> modules = new HashSet<String>()
	
	AbstractJibxTask module(String module) {
		modules.add(module)
		return this
	}

    /**
     * A list of modules or files to search for base binding files.
     * This can specify files in the local directory or files stored in your dependencies.
     *
     * If your based binding files are in a local file system, specify them as follows:<br/>
     * &lt;includeBaseBindings&gt;<br/>
     * &nbsp;&nbsp;&lt;includeBaseBinding&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;directory&gt;src/main/config&lt;/directory&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;includes&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include&gt;base-binding.xml&lt;/include&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/includes&gt;<br/>
     * &nbsp;&nbsp;&lt;/includeBaseBinding&gt;<br/>
     * &nbsp;&lt;/includeBaseBindings&gt;<br/>
     * 
     * If your based binding files are in a artifact that is one of your dependencies:<br/>
     * &lt;includeBaseBindings&gt;<br/>
     * &nbsp;&nbsp;&lt;includeBaseBinding&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId&gt;com.mycompany.baseschema&lt;/groupId&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId&gt;base-schema&lt;/artifactId&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;classifier&gt;bindings&lt;/classifier&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;directory&gt;META-INF&lt;/directory&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;includes&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;include&gt;base-binding.xml&lt;/include&gt;<br/>
     * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/includes&gt;<br/>
     * &nbsp;&nbsp;&lt;/includeBaseBinding&gt;<br/>
     * &lt;/includeBaseBindings&gt;<br/>
     * 
     * The classifier is optional (if your binding file is not in the main artifact)
     * A version is not necessary, since this declaration must be on your list of dependencies.<br/>
     * <b>Note: </b>For file filters, use the standard filter format described in the plexus
     * <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/DirectoryScanner.html">DirectoryScanner</a>.<br/>
     * <b>Defaults value is:</b> binding.xml.
     * Include existing bindings and use mappings from the bindings for matching schema global definitions.
     * (this is the basis for modular code generation)<br/>
     * <b>Note:</b> If directory is not specified, relative paths start at &lt;baseBindingDirectory&gt;.
     */
    protected HashSet<IncludeBaseBinding> includeBaseBindings = new HashSet<IncludeBaseBinding>()
	
	AbstractJibxTask includeBaseBinding(Map ibb) {
		includeBaseBindings.add(ibb.findAll { k, v -> k in IncludeBaseBinding.metaClass.properties*.name})
		return this
	}
	
    /**
     * Control flag multi-module mode.
     */
    protected boolean multimodule;

    /**
     * The directory which contains schema binding files.
     * Defaults to "src/main/config" (or "src/test/config" for test goals).
     * For code-gen or if the default directory does not exist, defaults to
     * "build/generated-sources" (or "build/generated-test-sources" for test goals).
     */
    protected String schemaBindingDirectory;

    /**
     * Exclude pattern for schema binding files.
     */
    protected ArrayList<String> excludeSchemaBindings = new ArrayList<String>()
	
	AbstractJibxTask excludeSchemaBinding(String excludeSchemaBinding) {
		excludeSchemaBindings.add(excludeSchemaBinding)
		return this
	}
	
    /**
     * Include pattern for schema binding files.<br/>
     * <b>Note: </b>Uses the standard filter format described in the plexus
     * <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/DirectoryScanner.html">DirectoryScanner</a>.<br/>
     * <b>Defaults value is:</b> binding.xml.
     * Include existing bindings and use mappings from the bindings for matching schema global definitions.
     * (this is the basis for modular code generation)
     * Include base bindings as follows:<br/>
     * &lt;includeSchemaBindings&gt;<br/>
     * &nbsp;&nbsp;&lt;includeSchemaBinding&gt;base-binding.xml&lt;/includeSchemaBinding&gt;<br/>
     * &lt;/includeSchemaBindings&gt;<br/>
     * <b>Note:</b> Relative paths start at &lt;directory&gt;.
     */
    protected ArrayList<String> includeSchemaBindings = new ArrayList<String>()
	
	AbstractJibxTask includeSchemaBinding(String includeSchemaBinding) {
		includeSchemaBindings.add(includeSchemaBinding)
		return this
	}
	
    /**
     * Get the default location of the base binding files.<br/>
     * <b>Defaults value is:</b> schemaBindingDirectory.
     */
    protected String baseBindingDirectory;

    //~ Methods --------------------------------------------------------------------------------------------------------

    /**
     * Determines if running in single- or multi-module mode, collects all bindings and finally runs the binding
     * compiler.
     */
    @TaskAction
    public abstract void run()

    /**
     * Returns the basedir of the given project.
     */
    protected String getProjectBasedir(Project project) {
        return FilenameUtils.normalize(project.projectDir.absolutePath)
    }

    /**
     * Verifies the plugins configuration and sets default values if needed.
     * Note: Remember to call inherited methods first.
     */
    protected void checkConfiguration() {
    	
        if (this.modules.size() > 0) {
            this.multimodule = true;
        } else {
            this.modules = null;
        }
        
    }
    
    /**
     * Returns all bindings in the given directory according to the configured include/exclude patterns.
     */
    protected List<String> getIncludedFiles(String path, ArrayList<String> includeFiles, ArrayList<String> excludeFiles) {
        List<String> bindingSet = new ArrayList<String>();

        File bindingdir = new File(path);
        if (!bindingdir.exists())
        {	// Probably a url...
        	try {
				URL url = new URL(path);
				if ("file".equalsIgnoreCase(url.getProtocol()))
					return bindingSet;	// If you pass a file or directory it must exist, so I can scan it; return empty dir
			} catch (MalformedURLException e) {
				return bindingSet;
			}
			String[] includes = (String[]) includeFiles.toArray(new String[includeFiles.size()]);
			if ((includes != null) && (includes .length > 0))
			{
				if (includes.length == 1)
					if (DEFAULT_INCLUDES == includes[0])
						if ((!path.endsWith("/")) && (!path.endsWith(File.separator)))
						{
							bindingSet.add(path);
							return bindingSet;	// Special case, user supplied complete URL as schemaLocation
						}
				for (String include : includes) {
					bindingSet.add(path + include);
				}
			}
			else
				bindingSet.add(path);	// Only supplied a single URL
        }
        if (!bindingdir.isDirectory()) {
            return bindingSet;
        }

        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(bindingdir);
        String[] includes = (String[]) includeFiles.toArray(new String[includeFiles.size()]);
        scanner.setIncludes(includes);
        String[] excludes = (String[]) excludeFiles.toArray(new String[excludeFiles.size()]);
        scanner.setExcludes(excludes);
        scanner.scan();

        String[] files = scanner.getIncludedFiles();
        String absolutePath = bindingdir.getAbsolutePath();
        for (int i = 0; i < files.length; i++) {
            String file = absolutePath + File.separator + files[i];
            bindingSet.add(file);
        }

        return bindingSet;
    }
    /**
     * Fix this file path.
     * If it is absolute, leave it alone, if it is relative prepend the default path or the base path.
     * @param filePath The path to fix
     * @param defaultPath The base path to use.
     * @return The absolute path to this file.
     */
    protected String fixFilePath(String filePath, String defaultPath)
    {
    	if (filePath == null)
    		return null;
        boolean relativePath = true;
        
        String basedir = defaultPath;
        if (basedir == null)
        	basedir = getProjectBasedir(this.project);
    	File file = new File(filePath);
    	if (!file.isAbsolute())
        {	// Possible relative path
    		try {
                file = new File(addToPath(basedir, filePath));
                if (file.exists())
                	filePath = addToPath(basedir, filePath);
            } catch (Exception e) {
            	// Exception = use relative path
            }
        }
        return filePath;
    }

    /**
     * Normalizes all entries.
     */
    /* package */ String[] normalizeClasspaths(Set<File> classpathSet) {
        String[] classpaths = new String[classpathSet.size()];

        int i = 0
        classpathSet.each() { file ->
            classpaths[i++] = FilenameUtils.normalize(file.absolutePath);
        }

        return classpaths;
    }

    /**
     * Returns all bindings in the given directory according to the configured include/exclude patterns.
     */
    List<String> getBindings(String path) {
        return this.getIncludedFiles(path, this.includeSchemaBindings, this.excludeSchemaBindings);
    }

    /**
     * Returns all binding in the current project and all referenced projects (multi-module mode)
     */
    String[] getMultiModuleBindings() {
        Set<String> basedirSet = getProjectBasedirSet(this.project);
        Set<String> bindingSet = new HashSet<String>();
        // No Need to add project (single module mode) dir, it is included in the baseDirSet

        for (String basedir : basedirSet) {
            if (basedir.equals(getProjectBasedir(this.project)))
            	basedir = getFullPath(getSchemaBindingDirectory());	// Main project
            else
            	basedir = addToPath(basedir, getSchemaBindingDirectory());
            List<String> bindingList = getBindings(basedir);
            bindingSet.addAll(bindingList);
        }

        return (String[]) bindingSet.toArray(new String[bindingSet.size()]);
    }

    /**
     * Returns the classpath for the binding compiler running in multi-module mode.
     */
    String[] getMultiModuleClasspaths() {
        Set<File> classpathSet = getProjectCompileClasspathElementsSet(this.project);

        return normalizeClasspaths(classpathSet);
    }

    /**
     * Returns the basedir of the given project and all (or all in "modules" specified) reference projects.
     */
	private Set<String> getProjectBasedirSet(Project project) {
        Set<String> basedirSet = new HashSet<String>();
        basedirSet.add(getProjectBasedir(project));

        for (Project projectReference : (Collection<Project>)project.childProjects.values()) {
            String projectId = projectReference.getGroupId() + ":" + projectReference.getArtifactId();

            if ((this.modules == null) || this.modules.contains(projectId)) {
                basedirSet.add(getProjectBasedir(projectReference));
            }
        }

        return basedirSet;
    }

    /**
     * Returns the build output directory of the given project.
     */
    protected abstract Set<File> getProjectCompileClasspathElements(Project project);

    /**
     * Returns the build output directory of the given project and all its reference projects.
     */
	private Set<File> getProjectCompileClasspathElementsSet(Project project) {
        Set<File> classpathElements = new HashSet<File>();
        classpathElements.addAll(getProjectCompileClasspathElements(project));

        for (Project projectReference : (Collection<Project>)project.childProjects.values()) {
            classpathElements.addAll(getProjectCompileClasspathElements(projectReference));        	
        }

        return classpathElements;
    }

    /**
     * Returns all bindings in the current project (single-module mode).
     */
    String[] getSingleModuleBindings() {
        String bindingdir = getFullPath(getSchemaBindingDirectory());
        List<String> bindingSet = getBindings(bindingdir);

        return (String[]) bindingSet.toArray(new String[bindingSet.size()]);
    }

    /**
     * Returns the classpath for the binding compiler running in single-module mode.
     */
    String[] getSingleModuleClasspaths() {
        Set<File> classpathSet = getProjectCompileClasspathElements(this.project);

        return normalizeClasspaths(classpathSet);
    }

    /**
     * Get the binding path name for a includeBaseBinding binding.
     * This method actually unjars the binding file(s) from dependent resources.
     * @param basedir
     * @param includeBinding
     * @return
     */
	public String[] getBaseBindings(String[] bindings)
	{
    	List<String> bindingSet = new ArrayList<String>();
    	for (String binding : bindings) {
    		bindingSet.add(binding);
    	}
        if (includeBaseBindings.size() > 0)
    	{
	        for (IncludeBaseBinding includeDependentBinding : includeBaseBindings)
	        {
	            if (includeDependentBinding == null)
	            	continue;
				if ((includeDependentBinding.groupId == null ) || (includeDependentBinding.artifactId == null))
					bindingSet = addManualDependentBinding(includeDependentBinding, bindingSet);	// Probably a reference to a binding on the filesystem
				else
					bindingSet = addDependentBinding(includeDependentBinding, bindingSet);
	        }
        }

        return bindingSet.toArray(new String[bindingSet.size()]);
	}
	/**
	 * Add bindings on the filesystem.
	 * @param includeDependentBinding
	 * @param bindingSet
	 * @return
	 */
	public List<String> addManualDependentBinding(IncludeBaseBinding includeDependentBinding, List<String> bindingSet)
	{
		String directory = includeDependentBinding.directory;
		if (directory == null)
			directory = this.getBaseBindingDirectory();
		String path = fixFilePath(directory, null);
        File bindingdir = new File(path);
        if (!bindingdir.exists())
        {	// Probably a url...
        	try {
				URL url = new URL(path);
				if (!"file".equalsIgnoreCase(url.getProtocol()))
				{
					String[] includes = (String[]) includeDependentBinding.includes.toArray(new String[includeDependentBinding.includes.size()]);
					if ((includes != null) && (includes .length > 0))
					{
						for (String include : includes) {
							bindingSet.add(path + include);
						}
					}
					else
						bindingSet.add(path);	// Only supplied a single URL
				}
				return bindingSet;
			} catch (MalformedURLException e) {
				// Try as a file
			}
        }

        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir(bindingdir);
        if (includeDependentBinding.includes.size() == 1)
        	if (includeDependentBinding.includes.get(0) == null)
        		includeDependentBinding.includes.remove(0);	// Sometime when using params a null include is passed, so skip it.
        String[] includes = (String[]) includeDependentBinding.includes.toArray(new String[includeDependentBinding.includes.size()]);
        scanner.setIncludes(includes);
        //String[] excludes = (String[]) excludes.toArray(new String[excludeFiles.size()]);
        //scanner.setExcludes(excludes);
        for (String include : includes)
        {
        	File file = new File(include);
        	if (file.isAbsolute())
        	{	// If the files specify an absolute path, don't scan
		        for (String includez : includes)
		        {
		            bindingSet.add(includez);
		        }
		        return bindingSet;
        	}
        }
        if (!bindingdir.isDirectory()) {
            return bindingSet;
        }
        scanner.scan();

        String[] files = scanner.getIncludedFiles();
        String absolutePath = bindingdir.getAbsolutePath();
        for (int i = 0; i < files.length; i++) {
            String file = addToPath(absolutePath, files[i]);
            bindingSet.add(file);
        }
        return bindingSet;
	}
	
	/**
	 * Returns the name of the configuration whose dependencies will be searched for bindings
	 */
	abstract String getConfigurationNameForBindings()
	
	/**
	 * Add bindings that are containted in artifacts.
	 * @param includeDependentBinding
	 * @param bindingSet
	 * @return
	 */
	public List<String> addDependentBinding(IncludeBaseBinding includeDependentBinding, List<String> bindingSet)
	{
		Set<ResolvedArtifact> artifacts = this.project.configurations.getByName(getConfigurationNameForBindings()).resolvedConfiguration.resolvedArtifacts
        for (ResolvedArtifact artifact : artifacts)
        {
        	if ((!"jar".equals(artifact.getType()))
        			&& (!"bundle".equals(artifact.getType()))
        			&& (!"zip".equals(artifact.getType())))
        		continue;
        	if ((!includeDependentBinding.groupId.equals(artifact.moduleVersion.id.group))
        			|| (!includeDependentBinding.artifactId.equals(artifact.moduleVersion.id.name)))
        		continue;
        	if (includeDependentBinding.classifier != null)
        		if (!includeDependentBinding.classifier.equals(artifact.getClassifier()))
        			continue;
        	bindingSet = addDependentArtifact(artifact, includeDependentBinding, bindingSet);
			
        }
        return bindingSet;
	}
    public List<String> addDependentArtifact(ResolvedArtifact artifact, IncludeBaseBinding includeDependentBinding, List<String> bindingSet)
    {
    	// Found it! Go through this jar and see if these binding files exist
		ZipFile zip = null;
		try {
			zip = new ZipFile(artifact.getFile());
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if ((includeDependentBinding.includes == null) || (includeDependentBinding.includes.size() == 0))
			bindingSet = extractBaseBindingFile(bindingSet, zip, includeDependentBinding.directory, includeDependentBinding.groupId, includeDependentBinding.artifactId, DEFAULT_INCLUDE_BINDINGS);
		else
		{
			for (String include : includeDependentBinding.includes) {
				bindingSet = extractBaseBindingFile(bindingSet, zip, includeDependentBinding.directory, includeDependentBinding.groupId, includeDependentBinding.artifactId, include);
			}
		}
		return bindingSet;
    }
	/**
	 * Extract the base binding file(s) from the dependent artifacts.
	 * @param pathList
	 * @param zip
	 * @param directory
	 * @param groupId
	 * @param artifactId
	 * @param filePathInJar
	 * @return
	 */
	public List<String> extractBaseBindingFile(List<String> pathList, ZipFile zip, String directory, String groupId, String artifactId, String filePathInJar)
	{
		try {
			String filenameIn = filePathInJar;
			if (directory != null)
				filenameIn = addToPath(directory, filenameIn);
			ZipEntry entry = zip.getEntry(filenameIn);
			filenameIn = filePathInJar;
			int lastSlash = filenameIn.lastIndexOf(File.separator);
			if (lastSlash == -1)
				lastSlash = filenameIn.lastIndexOf('/');
			String startFilePath = "";
			if (lastSlash != -1)
			{
				startFilePath = File.separator + filenameIn.substring(0, lastSlash);
				filenameIn = filenameIn.substring(lastSlash + 1);
			}
			if (entry != null)
			{
				String filenameOut = getFullPath(getBaseBindingDirectory()) + startFilePath;
				new File(filenameOut).mkdirs();
				filenameOut = filenameOut + File.separator + groupId + '-' + artifactId + '-' + filenameIn;
				File fileOut = new File(filenameOut);
				if (fileOut.exists())
					fileOut.delete();	// Hmmmmm - optimize LATER!
				InputStream inStream = zip.getInputStream(entry);
				if (fileOut.createNewFile())
				{
					OutputStream outStream = new FileOutputStream(filenameOut);
					byte[] b = new byte[1000];
					int size = 0;
					while ((size = inStream.read(b)) > 0)
					{
						outStream.write(b, 0, size);
					}
					outStream.close();
					inStream.close();
					pathList.add(filenameOut);
				}
			}
		
	} catch (IOException e) {
		e.printStackTrace();
	}
	return pathList;
}
    /**
     * Determine if the plugin is running in "multi-module" mode.
     */
    boolean isMultiModuleMode() {
        return this.multimodule;
    }

    /**
     * Determine if the plugin is running in "restricted multi-module" mode.
     */
    boolean isRestrictedMultiModuleMode() {
        return isMultiModuleMode() && (this.modules != null);
    }

    /**
     * Determine if the plugin is running in "single-module" mode.
     */
    boolean isSingleModuleMode() {
        return !isMultiModuleMode();
    }
    
    /**
     * Get the base binding files directory.
     * @return The binding files directory.
     */
    protected String getBaseBindingDirectory()
    {
    	if (baseBindingDirectory != null)
    		if (baseBindingDirectory.length() > 0)
    			return baseBindingDirectory;
    	return getSchemaBindingDirectory();
    }
    
    /**
     * Get the binding files directory.
     * @return The binding files directory.
     */
    protected String getSchemaBindingDirectory()
    {
    	if (schemaBindingDirectory != null)
    		return schemaBindingDirectory;
    	return getDefaultSchemaBindingDirectory();
    }
	
	protected void setSchemaBindingDirectory(String schemaBindingDirectory) {
		this.schemaBindingDirectory = schemaBindingDirectory
	}

    /**
     * Get the binding files directory.
     * @return The binding files directory.
     */
    protected abstract String getDefaultSchemaBindingDirectory();

    /**
     * Fix this directory path so it starts at the root project dir.
     * @param dir
     * @return
     */
    public String getFullPath(String dir)
    {
    	try {
			URL url = new URL(dir);
			if (!"file".equalsIgnoreCase(url.getProtocol()))
				return dir;	// If you pass a valid non file URL, use it!
		} catch (MalformedURLException e) {
		}
		File file = new File(dir);
		if (!file.isAbsolute())
    		dir = addToPath(getProjectBasedir(project), dir);
    	return dir;
    }
    /**
     * Fix this directory path so it starts at the root project dir.
     * @param dir
     * @return
     */
    public String addToPath(String prePath, String postPath)
    {
    	if ((prePath.length() > 0) && (!prePath.endsWith(File.separator))
    		&& (postPath.length() > 0) && (!postPath.startsWith(File.separator)))
    		return prePath + File.separator + postPath;
    	else
    		return prePath + postPath;
    }
}
