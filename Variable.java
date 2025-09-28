public class Variable extends Expression {
    public static void main(String[] args) {
    }
    
    private String symbol;
    public Variable()
    {
        
    }
    
    public Variable(String symbol)
    {
        this.symbol = symbol;
    }
    public Variable(Expression exponent, String symbol) 
    {
        super(exponent);
        this.symbol = symbol;
    }
    
    public String getSymbol()
    {
        return symbol;
    }
    
    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }
}