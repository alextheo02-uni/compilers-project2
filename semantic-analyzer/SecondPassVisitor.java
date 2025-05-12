import java.util.Arrays;
import java.util.HashSet;

import syntaxtree.*;
import visitor.*;

/* 
    Responsible to traverse the AST perform type checking.
*/

class SecondPassVisitor extends GJDepthFirst<String, Void>{

    // Fields
    private SymbolTable ST;
    private Context context;
    private HashSet<String> validTypes;


    // Constructor
    public SecondPassVisitor(SymbolTable ST, Context context){
        this.ST = ST;
        this.context = context;

        // Populate valid types with all user-defined types + minijava types
        this.validTypes = new HashSet<String>(Arrays.asList("int", "boolean", "int[]", "boolean[]"));
        ST.getClasses().forEach((key, value) -> {
            if (!value.isMain()) { this.validTypes.add(key); }
        });
    }

    // Check if specific type is valid, or user-defined, else, throw undefined type error
    private void typeValidityCheck(String type) throws Exception {
        if (!this.validTypes.contains(type)){
            throw new Exception("Undefined type: " + type + ".");
        }
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

        ClassSymbol cs = ST.getClassSymbol(classname);
        this.context.currentClass = cs;
        
        MethodSymbol ms = cs.getMethod("main");
        this.context.currentMethod = ms;
        
        // Check for conflicting type on declaration identifier
        n.f11.accept(this, null);
        
        n.f14.accept(this, null);
        
        // Accept statements
        n.f15.accept(this, null);
        
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

        ClassSymbol cs = ST.getClassSymbol(classname);
        this.context.currentClass = cs;
        
        // Var declarations
        n.f3.accept(this, null);

        // Method declarations
        n.f4.accept(this, null);

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

        n.f2.accept(this, null);
        String parentClassName = n.f3.accept(this, null);

        ClassSymbol cs = ST.getClassSymbol(classname);
        this.context.currentClass = cs;

        // Var declarations
        n.f5.accept(this, null);

        // Method declarations
        n.f6.accept(this, null);

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

        // Type
        String type = n.f0.accept(this, null);
        typeValidityCheck(type);

        // Identifier
        n.f1.accept(this, null);
        
        return null;
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

        // Type
        String myType = n.f1.accept(this, null);
        typeValidityCheck(myType);

        // Method declaration identifier
        String myName = n.f2.accept(this, null);

        if (this.context.currentClass == null) { throw new Exception("Method declaration " + myType + " " + myName + "() outside of class"); }

        if (this.context.currentMethod != null) { throw new Exception("Method declaration " + myType + " " + myName + "() inside method"); }

        // Add method to context
        MethodSymbol ms = this.context.currentClass.getMethod(myName);
        this.context.currentMethod = ms;

        // Formal parameter list
        n.f4.accept(this, null);
        
        // Var declarations
        n.f7.accept(this, null);

        // Statements
        n.f8.accept(this, null);

        // Return expression (must match return type)
        n.f10.accept(this, null);

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

        // Some extra error checking, unlikely to happen, parsing should take care of this
        if (this.context.currentMethod == null) { throw new Exception("Formal parameter outside of method declaration"); }

        String type = n.f0.accept(this, null);
        typeValidityCheck(type);

        String name = n.f1.accept(this, null);

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
    public String visit(Identifier n, Void argu) throws Exception {
        String id = n.f0.toString();

        // if is declaration && not extends,
        // if (ST.getClasses().containsKey(id)){
        //     throw new Exception("Identifier " + id + "belongs to a class name.");
        // }

        return id;
    }
}