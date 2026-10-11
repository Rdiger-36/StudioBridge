package rdiger36.StudioBridge.functions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for the mapping between printer model names and the model codes sent to the slicer.
 */
public class ModelsTest {

    private static final String[] MODELS = {
        "A1", "A1 Mini", "A2L", "P1P", "P1S", "P2S", "X1", "X1C", "X1E", "X2D",
        "H2C", "H2C (new revision)", "H2D", "H2D Pro", "H2S"
    };

    @Test
    public void mapsEveryModelBackToItsName() {
        for (String model : MODELS) {
            assertEquals(model, Models.getNameFromModel(Models.getModelFromName(model)));
        }
    }

    @Test
    public void mapsKnownModelsToCodes() {
        assertEquals("3DPrinter-X1-Carbon", Models.getModelFromName("X1C"));
        assertEquals("N2S", Models.getModelFromName("A1"));
        assertEquals("O1D", Models.getModelFromName("H2D"));
    }

    @Test
    public void keepsUnknownValues() {
        assertEquals("Z9", Models.getModelFromName("Z9"));
        assertEquals("Z9", Models.getNameFromModel("Z9"));
    }
}
