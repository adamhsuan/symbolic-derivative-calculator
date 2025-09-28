public class Num extends Expression {
    public static void main(String[] args) {
    }
    
    private int value;
    public Num()
    {
    }
    public Num(int value)
    {
        this.value = value;
    }
    public Num(Expression exponent, int value)
    {
        super(exponent);
        this.value = value;
    }
    
    public int getValue()
    {
        return value;
    }
    
    public void setValue(int value)
    {
        this.value = value;
    }
}