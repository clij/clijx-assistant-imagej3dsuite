package net.haesleinhuepf.clijx.imagej3dsuite;


import ij.ImagePlus;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageLabeller;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.utilities.HasClassifiedInputOutput;
import net.haesleinhuepf.clij2.utilities.IsCategorized;
import org.scijava.plugin.Plugin;

@Plugin(type = CLIJMacroPlugin.class, name = "CLIJx_imageJ3DSuiteConnectedComponentsLabeling")
public class ImageJ3DSuiteConnectedComponentsLabeling extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation, IsCategorized, HasClassifiedInputOutput
{
    public ImageJ3DSuiteConnectedComponentsLabeling() {
        super();
    }

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination";
    }

    @Override
    public boolean executeCL() {
        boolean result = imageJ3DSuiteConnectedComponentsLabeling(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]));
        return result;
    }

    public static boolean imageJ3DSuiteConnectedComponentsLabeling(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer output) {
        // pull image from GPU in ImageJ1 type
        ImagePlus input = clij2.pullBinary(input1);

        // process it using ImageJ 3D Suite
        ImageHandler ima1 = ImageHandler.wrap(input);
        ImageHandler ima2 = ima1.threshold((float)128, false, false);

        ImageLabeller labels = new ImageLabeller();

        ImageFloat seg = labels.getLabelsFloat(ima2);

        ImagePlus result_imp = seg.getImagePlus();

        // push result back
        ClearCLBuffer result_buffer = clij2.push(result_imp);

        // save it in the right place
        clij2.copy(result_buffer, output);

        // clean up
        result_buffer.close();

        return true;
    }

    @Override
    public String getDescription() {
        return "Apply ImageJ 3D Suite Connected Components Labeling (Segment 3D) to an image in 3D.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "3D";
    }

    @Override
    public String getCategories() {
        return "Labeling";
    }


    public static void main(String[] args) {
        CLIJ2 clij2 = CLIJ2.getInstance();

        ClearCLBuffer input = clij2.pushString("" +
                "0 0 0 1\n" +
                "1 0 0 1\n" +
                "\n" +
                "1 1 0 1\n" +
                "1 1 0 0");

        ClearCLBuffer output = clij2.create(input);

        imageJ3DSuiteConnectedComponentsLabeling(clij2, input, output);

        clij2.print(output);

    }

    @Override
    public String getInputType() {
        return "Binary Image";
    }

    @Override
    public String getOutputType() {
        return "Label Image";
    }
    
    
    @Override
    public ClearCLBuffer createOutputBufferFromSource(ClearCLBuffer input) {
        return clij.create(input.getDimensions(), NativeTypeEnum.Float);
    }
}
