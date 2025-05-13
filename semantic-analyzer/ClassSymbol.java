import java.util.LinkedHashMap;

public class ClassSymbol {
    private String className;
    private String parentClassName;
    private ClassSymbol parentClassSymbol;
    private LinkedHashMap<String, FieldSymbol> inheritedFields;     // identifier = field
    private LinkedHashMap<String, FieldSymbol> fields;              // identifier -> field
    private LinkedHashMap<String, MethodSymbol> methods;            // identifier -> method
    private LinkedHashMap<String, MethodSymbol> inheritedMethods;   // identifier -> method
    private boolean isMain; // flag for signifying if class is main
    private int methodCounter;
    private int order;

    // Constructor
    public ClassSymbol(String className, String parentClassName, ClassSymbol parentClassSymbol, boolean isMain, int order){
        this.className = className;
        this.parentClassName = parentClassName;
        this.parentClassSymbol = parentClassSymbol;

        this.fields = new LinkedHashMap<>();
        this.inheritedFields = new LinkedHashMap<>();

        this.methods = new LinkedHashMap<>();
        this.inheritedMethods = new LinkedHashMap<>();

        this.isMain = isMain;
        this.methodCounter = 0;
        this.order = order;
    }

    // Getters
    public String getClassName(){
        return this.className;
    }

    public String getParentClassName(){
        return this.parentClassName;
    }

    public ClassSymbol getParentClassSymbol(){
        return this.parentClassSymbol;
    }

    public LinkedHashMap<String, FieldSymbol> getFields(){
        return this.fields;
    }

    public FieldSymbol getField(String identifier){
        return this.fields.get(identifier);
    }

    
    public LinkedHashMap<String, FieldSymbol> getInheritedFields(){
        return this.inheritedFields;
    }

    public LinkedHashMap<String, MethodSymbol> getMethods(){
        return this.methods;
    }

    public LinkedHashMap<String, MethodSymbol> getInheritedMethods(){
        return this.inheritedMethods;
    }

    public MethodSymbol getMethod(String identifier){
        if (this.methods.containsKey(identifier)){
            return this.methods.get(identifier);
        }
        else if (this.inheritedMethods.containsKey(identifier)) {
            return this.inheritedMethods.get(identifier);
        }
        else if (this.parentClassSymbol != null) {
            MethodSymbol ms = parentClassSymbol.getMethod(identifier);
            return ms;
        }
        return null;
    }

    public boolean isMain(){
        return this.isMain;
    }

    // Insert methods

    public void insertField(FieldSymbol fs) throws Exception {
        String identifier = fs.getIdentifier();

        if (this.fields.containsKey(identifier)){
            throw new Exception("In class" + this.className + ", field " + identifier + " already exists.");
        }

        this.fields.put(identifier,fs);
    }

    public void insertMethod(MethodSymbol ms) throws Exception {
        String identifier = ms.getIdentifier();

        if (this.methods.containsKey(identifier)){
            throw new Exception("In class" + this.className + ", method " + identifier + " already exists.");
        }

        ms.setOrder(++methodCounter);
        this.methods.put(identifier,ms);
    }

    // Inheritance method
    public void inherit(ClassSymbol parent){

        // Inherit parent's fields
        LinkedHashMap<String, FieldSymbol> parentFields = parent.getFields();
        if (!parentFields.isEmpty()){
            parentFields.forEach((identifier, fieldSymbol) -> {
                this.inheritedFields.put(fieldSymbol.getIdentifier(), fieldSymbol);
            });
        }

        // Inherit parent's methods
        LinkedHashMap<String, MethodSymbol> parentMethods = parent.getMethods();
        if (!parentMethods.isEmpty()){
            parentMethods.forEach((identifier, methodSymbol) -> {
                this.inheritedMethods.put(methodSymbol.getIdentifier(), methodSymbol);
            });
        }
    }

    public void display(){
        
        System.out.println("\n------------------------");
        
        // Classname and parent classname
        System.out.println(this.order + " " + (isMain?"Main ":"") + "class " + this.className + (this.parentClassName != "" ? " extends " + this.parentClassName : ""));
        
        // Inherited Fields
        System.out.println("Inherited fields:");
        this.inheritedFields.forEach((key, value) -> {
            value.display();
        });
        System.out.println();
        
        // Fields
        System.out.println("Fields:");
        this.fields.forEach((key, value) -> {
            value.display();
        });
        System.out.println();
        
        // Inherited Methods
        System.out.println("Inherited methods:");
        this.inheritedMethods.forEach((key, value) -> {
            value.display();
        });
        System.out.println();
        
        // Methods
        System.out.println("Methods:");
        this.methods.forEach((key, value) -> {
            value.display();
        });
        
        System.out.println("------------------------\n");
    }
}
