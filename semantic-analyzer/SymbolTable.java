import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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

    public String getIdentifierType(String identifier, Context context){
        // Inside class
        if (context.currentClass != null){

            // Classname
            for (ClassSymbol cs : this.classes.values()){
                if (cs.getClassName().equals(identifier)){
                    return identifier;
                }
            }

            // Inside method
            if (context.currentMethod != null){
                VariableSymbol vs = null;

                // Parameter
                if ((vs = context.currentMethod.getParameter(identifier)) != null){
                    return vs.getType(); 
                }
                // Local Variable
                if ((vs = context.currentMethod.getLocalVariable(identifier)) != null){
                    return vs.getType(); 
                }

                // Derived class field
                ClassSymbol cs = context.currentClass;
                while (cs.getParentClassSymbol() != null){
                    ClassSymbol parent_cs = cs.getParentClassSymbol();
                    FieldSymbol fs = null;
                    if ((fs = parent_cs.getField(identifier)) != null){
                        return fs.getType();
                    }
                    cs = parent_cs;
                }

            }
            // Outside method

            // Field
            FieldSymbol fs = null;
            if ((fs = context.currentClass.getField(identifier)) != null){
                return fs.getType();
            }
            
            // Method
            MethodSymbol ms = null;
            if ((ms = context.currentClass.getMethod(identifier)) != null){
                return ms.getReturnType();
            }
        }
        // System.out.println("GET ID TYPE " + identifier); // TreeVisitor-error
        return "undefined";
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

    public int getOffsetValue(String type){
        // boolean
        if (type.equals("boolean"))
            return 1;

        // integer
        if (type.equals("int"))
            return 4;
        
        // pointer
        return 8;
    }

    // Pair to hold both offsets in one object
    public class ClassOffsets {
        int fieldOffset;
        int methodOffset;

        public ClassOffsets(int fieldOffset, int methodOfsset){
            this.fieldOffset = fieldOffset;
            this.methodOffset = methodOfsset;
        }
    }

    // Print offsets in a formatted way
    public void printOffsets(){
        System.out.println("Printing offsets");
        LinkedHashMap<String, ClassOffsets> classOffsetMap = new LinkedHashMap<>();

        // For each class
        for (Map.Entry<String, ClassSymbol> classEntry : this.classes.entrySet()){
            // Get classname and classSybmol
            String classname = classEntry.getKey();
            ClassSymbol cs = classEntry.getValue();

            
            // Check for inherited fields and methods
            int fieldOffset = cs.getParentClassSymbol() != null ? classOffsetMap.get(cs.getParentClassSymbol().getClassName()).fieldOffset : 0;
            int methodOffset = cs.getParentClassSymbol() != null ? classOffsetMap.get(cs.getParentClassSymbol().getClassName()).methodOffset : 0;
            
            
            for (Map.Entry<String, FieldSymbol> fieldEntry : cs.getFields().entrySet()){
                String fieldIdentifier = fieldEntry.getKey();
                FieldSymbol fs = fieldEntry.getValue();
                
                int offsetValue = getOffsetValue(fs.getType());
                System.out.println(cs.getClassName() + "." + fieldIdentifier + " : " + fieldOffset);
                fieldOffset += offsetValue;
            }
            
            for (Map.Entry<String, MethodSymbol> methodEntry : cs.getMethods().entrySet()){
                String methodIdentifier = methodEntry.getKey();
                
                // Skip overriden methods
                if (cs.getInheritedMethods().containsKey(methodIdentifier))
                    continue;
                
                // Skip main function
                if (methodIdentifier == "main")
                continue;
                
                int offsetValue = 8;
                System.out.println(cs.getClassName() + "." + methodIdentifier + " : " + methodOffset);
                methodOffset += offsetValue;
            }
            
            // Create ClassOffsets entry
            classOffsetMap.put(classname, new ClassOffsets(fieldOffset, methodOffset));
            System.out.println();

        }
    }
}
