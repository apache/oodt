//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.config;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Met Keys used when reading a {@link PgeConfig} XML file
 * </p>.
 */
public interface PgeConfigMetKeys {

    public static final String IMPORT_TAG = "import";

    public static final String NAMESPACE_ATTR = "namespace";

    public static final String FILE_ATTR = "file";

    public static final String CUSTOM_METADATA_TAG = "customMetadata";

    public static final String METADATA_TAG = "metadata";

    public static final String KEYREF_ATTR = "key-ref";
    
    public static final String KEY_GEN_ATTR = "key-gen";
    
    public static final String KEY_ATTR = "key";

    public static final String VAL_ATTR = "val";

    public static final String ENV_REPLACE_ATTR = "envReplace";

    public static final String ENV_REPLACE_NO_RECUR_ATTR = "envReplace-NoRecur";
    
    public static final String SPLIT_ATTR = "split";

    public static final String WORKFLOW_MET_ATTR = "workflowMet";
    
    public static final String DYN_INPUT_FILES_TAG = "dynInputFiles";

    public static final String FILE_TAG = "file";

    public static final String PATH_ATTR = "path";

    public static final String WRITER_CLASS_ATTR = "writerClass";

    public static final String ARGS_ATTR = "args";

    public static final String EXE_TAG = "exe";

    public static final String DIR_ATTR = "dir";

    public static final String SHELL_TYPE_ATTR = "shellType";

    public static final String CMD_TAG = "cmd";

    public static final String OUTPUT_TAG = "output";

    public static final String DIR_TAG = "dir";

    public static final String CREATE_BEFORE_EXEC_ATTR = "createBeforeExe";

    public static final String FILES_TAG = "files";

    public static final String REGEX_ATTR = "regExp";

    public static final String NAME_ATTR = "name";

    public static final String MET_FILE_WRITER_CLASS_ATTR = "metFileWriterClass";

    public static final String RENAMING_CONV_TAG = "renamingConv";

    public static final String NAMING_EXPR_ATTR = "namingExpr";

}
