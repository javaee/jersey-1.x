/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.utils;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to invoke apt.
 */
public class AptInvoker {
    
    /** -classpath option */
    protected String compileClasspath = null;
    
    public String getClasspath() { 
        return compileClasspath; 
    }
    public void setClasspath(String classpath) {
        compileClasspath = classpath;
    }

    
    /** -d option: directory to output processor and javac generated class files */
    private File destDir = null;
    public File getDestdir() { 
        return destDir; 
    }
    public void setDestdir(File base) { 
        this.destDir = base; 
    }
    
    /** -s option: directory to place processor generated source files */
    private File sourceDestDir;
    public void setSourcedestdir(File sourceBase) { 
        this.sourceDestDir = sourceBase; 
    }
    public File getSourcedestdir() { 
        return sourceDestDir; 
    }
    
    
    /** -A option */
    protected List<Option> options = new ArrayList<Option>();
    public List<Option> getOptions() { 
        return options; 
    }
    public Option createOption() {
            Option option = new Option();
            options.add(option);
            return option;
    }

    /** -J<flag> option: Pass <flag> directly to the runtime */
    protected List<Jvmarg> jvmargs = new ArrayList<Jvmarg>();
    public List<Jvmarg> getJvmargs() { 
        return jvmargs; 
    }
    public Jvmarg createJvmarg() {
        Jvmarg jvmarg = new Jvmarg();
        jvmargs.add(jvmarg);
        return jvmarg;
    }
	
    /** -nocompile option */
    private boolean noCompile = false;
    public boolean isNocompile() { 
        return noCompile; 
    }
    public void setNocompile(boolean noCompile) { 
        this.noCompile = noCompile; 
    }

    /******************** -print option **********************/
    private boolean print = false;
    public boolean isPrint() { 
        return print; 
    }
    public void setPrint(boolean print) { 
        this.print = print; 
    }

    /******************** -factorypath option **********************/
    private File factoryPath = null;
    public File getFactorypath() { 
        return factoryPath; 
    }
    public void setFactorypath(File factoryPath) { 
        this.factoryPath = factoryPath; 
    }

    /******************** -factory option **********************/
    private String factory = null;
    public String getFactory() { 
        return factory; 
    }
    public void setFactory(String factory) { 
        this.factory = factory; 
    }
	
	/******************** -XListAnnotationTypes option **********************/
    private boolean xListAnnotationTypes = false;
    public boolean isXlistannotationtypes() { 
        return xListAnnotationTypes; 
    }
    public void setXlistannotationtypes(boolean xListAnnotationTypes) { 
        this.xListAnnotationTypes = xListAnnotationTypes; 
    }

	/******************** -XListDeclarations option **********************/
    private boolean xListDeclarations = false;
    public boolean isXlistdeclarations() { 
        return xListDeclarations; 
    }
    public void setXlistdeclarations(boolean xListDeclarations) { 
        this.xListDeclarations = xListDeclarations; 
    }

	/******************** -XPrintAptRounds option **********************/
    private boolean xPrintAptRounds = false;
    public boolean isXprintaptrounds() { 
        return xPrintAptRounds; 
    }
    public void setXprintaptrounds(boolean xPrintAptRounds) { 
        this.xPrintAptRounds = xPrintAptRounds; 
    }

    /******************** -XPrintFactoryInfo option **********************/
    private boolean xPrintFactoryInfo = false;
    public boolean isXprintfactoryinfo() { 
        return xPrintFactoryInfo; 
    }
    public void setXprintfactoryinfo(boolean xPrintFactoryInfo) { 
        this.xPrintFactoryInfo = xPrintFactoryInfo; 
    }

	/******************** -XclassesAsDecls option **********************/
    private boolean xClassesAsDecls = false;
    public boolean isXclassesasdecls() { 
        return xClassesAsDecls; 
    }
    public void setXclassesasdecls(boolean xClassesAsDecls) { 
        this.xClassesAsDecls = xClassesAsDecls; 
    }
	
