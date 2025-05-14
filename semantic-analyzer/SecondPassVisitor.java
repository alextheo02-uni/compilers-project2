import java.util.ArrayList;
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
    private boolean messageSendFlag;


    // Constructor
    public SecondPassVisitor(SymbolTable ST, Context context){
        this.ST = ST;
        this.context = context;

        // Populate valid types with all user-defined types + minijava types
        this.validTypes = new HashSet<String>(Arrays.asList("int", "boolean", "int[]", "boolean[]"));
        ST.getClasses().forEach((key, value) -> {
            if (!value.isMain()) { this.validTypes.add(key); }
        });
        this.messageSendFlag = false;
    }

    private void derivationCheck(String A, String B) throws Exception {
        
        // Same type
        if (A.equals(B)){
            return;
        }
        
        if (!this.ST.getClasses().containsKey(B)){
            throw new Exception("Invalid assignment of type " + B + " to " + A);
        }
        
        ClassSymbol cs = this.ST.getClassSymbol(B);
        String parentClassName = "";
        while (!(parentClassName = cs.getParentClassName()).equals("")){
            if (parentClassName.equals(A)){
                return;
            }
            cs = cs.getParentClassSymbol();
        }
        throw new Exception("Invalid assignment of type " + B + " to " + A);
    }

    private void methodArgCheck(String paramList, String argList) throws Exception {

        // Split both strings to lists
        ArrayList<String> paramTypesList = new ArrayList<String>(Arrays.asList(paramList.split(" ")));
        ArrayList<String> argTypesList = new ArrayList<String>(Arrays.asList(argList.split(" ")));

        // Check lengths
        if (paramTypesList.size() == argTypesList.size()){
            for (int i=0; i<paramTypesList.size(); i++){
                derivationCheck(paramTypesList.get(i), argTypesList.get(i));
            }
        }
        else{
            throw new Exception("Function call arguments do not match expected parameter types. Expected: " + paramList + " but got: " + argList);
        }
    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        String identifier = n.f0.accept(this, null);
        
        // Get type of identifier based on context
        String identifierType = ST.getIdentifierType(identifier, context);

        
        // Accept expression, which will return resolved type, if expression type != identifier.type -> error
        String expressionType = n.f2.accept(this, null);

        // Check if expression type extends identifier type
        derivationCheck(identifierType, expressionType);

        return null;
    }

    /**
     * Grammar production:
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        String identifier = n.f0.accept(this, null);

        String identifierType = ST.getIdentifierType(identifier, context);
        
        // Not int[] or boolean[]
        if (!identifierType.equals("int[]") && !identifierType.equals("boolean[]")){
            throw new Exception("Identifier " + identifier + " is not of type int[] or boolean[]");
        } 

        String inBracketExprType = n.f2.accept(this, null);

        String rightExprType = n.f5.accept(this, null);

        if (!inBracketExprType.equals("int")) {
            throw new Exception("Array index must be of type int, not " + inBracketExprType);
        }

        if (!identifierType.substring(0, identifierType.length()-2).equals(rightExprType)){
            throw new Exception("Invalid assignment of type " + rightExprType + " to " + identifierType);
        }

        return null;
    }
    
    /**
     * Grammar production:
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public String visit(IfStatement n, Void argu) throws Exception {

        String exprType = n.f2.accept(this, null);
        if (!exprType.equals("boolean")){
            throw new Exception("If expression must evaluate to boolean, not " + exprType);
        }

        // Accept rest of the statements
        n.f4.accept(this, null);
        n.f6.accept(this, null);

        return null;
    }
    
    /**
     * Grammar production:
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    @Override
    public String visit(PrintStatement n, Void argu) throws Exception {
        // Print statements can only print integers

        String exprType = n.f2.accept(this, null);
        if (!exprType.equals("int")){
            throw new Exception("System.out.println() expected value of type int, not " + exprType);
        }

        return null;
    }
    
    /**
     * Grammar production:
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    @Override
    public String visit(Expression n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }
    
    /**
     * Grammar production:
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    @Override
    public String visit(AndExpression n, Void argu) throws Exception {
        String leftType = n.f0.accept(this, null);
        String rightType = n.f2.accept(this, null);

        if (!leftType.equals("boolean") || !rightType.equals("boolean")){
            throw new Exception("Logical AND expects boolean values on both sides, got: " + leftType + " and " + rightType);
        }

        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n, Void argu) throws Exception {
        String leftType = n.f0.accept(this, null);
        String rightType = n.f2.accept(this, null);

        if (!leftType.equals("int") || !rightType.equals("int")){
            throw new Exception("Less than comparison expects both values to be of type int, instead got: " + leftType + ", " + rightType);
        }

        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, Void argu) throws Exception {
        String leftType = n.f0.accept(this, null);
        String rightType = n.f2.accept(this, null);

        if (!leftType.equals("int") || !rightType.equals("int")){
            throw new Exception("Addition expects both values to be of type int, instead got: " + leftType + ", " + rightType);
        }
        
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n, Void argu) throws Exception {
        String leftType = n.f0.accept(this, null);
        String rightType = n.f2.accept(this, null);

        if (!leftType.equals("int") || !rightType.equals("int")){
            throw new Exception("Subtraction expects both values to be of type int, instead got: " + leftType + ", " + rightType);
        }
        
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n, Void argu) throws Exception {
        String leftType = n.f0.accept(this, null);
        String rightType = n.f2.accept(this, null);

        if (!leftType.equals("int") || !rightType.equals("int")){
            throw new Exception("Multiplication expects both values to be of type int, instead got: " + leftType + ", " + rightType);
        }

        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String pr1Type = n.f0.accept(this, null);
        if (!pr1Type.equals("int[]") && !pr1Type.equals("boolean[]")){
            throw new Exception("Can not use lookup operation on non int[] or boolean[] type");
        }
        String arrayAccessType = n.f2.accept(this, null);

        if (!arrayAccessType.equals("int")){
            throw new Exception("Array index must be of type int, not " + arrayAccessType);
        }

        return pr1Type.substring(0, pr1Type.length()-2);
    }


    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, Void argu) throws Exception {
        String primaryExprType = n.f0.accept(this, null);
        if (!primaryExprType.equals("int[]") && !primaryExprType.equals("boolean[]")){
            throw new Exception("Can not access property \".length\" on object of type " + primaryExprType);
        }

        return "int";
    }

    /**
     * Grammar production:
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, Void argu) throws Exception {
        this.messageSendFlag = true;
        String classname = n.f0.accept(this, null);
        this.messageSendFlag = false;
        
        String methodIdentifier = n.f2.accept(this, null);
        
        String expressionListTypes = n.f4.present() ? n.f4.accept(this, null) : "";

        ClassSymbol cs = null;
        if ((cs = ST.getClassSymbol(classname)) != null){
            MethodSymbol ms = null;
            if ((ms = cs.getMethod(methodIdentifier)) != null){
                // Method found, check expression list with parameter list
                String parameterListTypes = ms.getStrParameterTypes();

                if (!parameterListTypes.equals(expressionListTypes)) {
                    // Check each argument individually, for derived classes
                    methodArgCheck(parameterListTypes, expressionListTypes);
                }

                return ms.getReturnType();
            }
            throw new Exception("Object of type " + classname + " does not have method: " + methodIdentifier);
        }
        throw new Exception("Classname " + classname + " not found");

    }

    /**
     * Grammar production:
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    @Override
    public String visit(Clause n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    /**
     * Grammar production:
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null) + " ";

        if (n.f1 != null) {
            String r = n.f1.accept(this, null);
            if (!r.isEmpty()){
                ret += r;
            }
        }

        return ret;
    }

    /**
     * Grammar production:
     * f0 -> ( ExpressionTerm() )*
     */
    @Override
    public String visit(ExpressionTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += node.accept(this, null) + " ";
        }

        return ret;
    }

    /**
     * Grammar production:
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }

    /**
     * Grammar production:
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    @Override
    public String visit(PrimaryExpression n, Void argu) throws Exception {

        if (n.f0.choice.getClass().getName().toString().equals("syntaxtree.Identifier")){

            String identifier = n.f0.accept(this, null);
            String identifierType = this.ST.getIdentifierType(identifier, context);

            // Only perform the following checks if coming from messageSend
            if (this.messageSendFlag){

                if (this.ST.getClasses().containsKey(identifier)){
                    throw new Exception("Can not use access operator (.) on not instantiated object.");
                }
                
                
                if (!this.ST.getClasses().containsKey(identifierType)){
                    throw new Exception("Can not use access operator (.) on non basic minijava types except for int[] and boolean[] with .length");
                }
            }
            return identifierType;
        }
        
        String res = n.f0.accept(this, null);
        return res;

    }

    /**
     * Grammar production:
     * f0 -> "this"
     */
    @Override public String visit(ThisExpression n, Void argu) throws Exception {
        if (this.context.currentClass == null) {
            throw new Exception("\"this\" operator used outside of class scope.");
        }

        // Return classname
        return this.context.currentClass.getClassName();
    }


    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
        String exprType = n.f3.accept(this, null);
        if (!exprType.equals("int")){
            throw new Exception("Array size must be of type int, not " + exprType);
        }
        return "boolean[]";
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(IntegerArrayAllocationExpression n, Void argu) throws Exception {
        String exprType = n.f3.accept(this, null);
        if (!exprType.equals("int")){
            throw new Exception("Array size must be of type int, not " + exprType);
        }
        return "int[]";
    }

    /**
     * Grammar production:
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, Void argu) throws Exception {
        String identifier = n.f1.accept(this, null);
        return ST.getIdentifierType(identifier, context);
    }

    /**
     * Grammar production:
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n, Void argu) throws Exception {
        return n.f1.accept(this, null);
    }



    /**
     * Grammar production:
     * f0 -> "!"
     * f1 -> Clause()
     */
    @Override
    public String visit(NotExpression n, Void argu) throws Exception {
        String clauseType = n.f1.accept(this, null);
        if (!clauseType.equals("boolean")) {
            throw new Exception("Not (!) operator expected value of type boolean, got " + clauseType);
        }
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> <INTEGER_LITERAL>
     */
    @Override
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return "int";
    }

    /**
     * Grammar production:
     * f0 -> "true"
     */
    @Override
    public String visit(TrueLiteral n, Void argu) throws Exception {
        return "boolean";
    }

    /**
     * Grammar production:
     * f0 -> "false"
     */
    @Override
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return "boolean";
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
        n.f3.accept(this, null);

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
        n.f0.accept(this, null);

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
        String returnType = n.f10.accept(this, null);

        if (!returnType.equals(ms.getReturnType())){
            throw new Exception("Method " + ms.getIdentifier() + " in class " + this.context.currentClass.getClassName() + " must return " + ms.getReturnType() + ", not " + returnType);
        }

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

        String name = n.f1.accept(this, null);

        return type + " " + name;
    }

    /**
     * Grammar production:
     * f0 -> BooleanArrayType()
     *       | IntegerArrayType()
     */
    @Override
    public String visit(ArrayType n, Void argu) throws Exception {
        return n.f0.accept(this, null);
    }

    // Added boolean array type support
    @Override
    public String visit(BooleanArrayType n, Void argu) {
        return "boolean[]";
    }

    @Override
    public String visit(IntegerArrayType n, Void argu) {
        return "int[]";
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