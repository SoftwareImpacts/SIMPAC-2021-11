/*
 * Copyright (C) 2014 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.graphab.mpi;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.MathException;
import org.geotools.feature.SchemaException;
import org.thema.graphab.CLITools;
import org.thema.graphab.Project;
import org.thema.parallel.mpi.MainMPI;
import org.thema.parallel.mpi.OpenMPIInterface;

/**
 * Start point for MPI execution.
 * 
 * @author Gilles Vuidel
 */
public class MpiLauncher extends MainMPI {

    private static Project project;
    
    /**
     * Creates a new MpiLauncher passing command line argument
     * @param args command line argument from public static void main method
     */
    public MpiLauncher(String[] args) {
        super(new OpenMPIInterface(), args);
    }
    
    @Override
    public void master() {
        try {
            new CLITools().execute(args);
        } catch (IOException | SchemaException | MathException ex) {
            Logger.getLogger(MpiLauncher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initWorker(String [] args) throws IOException  {
        if(args.length < 2 || !args[0].equals("--project")) {
            throw new IllegalArgumentException();
        }
        
        project = Project.loadProject(new File(args[1]), false);
    }

    /**
     * 
     * @return the project loaded by this worker or null if this jvm process is the master
     */
    public static Project getProject() {
        return project;
    }
    
    /**
     * @return true is this jvm process is a worker, false if it is the master process
     */
    public static boolean IsMPIWorker() {
        return project != null;
    }
    
}
