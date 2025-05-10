import java.util.LinkedHashMap;

public class SymbolTable {
    private LinkedHashMap<String, ClassSymbol> classes;

    // Constructor
    public SymbolTable(){
        this.classes = new LinkedHashMap<String, ClassSymbol>();
    }

    // Getters
    public LinkedHashMap<String, ClassSymbol> getClasses(){
        return this.classes;
    }

    // Insert entry into classes
    public void insertClassSymbol(ClassSymbol cs) throws Exception{
        // System.out.println("Trying to insert class " + cs.getClassName() + " to ST");
        String className = cs.getClassName();

        if (this.classes.containsKey(className)){
            throw new Exception("Classname " + className + " already exists.");
        }

        // Insert entry into linked hashmap classes
        this.classes.put(className, cs);
    }
}
