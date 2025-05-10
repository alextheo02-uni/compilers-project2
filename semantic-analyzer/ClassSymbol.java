import java.util.LinkedHashMap;

public class ClassSymbol {
    private String className;
    private String parentClassName;
    private LinkedHashMap<String, FieldSymbol> fields;   // identifier -> field
    private LinkedHashMap<String, MethodSymbol> methods; // identifier -> method
    private boolean isMain; // flag for signifying if class is main

    // Constructor
    public ClassSymbol(String className, String parentClassName, boolean isMain){
        this.className = className;
        this.parentClassName = parentClassName;
        this.fields = new LinkedHashMap<>();
        this.methods = new LinkedHashMap<>();
        this.isMain = isMain;
    }

    // Getters
    public String getClassName(){
        return this.className;
    }

    public String getParentClassName(){
        return this.parentClassName;
    }

    public LinkedHashMap<String, FieldSymbol> getFields(){
        return this.fields;
    }

    public LinkedHashMap<String, MethodSymbol> getMethods(){
        return this.methods;
    }

    public boolean isMain(){
        return this.isMain;
    }

    // Insert methods

    public void insertField(FieldSymbol fs) throws Exception{
        String identifier = fs.getIdentifier();

        if (this.fields.containsKey(identifier)){
            throw new Exception("Field " + identifier + " already exists.");
        }

        this.fields.put(identifier,fs);
    }

    public void insertMethod(MethodSymbol ms) throws Exception {
        String identifier = ms.getIdentifier();

        if (this.fields.containsKey(identifier)){
            throw new Exception("Method " + identifier + " already exists.");
        }

        this.methods.put(identifier,ms);
    }

    public void display(){
        
        System.out.println("\n------------------------");
        
        // Classname and parent classname
        System.out.println((isMain?"Main ":"") + "class " + this.className + (this.parentClassName != "" ? " extends " + this.parentClassName : ""));
        
        // Fields
        this.fields.forEach((key, value) -> {
            value.display();
        });
        
        // Methods
        this.methods.forEach((key, value) -> {
            value.display();
        });
        
        System.out.println("------------------------\n");
    }
}
