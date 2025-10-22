package rdiger36.StudioBridge.functions;

public class Models {

    /**
     * Converts model names to a specific format for sending to the printer.
     *
     * @param model The model name as selected by the user.
     * @return The formatted model name.
     */
    public static String getModelFromName(String model) {
        switch (model) {
            case "A1": model = "N2S"; break;
            case "A1 Mini": model = "N1"; break;
            case "P1P": model = "C11"; break;
            case "P1S": model = "C12"; break;
            case "P2S": model = "N7"; break;
            case "X1": model = "3DPrinter-X1"; break;
            case "X1C": model = "3DPrinter-X1-Carbon"; break;
            case "X1E": model = "C13"; break;
            case "H2D": model = "O1D"; break;
            case "H2D Pro": model = "O1E"; break;
            case "H2S": model = "O1S"; break;
            default: break;
        }
        return model;
    }
	
    
    /**
     * Maps model codes to model names.
     * 
     * @param model The model code as a string.
     * @return The corresponding model name.
     */
    public static String getNameFromModel(String model) {
        switch (model) {
	        case "N2S": model = "A1"; break;
	        case "N1": model = "A1 Mini"; break;
	        case "C11": model = "P1P"; break;
	        case "C12": model = "P1S"; break;
	        case "N7": model = "P2S"; break;
	        case "3DPrinter-X1": model = "X1"; break;
	        case "3DPrinter-X1-Carbon": model = "X1C"; break;
	        case "C13": model = "X1E"; break;
	        case "O1D": model = "H2D"; break;
	        case "O1E": model = "H2D Pro"; break;
	        case "O1S": model = "H2S"; break;
            default: break;
        }
        return model;
    }
}
