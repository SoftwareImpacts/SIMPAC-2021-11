/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.graphab.mpi;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.common.distribute.mpi.MainMPI;
import org.thema.common.distribute.mpi.OpenMPIInterface;
import org.thema.graphab.CLITools;
import org.thema.graphab.MainFrame;
import org.thema.graphab.Project;

/**
 *
 * @author gvuidel
 */
public class MpiLauncher extends MainMPI {

    public MpiLauncher(String[] args) {
        super(new OpenMPIInterface(), args);
    }
    
    @Override
    public void master() {
        try {
            new CLITools().execute(args);
        } catch (Throwable ex) {
            Logger.getLogger(MpiLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initWorker(String [] args) throws Exception {
        if(args.length < 2 || !args[0].equals("--project"))
            throw new IllegalArgumentException();
        
        MainFrame.project = Project.loadProject(new File(args[1]), false);
    }
    
}
