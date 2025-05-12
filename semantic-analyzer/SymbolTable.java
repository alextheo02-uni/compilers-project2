import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class SymbolTable {
    private LinkedHashMap<String, ClassSymbol> classes;
    private ArrayList<String> validTypes;

    // Constructor
    public SymbolTable(){
        this.classes = new LinkedHashMap<String, ClassSymbol>();
        this.validTypes = null;
    }

    // Getters
    public LinkedHashMap<String, ClassSymbol> getClasses(){
        return this.classes;
    }

    public ClassSymbol getClassSymbol(String classname){
        if (this.classes.containsKey(classname)){
            return this.classes.get(classname);
        }
        return null;
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

    // Check if specific type is valid
    private void typeValidityCheck(String type) throws Exception {
        if (!this.validTypes.contains(type)){
            throw new Exception("Undefined type: " + type + ".");
        }
    }

    public void declarationCheck() throws Exception {

        // Populate valid types with all user-defined types + minijava types
        this.validTypes = new ArrayList<String>(
            Arrays.asList("int", "boolean", "int[]", "boolean[]")
        );
        this.classes.forEach((key, value) -> {
            if (!value.isMain()) { validTypes.add(key); }
        });

        // For all classes
        for (String classname : this.classes.keySet()){
            ClassSymbol cs = this.classes.get(classname);

            // Field namecheck
            for (String field_identifier : cs.getFields().keySet()){
                if (this.classes.containsKey(field_identifier)) {
                    throw new Exception("Field identifier " + field_identifier + " conflicts with another class name.");
                }
                // Check field's type
                typeValidityCheck(cs.getField(field_identifier).getType());
            }

            // Method namecheck
            for (String method_identifier : cs.getMethods().keySet()){
                if (this.classes.containsKey(method_identifier)) {
                    throw new Exception("Method identifier " + method_identifier + " conflicts with another class name.");
                }
                MethodSymbol ms = cs.getMethod(method_identifier);
                
                // Check method's return type (skip main, because type is void)
                if (!ms.getIdentifier().equals("main")){ typeValidityCheck(ms.getReturnType()); }
                
                // Local variable namecheck
                for (String var_identifier : ms.getLocalVariables().keySet()){
                    if (this.classes.containsKey(var_identifier)) {
                        throw new Exception("Local variable " + var_identifier + " in method " + ms.getIdentifier() + " in class " + cs.getClassName() + " conflicts with another class name.");
                    }
                    // Check local variable's type
                    typeValidityCheck(ms.getLocalVariable(var_identifier).getType());
                }
                
                // Parameter namecheck
                for (String parameter_identifier : ms.getParameters().keySet()){
                    if (this.classes.containsKey(parameter_identifier)) {
                        throw new Exception("Parameter " + parameter_identifier + " in method " + method_identifier + " in class " + cs.getClassName() + " conflicts with another class name.");
                    }
                    // Check parameter's type (skip main, it only has parameters string[] identifier)
                    if (!ms.getIdentifier().equals("main")){ typeValidityCheck(ms.getParameter(parameter_identifier).getType()); }
                }

                // If method_identifier also exists in parent class, check that parameters and return types match
                if (cs.getInheritedMethods() != null && cs.getInheritedMethods().containsKey(method_identifier)) {
                    MethodSymbol ims = cs.getInheritedMethods().get(method_identifier);

                    // Return type check
                    if (!ims.getReturnType().equals(ms.getReturnType())){
                        throw new Exception("Type mismatch between " + classname + "." + method_identifier + " and " + cs.getParentClassName() + "." + method_identifier);
                    }

                    // Parameters check
                    if (!ims.getStrParameterTypes().equals(ms.getStrParameterTypes())){
                        throw new Exception("Type mismatch between " + classname + "." + method_identifier + " and " + cs.getParentClassName() + "." + method_identifier);
                    }

                }

            }   // Methods

        }   // Classes

    }

    public void display(){
        this.classes.forEach((key, value) -> {
            value.display();
        });
    }
}
