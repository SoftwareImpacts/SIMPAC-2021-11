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

package org.thema.graphab;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math.MathException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.Envelope2D;
import org.thema.common.collection.HashMapList;
import org.thema.data.IOFeature;
import org.thema.data.IOImage;
import org.thema.data.feature.DefaultFeature;
import org.thema.graphab.mpi.MpiLauncher;
import org.thema.parallel.AbstractParallelTask;

/**
 * Parallel task for creating multiple projects from land modifications and executing CLI commands for each.
 * This task works in theaded and MPI mode.
 * 
 * @author Gilles Vuidel
 */
public class LandModTask extends AbstractParallelTask<Void, Void> implements Serializable {

    private File fileZone;
    private String idField;
    private String codeField;
    private boolean voronoi;
    private Set<String> ids;
    private List<String> args;
    
    private transient Project project;
    private transient HashMapList<String, DefaultFeature> zones;
    private transient List<String> zoneIds;
    private transient Envelope2D landEnv;

    /**
     * Creates a new LandmodTask
     * @param project the initial project 
     * @param fileZone the shapefile containing polygons of land modifications
     * @param idField the shapefile field containing identifier
     * @param codeField the shapefile field containing the new land code
     * @param voronoi is voronoi (ie. planar topology) must be calculated for new projects ?
     * @param args the CLI commands to execute after creating each project
     */
    public LandModTask(Project project, File fileZone, String idField, String codeField, boolean voronoi, Set<String> ids, List<String> args) {
        this.project = project;
        this.fileZone = fileZone;
        this.idField = idField;
        this.codeField = codeField;
        this.voronoi = voronoi;
        this.ids = ids;
        this.args = args;
    }

    @Override
    public void init() {
        // useful for MPI only, because project is not serializable
        if(project == null) {
            project = MpiLauncher.getProject();
        }
        try {
            List<DefaultFeature> features = IOFeature.loadFeatures(fileZone);
            if(!features.get(0).getAttributeNames().contains(idField)) {
                throw new IllegalArgumentException("Unknow field : " + idField);
            }
            if(!features.get(0).getAttributeNames().contains(codeField)) {
                throw new IllegalArgumentException("Unknow field : " + codeField);
            }
            zones = new HashMapList<>();
            for(DefaultFeature f : features) {
                String id = f.getAttribute(idField).toString();
                if(ids == null || ids.contains(id)) {
                    zones.putValue(id, f);
                }
            }
            zoneIds = new ArrayList(zones.keySet());
            // sort to ensure the same order for several JVM in MPI mode
            Collections.sort(zoneIds);
            
            super.init();
            landEnv = IOImage.loadTiffWithoutCRS(new File(project.getDirectory(), "source.tif")).getEnvelope2D();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public Void execute(int start, int end) {
        for(String id : zoneIds.subList(start, end)) {
            try {
                File prjFile = createProject(id, zones.get(id)).getProjectFile();
                
                // execute next commands
                ArrayList<String> newArgs = new ArrayList<>(args);
                newArgs.add(0, "--project");
                newArgs.add(1, prjFile.getAbsolutePath());
                new CLITools().execute(newArgs.toArray(new String[0]));
            } catch (IOException | SchemaException | MathException  ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    @Override
    public int getSplitRange() {
        return zones.size();
    }

    @Override
    public void gather(Void results) {
    }

    /**
     * @return never
     * @throws UnsupportedOperationException
     */
    @Override
    public Void getResult() {
        throw new UnsupportedOperationException(); 
    }

    /**
     * Creates a new Project based on the initial project while changing the landmap on areas covering the features zones
     * @param id the identifier for the new project
     * @param zones the zones to change in the land map
     * @return the new created project
     * @throws IOException
     * @throws SchemaException 
     */
    private Project createProject(String id, List<DefaultFeature> zones) throws IOException, SchemaException {
        WritableRaster src = project.getImageSource();
        WritableRaster raster0 = project.getImageSource().createCompatibleWritableRaster();
        WritableRaster raster = raster0.createWritableTranslatedChild(src.getMinX(), src.getMinY());
        raster.setRect(src);
        TreeSet<Integer> codes = new TreeSet<>(project.getCodes());
        
        // update land map
        for(DefaultFeature zone : zones) {
            int code = ((Number)zone.getAttribute(codeField)).intValue();
            Geometry trGeom = project.getSpace2grid().transform(zone.getGeometry());
            for(int i = 0; i < trGeom.getNumGeometries(); i++) {
                Geometry transGeom = trGeom.getGeometryN(i);
                Envelope env = transGeom.getEnvelopeInternal();
                int miny = Math.max((int)env.getMinY(), raster.getMinY());
                int minx = Math.max((int)env.getMinX(), raster.getMinX());
                int maxy = Math.min((int)Math.ceil(env.getMaxY()), raster.getMinY() + raster.getHeight());
                int maxx = Math.min((int)Math.ceil(env.getMaxX()), raster.getMinX() + raster.getWidth());
                Coordinate c = new Coordinate();
                GeometryFactory geomFact = new GeometryFactory();
                for(c.y = miny+0.5; c.y < maxy; c.y++) {
                    for(c.x = minx+0.5; c.x < maxx; c.x++) {
                        if(transGeom.intersects(geomFact.createPoint(c))) {
                            raster.setSample((int)c.x, (int)c.y, 0, code);
                        }
                    }
                }
            }
            codes.add(code);
        }
        
        GridCoverage2D newLand = new GridCoverageFactory().create("", raster0, landEnv);

        // create project
        File dir = new File(project.getDirectory(), id);       
        return new Project(project.getName() + "-" + id, dir, newLand, codes, 
                project.getPatchCodes(), project.getNoData(), project.isCon8(), project.getMinArea(), project.getMaxSize(), project.isSimplify(), voronoi);
    }
}
