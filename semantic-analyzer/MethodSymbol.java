import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

public class MethodSymbol {
    private String identifier;
    private String returnType;
    private String parentClassname;
    private LinkedHashMap<String, VariableSymbol> parameters;
    private LinkedHashMap<String, VariableSymbol> localVariables;
    private int variableCounter;
    private int order;

    // Constructor
    public MethodSymbol(String identifier, String returnType, String parentClassname){
        this.identifier = identifier;
        this.returnType = returnType;
        this.parentClassname = parentClassname;
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

    public VariableSymbol getLocalVariable(String identifier){
        return this.localVariables.get(identifier);
    }

    public VariableSymbol getParameter(String identifier){
        return this.parameters.get(identifier);
    }

    // Returns parameters serialized as a string
    public String getStrParameterTypes(){
        PriorityQueue<VariableSymbol> params = new PriorityQueue<>(
            Comparator.comparingInt(VariableSymbol::getOrder)
        );

        for (String identifier : this.parameters.keySet()){
            VariableSymbol vs = this.parameters.get(identifier);
            params.add(vs);
        }

        String res = "";
        for (VariableSymbol vs : params){
            res += vs.getType() + " ";
        }

        return res;
    }

    public String getParentClassName(){
        return this.parentClassname;
    }

    public int getOrder(){
        return this.order;
    }

    // Setters
    public void setOrder(int o){
        this.order = o;
    }

    // Insertions
    public void insertLocalVariable(VariableSymbol vs) throws Exception{

        String identifier = vs.getIdentifier();

        // Check both local variables and parameters for duplicate identifier
        if (this.localVariables.containsKey(identifier) || this.parameters.containsKey(identifier)){
            throw new Exception("In class " + this.parentClassname + ", in method " + this.identifier + ", local variable " + identifier + " already exists.");
        }

        vs.setOrder(++this.variableCounter);

        // Insert entry into linked hashmap
        this.localVariables.put(identifier, vs);
    }

    public void insertParameter(VariableSymbol vs) throws Exception{
        String identifier = vs.getIdentifier();

        if (this.parameters.containsKey(identifier)){
            throw new Exception("In class " + this.parentClassname + ", in method " + this.identifier + ", parameter " + identifier + " already exists.");
        }

        vs.setOrder(++this.variableCounter);

        // Insert entry into linked hashmap
        this.parameters.put(identifier, vs);
    }


    public void display(){

        // Classname and parent classname
        System.out.println("\t" + this.order + " Method " + this.returnType + " " + this.identifier + " inside class " + this.parentClassname);
        
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
