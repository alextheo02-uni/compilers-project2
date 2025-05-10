// Argument type for visit functions
public class Context {
    public ClassSymbol currentClass;
    public MethodSymbol currentMethod;

    public Context(ClassSymbol cs, MethodSymbol ms){
        this.currentClass = cs;
        this.currentMethod = ms;
    }
}