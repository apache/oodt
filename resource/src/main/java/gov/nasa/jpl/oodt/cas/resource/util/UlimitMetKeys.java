//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata keys to describe the output
 * of the ulimit call.
 * </p>.
 */
public interface UlimitMetKeys {

    public static final String CORE_FILE_SIZE = "core file size";

    public static final String DATA_SEGMENT_SIZE = "data seg size";
    
    public static final String FILE_SIZE = "file size";

    public static final String MAX_LOCKED_MEMORY = "max locked memory";

    public static final String MAX_MEMORY_SIZE = "max memory size";

    public static final String MAX_OPEN_FILES = "open files";

    public static final String MAX_PIPE_SIZE = "pipe size";

    public static final String MAX_STACK_SIZE = "stack size";

    public static final String MAX_CPU_TIME = "cpu time";

    public static final String MAX_USER_PROCESSES = "max user processes";

    public static final String MAX_VIRTUAL_MEMORY = "virtual memory";

}
