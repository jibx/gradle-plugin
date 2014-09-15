package org.jibx.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class JibxPlugin implements Plugin<Project> {
	
	static String BIND_TASK_NAME = 'bind'
	
	static String SCHEMA_CODEGEN_TASK_NAME = 'schemaCodegen'
	
	static String JIBX_2_WSDL_TASK_NAME = 'jibx2Wsdl'
	
	static String TEST_BIND_TASK_NAME = 'testBind'
	
	static String TEST_SCHEMA_CODEGEN_TASK_NAME = 'testSchemaCodegen'
	
	static String TEST_JIBX_2_WSDL_TASK_NAME = 'testJibx2Wsdl'
	
	static String DOCUMENT_COMPARE_TASK_NAME = 'documentCompare'
	
    void apply(Project project) {
		// bind
		project.tasks.create(BIND_TASK_NAME, CompileBindingTask)
		project.tasks.getByName(BIND_TASK_NAME).dependsOn JavaPlugin.COMPILE_JAVA_TASK_NAME
		project.tasks.getByName(JavaPlugin.CLASSES_TASK_NAME).mustRunAfter BIND_TASK_NAME
		// schemaCodegen
		project.tasks.create(SCHEMA_CODEGEN_TASK_NAME, SchemaCodeGenTask)
		project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).mustRunAfter SCHEMA_CODEGEN_TASK_NAME
		// jibx2Wsdl
		project.tasks.create(JIBX_2_WSDL_TASK_NAME, Jibx2WsdlTask)
		project.tasks.getByName(JIBX_2_WSDL_TASK_NAME).dependsOn JavaPlugin.CLASSES_TASK_NAME
		// testBind
		project.tasks.create(TEST_BIND_TASK_NAME, TestCompileBindingTask)
		project.tasks.getByName(TEST_BIND_TASK_NAME).dependsOn JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME
		project.tasks.getByName(JavaPlugin.TEST_CLASSES_TASK_NAME).mustRunAfter TEST_BIND_TASK_NAME
		// testSchemaCodegen
		project.tasks.create(TEST_SCHEMA_CODEGEN_TASK_NAME, TestSchemaCodeGenTask)
		project.tasks.getByName(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME).mustRunAfter TEST_SCHEMA_CODEGEN_TASK_NAME
		// testJibx2Wsdl
		project.tasks.create(TEST_JIBX_2_WSDL_TASK_NAME, TestJibx2WsdlTask)
		project.tasks.getByName(TEST_JIBX_2_WSDL_TASK_NAME).dependsOn JavaPlugin.TEST_CLASSES_TASK_NAME
		// documentCompare
		project.tasks.create(DOCUMENT_COMPARE_TASK_NAME, DocumentCompareTask)
    }
	
}
