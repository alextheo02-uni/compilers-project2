import java.util.LinkedHashMap;

public class MethodSymbol {
    private String identifier;
    private String returnType;
    private LinkedHashMap<String, VariableSymbol> parameters;
    private LinkedHashMap<String, VariableSymbol> localVariables;
    private int variableCounter;

    // Constructor
    public MethodSymbol(String identifier, String returnType){
        this.identifier = identifier;
        this.returnType = returnType;
        this.parameters = new LinkedHashMap<>();
        this.localVariables = new LinkedHashMap<>();
        this.variableCounter = 0;
    }

    // Getters
    public String getIdentifier(){
        return this.identifier;
    }

    public String getReturnType(){
        return this.returnType;
    }

    public LinkedHashMap<String, VariableSymbol> getLocalVariables(){
        return this.localVariables;
    }

    public LinkedHashMap<String, VariableSymbol> getParameters(){
        return this.parameters;
    }

    // Insertions
    public void insertLocalVariable(VariableSymbol vs) throws Exception{

        String identifier = vs.getIdentifier();

        if (this.localVariables.containsKey(identifier)){
            throw new Exception("Local variable " + identifier + " already exists.");
        }

        vs.setOrder(++this.variableCounter);

        // Insert entry into linked hashmap
        this.localVariables.put(identifier, vs);
    }

    public void insertParameter(VariableSymbol vs) throws Exception{
        String identifier = vs.getIdentifier();

        if (this.parameters.containsKey(identifier)){
            throw new Exception("Parameter " + identifier + " already exists.");
        }

        vs.setOrder(++this.variableCounter);

        // Insert entry into linked hashmap
        this.parameters.put(identifier, vs);
    }


    public void display(){

        // Classname and parent classname
        System.out.println("\tMethod " + this.returnType + " " + this.identifier);
        
        // Parameters
        System.out.println("\tParameters:");
        this.parameters.forEach((key, value) -> {
            value.display();
        });
        
        // Local variables
        System.out.println("\tLocal variables:");
        this.localVariables.forEach((key, value) -> {
            value.display();
        });
    }

}
