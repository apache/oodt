//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge;

//JDK imports
import java.io.File;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.SerializableMetadata;

/**
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Runs a {@link PGETaskInstance} given {@link Metadata} and a
 * {@link WorkflowTaskConfiguration}
 * </p>.
 */
public class PGETask {

    private Metadata metadata;

    private WorkflowTaskConfiguration wftConfig;

    public PGETask(Metadata metadata, WorkflowTaskConfiguration wftConfig) {
        this.metadata = metadata;
        this.wftConfig = wftConfig;
    }

    public void run(String pgeTaskInstanceClasspath)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, WorkflowTaskInstanceException {
        PGETaskInstance pgeTaskInst = (PGETaskInstance) Class.forName(
                pgeTaskInstanceClasspath).newInstance();
        pgeTaskInst.run(this.metadata, this.wftConfig);
    }

    public static void main(String[] args) throws Exception {
        String metadataFilePath = null, configPropertiesPath = null;
        String pgeTaskInstanceClasspath = null;
        String usage = "PGETask --instanceClass <PGETaskInstance> "
                + "--metadata </path/to/metadata/file> "
                + "--config </path/to/task/config/file>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--metadata")) {
                metadataFilePath = args[++i];
            } else if (args[i].equals("--config")) {
                configPropertiesPath = args[++i];
            } else if (args[i].equals("--instanceClass")) {
                pgeTaskInstanceClasspath = args[++i];
            }
        }

        if (metadataFilePath == null || configPropertiesPath == null) {
            System.err.println(usage);
            System.exit(1);
        }

        SerializableMetadata sm = new SerializableMetadata("UTF-8", false);
        sm.loadMetadataFromXmlStream(new File(metadataFilePath).toURL()
                .openStream());
        WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
        config.getProperties().load(
                new File(configPropertiesPath).toURL().openStream());

        PGETask task = new PGETask(sm, config);
        task.run(pgeTaskInstanceClasspath);
    }

}
