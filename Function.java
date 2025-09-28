import java.util.*;
public class Function extends Expression {
    public static void main(String[] args) {
    }
    
    private String type;
    private Expression argument;
    
    public Function()
    {
        
    }
    public Function(String type)
    {
        this.type = type;
    }
    public Function(String type, Expression argument)
    {
        this.type = type;
        this.argument = argument;
    }
    public Function(Expression exponent, String type)
    {
        super(exponent);
        this.type = type;
        //this.argument = null;
    }
    public Function(Expression exponent, String type, Expression argument)
    {
        super(exponent);
        this.type = type;
        this.argument = argument;
    }

    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    public Expression getArgument()
    {
        return argument;
    }
    
    public void setArgument(Expression argument)
    {
        this.argument = argument;
        argument.setOuterExpression(this.getOuterExpression());
    }
    
}