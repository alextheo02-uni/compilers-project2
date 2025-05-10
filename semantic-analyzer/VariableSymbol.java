public class VariableSymbol {
    private String identifier;
    private String type;
    private int order;

    // Constructor
    public VariableSymbol(String identifier, String type) {

        this.identifier = identifier;
        this.type = type;
        this.order = 0;
    }

    // Getters
    public String getIdentifier(){
        return this.identifier;
    }

    public String getType(){
        return this.type;
    }

    public int getOrder(){
        return this.order;
    }

    // Setters
    public void setOrder(int o){
        this.order = o;
    }
}
