
package org.thema.graphab.util;

import java.awt.Color;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import javax.imageio.stream.FileImageInputStream;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;

/**
 * RST format raster reader.
 * RST format is used mainly by IDRISI software.
 * 
 * @author Gilles Vuidel
 */
public class RSTGridReader extends AbstractGridCoverage2DReader  {

    //----- file extensions:
    private static final String extRST =         "rst";
    private static final String extRDC =         "rdc";
    private static final String extSMP =         "smp";
    private static final String extREF =         "ref";

    //----- field names on rdc file:
    private static final String rdcFILE_FORMAT = "file format ";
    private static final String rdcFILE_TITLE =  "file title  ";
    private static final String rdcDATA_TYPE =   "data type   ";
    private static final String rdcFILE_TYPE =   "file type   ";
    private static final String rdcCOLUMNS  =            "columns     ";
    private static final String rdcROWS     =            "rows        ";
    private static final String rdcREF_SYSTEM =  "ref. system ";
    private static final String rdcREF_UNITS  =  "ref. units  ";
    private static final String rdcUNIT_DIST =   "unit dist.  ";
    private static final String rdcMIN_X     =           "min. X      ";
    private static final String rdcMAX_X     =           "max. X      ";
    private static final String rdcMIN_Y     =           "min. Y      ";
    private static final String rdcMAX_Y     =           "max. Y      ";
    private static final String rdcPOSN_ERROR =  "pos'n error ";
    private static final String rdcRESOLUTION =  "resolution  ";
    private static final String rdcMIN_VALUE  =  "min. value  ";
    private static final String rdcMAX_VALUE  =  "max. value  ";
    private static final String rdcDISPLAY_MIN =  "display min ";
    private static final String rdcDISPLAY_MAX = "display max ";
    private static final String rdcVALUE_UNITS = "value units ";
    private static final String rdcVALUE_ERROR = "value error ";
    private static final String rdcFLAG_VALUE =  "flag value  ";
    private static final String rdcFLAG_DEFN  =  "flag def'n  ";
    private static final String rdcFLAG_DEFN2 =  "flag def`n  ";
    private static final String rdcLEGEND_CATS = "legend cats ";
    private static final String rdcLINEAGES   =          "lineage     ";
    private static final String rdcCOMMENTS  =           "comment     ";
    private static final String rdcCODE_N    =           "code %6d ";


    //----- standard values:
    private static final String rstVERSION  =    "Idrisi Raster A.1";
    private static final String rstBYTE    =     "byte";
    private static final String rstINTEGER =     "integer";
    private static final String rstREAL    =     "real";
    private static final String rstRGB24  =      "rgb24";
    private static final String rstDEGREE  =     "degree";
    private static final String rstMETER   =     "meter";
    private static final String rstLATLONG  =    "latlong";
    private static final String rstPLANE   =     "plane";
    private static final String rstUTM     =     "utm-%d%c";
    private static final String rstSPC      =    "spc%2d%2s%d";

    private int nbLine;
    private int nbCol;
    private int dataType;

    private double minValue, maxValue;

    /**
     * Creates a new RST/RDC gridcoverage reader
     * @param file the RST or RDC file
     * @throws IOException 
     */
    public RSTGridReader(File file) throws IOException {
        File rdcFile = file;
        if(file.getName().toLowerCase().endsWith(extRST)) {
            rdcFile = new File(file.getAbsolutePath().replace("."+extRST, "."+extRDC));
        } else {
            file = new File(file.getAbsolutePath().replace("."+extRDC, "."+extRST));
        }

        inStream = new FileImageInputStream(file);
        inStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        HashMap<String, String> params;
        try (BufferedReader reader = new BufferedReader(new FileReader(rdcFile))) {
            params = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                params.put(line.substring(0, 12), line.substring(14, line.length()));
            }
        }

