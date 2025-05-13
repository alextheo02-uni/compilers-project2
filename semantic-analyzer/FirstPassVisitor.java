import syntaxtree.*;
import visitor.*;

/* 
    Responsible to traverse the AST and collect information about
    classes, fields, methods and local variables to create the symbol table
*/

class FirstPassVisitor extends GJDepthFirst<String, Void>{

    // Fields
    private SymbolTable ST;
    private Context context;
    private int classOrder;
    
    // Constructor
    public FirstPassVisitor(SymbolTable ST, Context context){
        this.ST = ST;
        this.context = context;
        this.classOrder = 0;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Void argu) throws Exception {

        String classname = n.f1.accept(this, null);
        // System.out.println("Main class: " + classname);

        ClassSymbol cs = new ClassSymbol(classname, "", true, ++classOrder);
        this.context.currentClass = cs;
        
        // Create main method symbol
        MethodSymbol ms = new MethodSymbol("main", "void", this.context.currentClass.getClassName());
        this.context.currentMethod = ms;
        String argsIden = n.f11.accept(this, null);
        // Create variable symbol for String[] var
        VariableSymbol vs = new VariableSymbol(argsIden, "String[]");
        ms.insertParameter(vs);
        cs.insertMethod(ms);
        
        // Local variable declaration for public static void main method
        n.f14.accept(this, null);

        // Insert class symbol into symbol table
        ST.insertClassSymbol(cs);

        // System.out.println();
        
        // Clear currentMethod context
        this.context.currentMethod = null;

        // Clear currentClass context
        this.context.currentClass = null;

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, null);
        
        String classname = n.f1.accept(this, null);
        // System.out.println("Class: " + classname);

        ClassSymbol cs = new ClassSymbol(classname, "", false, ++classOrder);
        this.context.currentClass = cs;
        
        // Insert class symbol into symbol table
        ST.insertClassSymbol(cs);

        // System.out.println("Fields: ");
        n.f3.accept(this, null);
        // System.out.println("Methods: ");
        n.f4.accept(this, null);
        n.f5.accept(this, null);

        // System.out.println();

        // Clear currentClass context
        this.context.currentClass = null;

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, null);

        String classname = n.f1.accept(this, null);
        // System.out.println("Class: " + classname);

        n.f2.accept(this, null);

        String parentClassName = n.f3.accept(this, null);

        // check if parent className exists in symbol table (defined before extends)
        if (!this.ST.getClasses().keySet().contains(parentClassName)){
            throw new Exception("Parent class " + parentClassName + " must be defined before using keyword \"extends\". ");
        }

        ClassSymbol cs = new ClassSymbol(classname, parentClassName, false, ++classOrder);
        this.context.currentClass = cs;

        cs.inherit(ST.getClassSymbol(parentClassName));
        

        // Insert class symbol into symbol table
        ST.insertClassSymbol(cs);

        // System.out.println("Fields: ");
        n.f5.accept(this, null);
        // System.out.println("Methods: ");
        n.f6.accept(this, null);

        // System.out.println();

        // Clear currentClass context
        this.context.currentClass = null;

        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public String visit(VarDeclaration n, Void argu) throws Exception {
        String _ret=null;
        String type = n.f0.accept(this, null);
        String identifier = n.f1.accept(this, null);

        // Check context
        if (this.context.currentClass != null){
            // Method local variable
            if (this.context.currentMethod != null) {
                VariableSymbol vs = new VariableSymbol(identifier, type);
                this.context.currentMethod.insertLocalVariable(vs);
            }
            
            // Class field
            else {
                FieldSymbol fs = new FieldSymbol(identifier, type);
                this.context.currentClass.insertField(fs);
            }
        }
        // Error, stray variable, not inside class or method context (scope)
        else  {
            throw new Exception("Stray variable detected: " + type + " " + identifier);
        }

        // System.out.println(identifier + " " + type);
        
        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        if (this.context.currentClass == null) { throw new Exception("Method declaration " + myType + " " + myName + "() outside of class"); }

        if (this.context.currentMethod != null) { throw new Exception("Method declaration " + myType + " " + myName + "() inside method"); }

        
        MethodSymbol ms = new MethodSymbol(myName, myType, this.context.currentClass.getClassName());
        this.context.currentMethod = ms;

        this.context.currentClass.insertMethod(ms);

        this.context.isParameter = true;
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        this.context.isParameter = false;
        
        
        // System.out.println("Method: " + myType + " " + myName + " (" + argumentList + ")");
        // System.out.println("Local vars:");
        
        n.f7.accept(this, null);

        // Clear currentMethod context
        this.context.currentMethod = null;
        return null;
    }

    // FormalParameterList is always in context of method (only MethodDeclaration BNF grammar rule contains it)
    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);

        // Some extra error checking, unlikely to happen, parsing should take care of this
        if (this.context.currentMethod == null) { throw new Exception("Formal parameter outside of method declaration"); }

        // Insert to method context
        VariableSymbol vs = new VariableSymbol(name, type);
        if (this.context.isParameter){
            this.context.currentMethod.insertParameter(vs);
        } else {
            this.context.currentMethod.insertLocalVariable(vs);
        }

        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }

    // Added boolean array type support
    @Override
    public String visit(BooleanArrayType n, Void argu) {
        return "boolean[]";
    }

    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString();
    }
}