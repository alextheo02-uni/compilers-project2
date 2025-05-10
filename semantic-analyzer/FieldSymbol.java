public class FieldSymbol {
    private String identifier;
    private String type;

    // Constructor
    public FieldSymbol(String identifier, String type) {

        this.identifier = identifier;
        this.type = type;
    }

    // Getters
    public String getIdentifier(){
        return this.identifier;
    }

    public String getType(){
        return this.type;
    }

}