    /** Inherited from javac */

    /** -g option: debugging info */
    protected boolean debug = false;
    public boolean isDebug() { 
        return debug; 
    }
    public void setDebug(boolean debug) { 
        this.debug = debug; 
    }
	
    /** debug level */
    protected String debugLevel = null;
    public String getDebuglevel() { 
        return debugLevel; 
    }
    public void setDebuglevel(String debugLevel) { 
        this.debugLevel = debugLevel; 
    }
	
    /** -nowarn option: generate no warnings */
    protected boolean nowarn = false;
    public boolean isNowarn() { 
        return nowarn; 
    }
    public void setNowarn(boolean nowarn) { 
        this.nowarn = nowarn; 
    }

    /** -deprecation option: output source locations where deprecated APIs are used */
    protected boolean deprecation = false;
    public boolean isDeprecation() { 
        return deprecation; 
    }
    public void setDeprecation(boolean deprecation) { 
        this.deprecation = deprecation; 
    }

    /** -bootclasspath option: override location of bootstrap class files */
    protected String bootclassPath = null;
    public String getBootclasspath() { 
        return bootclassPath; 
    }
    public void setBootclasspath(String bootclassPath) { 
        this.bootclassPath = bootclassPath; 
    }

    /** -extdirs option: override location of installed extensions */
    protected String extdirs = null;
    public String getExtdirs() { 
        return extdirs; 
    }
    public void setExtdirs(String extdirs) { 
        this.extdirs = extdirs; 
    }

    /** -endorseddirs option: override location of endorsed standards path */
    protected String endorseddirs = null;
    public String getEndorseddirs() { 
        return endorseddirs; 
    }
    public void setEndorseddirs(String endorseddirs) { 
        this.endorseddirs = endorseddirs; 
    }

    /** -verbose option: output messages about what the compiler is doing */
    protected boolean verbose = false;
    public boolean isVerbose() { 
        return verbose; 
    }
    public void setVerbose(boolean verbose) { 
        this.verbose = verbose; 
    }
	
    /** -sourcepath option: Specify where to find input source files */
    protected String sourcePath = null;
    public String getSourcepath() { 
        return sourcePath; 
    }
    public void setSourcepath(String sourcePath) { 
        this.sourcePath = sourcePath; 
    }

    /** -encoding option: character encoding used by the source files */
    protected String encoding = null;
    public String getEncoding() { 
        return encoding; 
    }
    public void setEncoding(String encoding) { 
        this.encoding = encoding; 
    }

    /** -target option: generate class files for specific VM version */
    protected String targetVM = null;
    public String getTarget() { 
        return targetVM; 
    }
    public void setTarget(String target) { 
        this.targetVM = target; 
    }



	/** Others */
	
    /** -fork option: */
    protected boolean fork = false;
    public boolean isFork() { 
        return fork; 
    }
    public void setFork(boolean fork) { 
        this.fork = fork; 
    }
	