        nbLine = Integer.parseInt(params.get(rdcROWS));
        nbCol = Integer.parseInt(params.get(rdcCOLUMNS));
        if(params.get(rdcDATA_TYPE).equals(rstBYTE)) {
            dataType = DataBuffer.TYPE_BYTE;
        } else if(params.get(rdcDATA_TYPE).equals(rstINTEGER)) {
            dataType = DataBuffer.TYPE_SHORT;
        } else if(params.get(rdcDATA_TYPE).equals(rstREAL)) {
            dataType = DataBuffer.TYPE_FLOAT;
        } else {
            throw new IllegalArgumentException("Type format not supported !");
        }

        double coef = Double.parseDouble(params.get(rdcUNIT_DIST));
        double minX = coef*Double.parseDouble(params.get(rdcMIN_X));
        double maxX = coef*Double.parseDouble(params.get(rdcMAX_X));
        double minY = coef*Double.parseDouble(params.get(rdcMIN_Y));
        double maxY = coef*Double.parseDouble(params.get(rdcMAX_Y));

        originalEnvelope = new GeneralEnvelope(new double[] {minX, minY}, new double[] {maxX, maxY});


        minValue = Double.parseDouble(params.get(rdcDISPLAY_MIN));
        maxValue = Double.parseDouble(params.get(rdcDISPLAY_MAX));

        coverageName = file.getName().substring(0, file.getName().length()-4);
    }


    @Override
    public GridCoverage2D read(GeneralParameterValue[] parameters) throws IllegalArgumentException, IOException {
        final int SIZE = 1024;
        BufferedImage img = null;
        if(dataType == DataBuffer.TYPE_BYTE) {
            byte [] buf = new byte[nbCol*nbLine];
            int off = 0;
            while(off < buf.length) {
                inStream.readFully(buf, off, off+SIZE <= buf.length ? SIZE : buf.length-off);
                off += SIZE;
            }
            
            WritableRaster r = WritableRaster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_BYTE, nbCol, nbLine, 1),
                    new DataBufferByte(buf, buf.length), new Point(0, 0));
            img = new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
                    Transparency.OPAQUE, DataBuffer.TYPE_BYTE), r, false, null);
        }
        if(dataType == DataBuffer.TYPE_SHORT) {
            short [] sbuf = new short[nbCol*nbLine];
            int off = 0;
            while(off < sbuf.length) {
                inStream.readFully(sbuf, off, off+SIZE <= sbuf.length ? SIZE : sbuf.length-off);
                off += SIZE;
            }

            WritableRaster r = WritableRaster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_SHORT, nbCol, nbLine, 1),
                    new DataBufferShort(sbuf, sbuf.length), new Point(0, 0));
            img = new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
                    Transparency.OPAQUE, DataBuffer.TYPE_SHORT),r, false, null);

        }
        if(dataType == DataBuffer.TYPE_FLOAT) {
            float [] buf = new float[nbCol*nbLine];
            int off = 0;
            while(off < buf.length) {
                inStream.readFully(buf, off, off+SIZE <= buf.length ? SIZE : buf.length-off);
                off += SIZE;
            }
            WritableRaster r = WritableRaster.createWritableRaster(new BandedSampleModel(DataBuffer.TYPE_INT, nbCol, nbLine, 1),
                    new DataBufferFloat(buf, buf.length), new Point(0, 0));
            img = new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
                    Transparency.OPAQUE, DataBuffer.TYPE_INT), r, false, null);
        }

        inStream.close();


        GridSampleDimension band = new GridSampleDimension("Temp", getCategories(), null);
        return new GridCoverageFactory().create(coverageName, img,
                        originalEnvelope, new GridSampleDimension[] {band}, null, null);
    }


    protected Category [] getCategories() {
        NumberRange range = null;

        switch(dataType) {
        case DataBuffer.TYPE_BYTE: range = NumberRange.create((short)minValue, (short)maxValue); break;
        case DataBuffer.TYPE_SHORT: range = NumberRange.create((short)minValue, (short)maxValue); break;
        case DataBuffer.TYPE_FLOAT: range = NumberRange.create((int)minValue, (int)maxValue); break;
        }
        
        Category [] tabCat = new Category[1];
        tabCat[0] = new Category("values", new Color[]{Color.BLACK, Color.WHITE}, range, 1, 0);

        return tabCat;
    }

    @Override
    public Format getFormat() {
        return null;
    }

}
