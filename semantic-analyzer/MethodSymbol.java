import java.util.LinkedHashMap;

public class MethodSymbol {
    private String identifier;
    private String returnType;
    private LinkedHashMap<String, VariableSymbol> localVariables;
    private int variableCounter;

    // Constructor
    public MethodSymbol(String identifier, String returnType){
        this.identifier = identifier;
        this.returnType = returnType;
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

    // Insertions
    public void insertLocalVariable(VariableSymbol vs) throws Exception{

        String identifier = vs.getIdentifier();

        if (this.localVariables.containsKey(identifier)){
            throw new Exception("Local variable " + identifier + " already exists.");
        }

        vs.setOrder(++this.variableCounter);

        // Insert entry into linked hashmap classes
        this.localVariables.put(identifier, vs);
    }

}