    protected Set<String> sourceFiles = new TreeSet<String>();
    public void setSourceFiles(Set<String> fileset) {
        sourceFiles = fileset;
    }
	
    
    private List<String> setupAptCommand() throws BuildException {
        List<String> cmd = setupAptArgs();
        
        // classpath option (cp option just uses classpath option)
        String classpath = getClasspath();
        
        if (classpath != null && !classpath.toString().equals("")) {
            cmd.add("-classpath");
            cmd.add(classpath);
        }
        return cmd;
    }

    
    private List<String> setupAptArgs() throws BuildException {
        List<String> cmd = new ArrayList<String>();
        
        if (null != getDestdir() && !getDestdir().getName().equals("")) {
            cmd.add("-d");
            cmd.add(getDestdir().getAbsolutePath());
        }
        
        if (null != getSourcedestdir() && !getSourcedestdir().getName().equals("")) {
            cmd.add("-s");
            cmd.add(getSourcedestdir().getAbsolutePath());
        }
		
        if (getSourcepath() == null)
            throw new BuildException("\"sourcePath\" attribute must be set.");
        
        if (getSourcepath() != null && !getSourcepath().toString().equals("")) {
            cmd.add("-sourcepath");
            cmd.add(getSourcepath().toString());
        }
        
        if (getBootclasspath() != null && !getBootclasspath().toString().equals("")) {
            cmd.add("-bootclasspath");
            cmd.add(getBootclasspath().toString());
        }
        
        if (getExtdirs() != null && !getExtdirs().equals("")) {
            cmd.add("-extdirs");
            cmd.add(getExtdirs());
        }
        
        if (getEndorseddirs() != null && !getEndorseddirs().equals("")) {
            cmd.add("-endorseddirs");
            cmd.add(getEndorseddirs());
        }
        
        if (isDebug()) {
            String debugOption = "";
            debugOption = "-g";
            if (getDebuglevel() != null && !getDebuglevel().equals(""))
                debugOption += ":" + getDebuglevel();
            cmd.add(debugOption);
        } else
            cmd.add("-g:none");
        
	if (isVerbose())
            cmd.add("-verbose");
		
        if (getEncoding() != null && !getEncoding().equals("")) {
            cmd.add("-encoding");
            cmd.add(getEncoding());
        }
        
        if (getTarget() != null && !getTarget().equals("")) {
            cmd.add("-target");
            cmd.add(getTarget());
        }
        
        for (Jvmarg jvmarg : jvmargs) {
            cmd.add("-J" + jvmarg.getValue());
        }

        for (Option option : options) {
            cmd.add("-A" + option.getKey() + "=" + option.getValue());
        }
        
        if(isNowarn()){
            cmd.add("-nowarn");
        }

        if(isNocompile()){
            cmd.add("-nocompile");
        }
		
        if(isDeprecation()){
            cmd.add("-deprecation");
        }

        if(isPrint()){
            cmd.add("-print");
        }

        if(getFactorypath() != null){
            cmd.add("-factorypath");
            cmd.add(getFactorypath().toString());
        }
		
        if(getFactory() != null){
            cmd.add("-factory");
            cmd.add(getFactory());
        }
		
        if (isXlistannotationtypes()) {
            cmd.add("-XListAnnotationTypes");
        }
        
        if (isXlistdeclarations()) {
            cmd.add("-XListDeclarations");
        }
        
        if (isXprintaptrounds()) {
            cmd.add("-XPrintAptRounds");
        }
        
        if (isXprintfactoryinfo()) {
            cmd.add("-XPrintFactoryInfo");
        }
		
        if (isXprintfactoryinfo()) {
            cmd.add("-XclassesAsDecls");
        }
		
		
        if(!sourceFiles.isEmpty()){
            for(String source : sourceFiles){
                cmd.add(source);                
            }
        }
        
        return cmd;
    }

    
    /** Called by the project to let the task do it's work **/
    public void execute() throws BuildException {

        PrintWriter writer = null;
        boolean ok = false;
        try {
            List<String> cmd = setupAptCommand();
			
            if (verbose) {
                System.out.println("command line: " + "apt " + cmd.toString());
            }
	    int status = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer = new PrintWriter(baos);

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            try {
                com.sun.tools.apt.Main aptTool = new com.sun.tools.apt.Main();
                String[] array = new String[cmd.size()];
                status = aptTool.process(writer, cmd.toArray(array));
                writer.flush();
                if (verbose || baos.size()!=0)
                    System.out.print(baos.toString());
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
            ok = (status == 0) ? true : false;
            if (!ok) {
                if (!verbose) {
                    System.out.println("Command invoked: "+"apt "+cmd.toString());
                }
                throw new BuildException("apt failed");
            }
        } catch (Exception ex) {
            if (ex instanceof BuildException) {
                throw (BuildException)ex;
            } else {
                throw new BuildException("Error starting apt: ", ex);
            }
        } 
    }
    

    
    public static class Option {
        protected String key;
        protected String value;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class Jvmarg {
        protected String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
