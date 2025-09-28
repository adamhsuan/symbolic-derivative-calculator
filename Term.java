import java.util.*;
public class Term extends Expression {
    public static void main(String[] args) {
    }
    
    ArrayList<Expression> factors = new ArrayList<Expression>();
    
    public Term()
    {
        
    }
    public Term(ArrayList<Expression> factors, Expression exponent)
    {
        super(exponent);
        this.factors = factors;
    }
    
    public Term(ArrayList<Expression> factors)
    {
        this.factors = factors;
    }
    
    public ArrayList<Expression> getFactors()
    {
        return factors;
    }
    
    @Override
    public void addFactor(Expression factor)
    {
        factors.add(factor);
        factor.setOuterExpression(this);
    }
    
    public void setFactors(ArrayList<Expression> factors)
    {
        this.factors = new ArrayList<Expression>();
        for (Expression factor : factors)
        {
            addFactor(factor);
            factor.setOuterExpression(this);
        }
    }
    
    /*simplifyAbsoluteValues simplifies absolute value expressions in term, removing absolute value if possible*/
    public void simplifyAbsoluteValues()
    {
        for (int i=0; i<factors.size(); i++)
        {
            Expression factor = factors.get(i);
            if (factor.getIsAbsoluteValue() && factor.getTerms().size()==1)
            {
                for (int j=0; j<factor.getFirstTerm().getFactors().size(); j++)
                {
                    Expression innerFactor = factor.getFirstTerm().getFactors().get(j);
                    if (innerFactor instanceof Num)
                    {
                        if (((Num)innerFactor).getValue()<0)
                            ((Num)innerFactor).setValue(((Num)innerFactor).getValue()*-1);
                        factor.getFirstTerm().getFactors().remove(innerFactor);
                        addFactor(innerFactor);
                        j--;
                    }
                    else if (innerFactor.getExponent() != null && innerFactor.getExponent().getFirstFactor() instanceof Num && ((Num)innerFactor.getExponent().getFirstFactor()).getValue() % 2 == 0)
                    {
                        factor.getFirstTerm().getFactors().remove(innerFactor);
                        addFactor(innerFactor);
                        j--;
                    }
                }
            }
        }
    }
    /*expandExpressionsInTerm takes all expressions with one term and expands them (carrying over exponent and dropping parenthesis)*/
    public void expandExpressionsInTerm()
    {

        for (int i=0; i<factors.size(); i++)
        {
            Expression factor = factors.get(i);
            if (factor.isInstanceOfFunction() && ((Function)factor).getType().equals("sqrt"))
            {
                factor = createDeepCopy(((Function)factor).getArgument());
                factors.set(i,factor);
                factor.setExponent(createExpressionWithFactor(new Num(2)));
                factor.getExponent().getFirstFactor().setExponent(createExpressionWithFactor(new Num(-1)));
            }
            if (factor.isInstanceOfExpression() && !factor.getIsAbsoluteValue() && factor.getTerms() !=null)
            {
                if (factor.getTerms().size()==1)
                {
                    for (Expression enclosedFactor : factor.getFirstTerm().getFactors())
                    {
                        boolean initiallyEven = false;
                        if (!factor.getIsAbsoluteValue())
                        {
                            if (factor.getExponent()!=null)
                            {
                                if (enclosedFactor.getExponent()==null)
                                {
                                    enclosedFactor.setExponent(createDeepCopy(factor.getExponent()));
                                    if (enclosedFactor.getTerms().size()==1)
                                    {
                                        Term term = enclosedFactor.getFirstTerm();
                                    }
                                }
                                else
                                {
                                    Expression exponent = new Expression();
                                    if (isNumeric(enclosedFactor.getExponent()))
                                    {
                                        enclosedFactor.getExponent().simplifyExpression();
                                        if(enclosedFactor.getExponent().getTerms().size()==1 && ((Num)((Term)enclosedFactor.getExponent().getTerms().get(0)).getFactors().get(0)).getValue() % 2 == 0)
                                        initiallyEven = true;
                                    }
                                    Term newTerm = new Term();
                                    newTerm.addFactor(enclosedFactor.getExponent());
                                    newTerm.addFactor(factor.getExponent());
                                    exponent.addTerm(newTerm);
                                    enclosedFactor.setExponent(exponent);
                                    exponent.simplifyExpression();
                                }
                    
                            }
                            if (initiallyEven && enclosedFactor.getExponent().hasOneFactor() && enclosedFactor.getExponent().getFirstFactor() instanceof Num && ((Num)enclosedFactor.getExponent().getFirstFactor()).getValue() % 2 == 1)
                            {
                                enclosedFactor = createExpressionWithFactor(enclosedFactor);
                                enclosedFactor.setIsAbsoluteValue(true);
                            }
                            addFactor(enclosedFactor);
                        }
                    }
                    factors.remove(i);
                    i--;
                }
            }
        }
    }
    
    /*factorizeNums takes all nums and reduces them to their most factorized form, expanding if integer exponents as well*/
    public void factorizeNums()
    {
        int numFactors = factors.size();
        for (int i=0; i<numFactors; i++)
        {
            Expression factor = factors.get(i);
            if (factor instanceof Num)
            {
                Num number = (Num) factor;
                ArrayList<Integer> numberFactors = new ArrayList<Integer>();
                int numValue = number.getValue();
                for (int j=2;j<numValue; j++)
                {
                    if ((double)(numValue/j) == ((double)numValue)/j)
                    {
                        numValue /= j;
                        numberFactors.add(j);
                        j--;
                    }
                }
                numberFactors.add(numValue);
            
                for(int numberFactor : numberFactors)
                {
                    if (number.getExponent() == null)
                        addFactor(new Num(numberFactor));
                    else
                        addFactor(new Num(number.getExponent(), numberFactor));
                }
                factors.remove(factor);
                i--;
                numFactors--;
            }
            else if (factor.isInstanceOfFunction())
            {
                Expression argument = ((Function)factor).getArgument();
                argument.simplifyExpression();
                ((Function)factor).setArgument(argument);
                
            }
        }
    }
    /*combineLikeBases combines factors with like bases by adding exponents*/
    public void combineLikeBases()
    {
        for (int i=0; i<factors.size(); i++)
        {
            Expression factor1 = factors.get(i);
            Expression factor1Exponent = factors.get(i).getExponent();
            Expression exponent = new Expression();
            factor1.setExponent(null);
            if (factor1Exponent == null)
                exponent.addFactor(new Num(1));
            else
                exponent.addFactor(factor1Exponent);
            for (int j=i+1; j<factors.size(); j++)
            {
                Expression factor2 = factors.get(j);
                Expression factor2Exponent = factors.get(j).getExponent();
                factor2.setExponent(null);
                if(factor1.equals(factor2))
                {
                    if (factor2Exponent == null)
                        exponent.addFactor(new Num(1));
                    else
                        exponent.addFactor(factor2Exponent);
                    factors.remove(j);
                    j--;
                }
                else
                    factor2.setExponent(factor2Exponent);
                
            }
            if (exponent.getTerms().size()==1 && ((Term)exponent.getTerms().get(0)).getFactors().get(0).equals(new Num(1)) && ((Term)exponent.getTerms().get(0)).getFactors().get(0).getExponent()==null)
            {
                factor1.setExponent(null);
            }
            else
            {
                factor1.setExponent(exponent);
            }
            if (factor1.getExponent()!=null)
            {
                Expression exponent1 = factor1.getExponent();
                exponent1.simplifyExpression();
                factor1.setExponent(exponent1);
            }
        }
        setFactors(removeEmptyExpressions(factors));
    }
    /*simplifyNumsInTerm takes all the Num objects with null exponents and combines them into two Num objects, the numerator and denominator*/
    public void simplifyNumsInTerm()
    {
        for (int i=0; i<factors.size(); i++)
        {
            if (factors.get(i).getExponent()!=null)
            {
                Expression newExpression = createExpressionWithFactor(new Num(0));
                if (factors.get(i).getExponent().equals(newExpression))
                {
                    factors.set(i,new Num(1));
                    i--;
                }
            }
        }
        
        int numeratorProduct = 1;
        int denominatorProduct = 1;
        for (int i=0; i<factors.size(); i++)
        {
            Expression factorExpression = new Expression();
            factorExpression.addTerm(factors.get(i));
            if (isNumeric(factorExpression) && factors.get(i) instanceof Num)
            {
                if (factors.get(i).getExponent()==null)
                {
                    numeratorProduct*=((Num)factors.get(i)).getValue();
                    factors.remove(i);
                    i--;
                }
                else if (factors.get(i).getExponent().getTerms().size()==1)
                {
                    Term exponentTerm = (Term) factors.get(i).getExponent().getTerms().get(0);
                    if (exponentTerm.getFactors().size()==1 && exponentTerm.getFactors().get(0) instanceof Num && exponentTerm.getFactors().get(0).getExponent() == null)
                    {
                        int exponentValue = ((Num)exponentTerm.getFactors().get(0)).getValue();
                        if (exponentValue>=0)
                            numeratorProduct*=Math.pow((((Num)factors.get(i)).getValue()),exponentValue);
                        else
                            denominatorProduct*=Math.pow((((Num)factors.get(i)).getValue()),-1*exponentValue);
                        factors.remove(i);
                        i--;
                        
                    }
                    
                }
            }
        }
        if (numeratorProduct!=1 || factors.size()==0)
            addFactor(new Num(numeratorProduct));
        if (denominatorProduct!=1 || factors.size()==0)
        {
            addFactor(new Num(createExpressionWithFactor(new Num(-1)),denominatorProduct));
        }
        for (int i=0; i<factors.size(); i++)
        {
            Expression factor = factors.get(i);
            if (factor instanceof Num)
            {
                factors.remove(factor);
                factors.add(0,factor);
                
            }
        }
    }
    /*expandRemainingExpressions takes all expressions in the term with null exponents and expands them out (distributive property)*/
    public void expandRemainingExpressions()
    {
        //check whether this is working...
        boolean containsNoNonNumericExpressions = true;
        for (Expression factor : factors)
        {
            if (factor.isInstanceOfExpression() && !factor.getIsAbsoluteValue()) 
            {
                if (Expression.isNumeric(factor)==false)
                    containsNoNonNumericExpressions=false;
                if (factor.getExponent()!=null)
                    return;
            }
        }
        if (containsNoNonNumericExpressions)
            return;
        while (true)
        {
            if (factors.size()==1)
                break;
            Expression factor1 = createDeepCopy(factors.get(0));
            Expression factor2 = createDeepCopy(factors.get(1));
            if (!(factor1.isInstanceOfExpression()&&!factor1.getIsAbsoluteValue()) && (factor2.isInstanceOfExpression()&&!factor2.getIsAbsoluteValue()))
            {
                Expression temp = factor1;
                factor1 = factor2;
                factor2 = temp;
            }
            Expression newExpression = new Expression();
            if (factor1.isInstanceOfExpression()&&!factor1.getIsAbsoluteValue())
            {
                if (factor2.isInstanceOfExpression()&&!factor2.getIsAbsoluteValue())
                {
                    for (Expression term1 : factor1.getTerms())
                    {
                        for (Expression term2 : factor2.getTerms())
                        {
                            Term newTerm = new Term();
                            for (Expression newFactor : ((Term)createDeepCopy(term1)).getFactors())
                                newTerm.addFactor(newFactor);
                            for (Expression newFactor : ((Term)createDeepCopy(term2)).getFactors())
                                newTerm.addFactor(newFactor);
                            newExpression.addTerm(newTerm);
                        }
                    }
                }
                else
                {
                    for (Expression term1 : factor1.getTerms())
                    {
                        Term newTerm = new Term();
                        for (Expression newFactor : ((Term)createDeepCopy(term1)).getFactors())
                            newTerm.addFactor(newFactor);
                        newTerm.addFactor(factor2);
                        newExpression.addTerm(newTerm);
                    }
                }
            }
            else
            {
                Term newTerm = new Term();
                newTerm.addFactor(factor1);
                newTerm.addFactor(factor2);
                newExpression.addTerm(newTerm);
            }
            newExpression.simplifyExpression();
            factors.add(newExpression);
            factors.remove(1);
            factors.remove(0);
        }
    }
    /*combineLikeExponents groups all factors with the same exponent other than null into an expression with that exponent*/
    public void combineLikeExponents()
    {
        for (int i=0; i<factors.size(); i++)
        {
            Expression factor1 = factors.get(i);
            if (factor1.getExponent()!=null && factor1.getExponent()!=createExpressionWithFactor(new Num(1)) && factor1.getExponent()!=createExpressionWithFactor(new Num(-1)))
            {
                Expression exponent1 = createDeepCopy(factor1.getExponent());
                for (int j=i+1; j<factors.size(); j++)
                {
                    if (factors.get(j).getExponent()!=null)
                    {
                        if (exponent1.equals(createDeepCopy(factors.get(j).getExponent())))
                        {
                            factor1 = createExpressionWithFactor(factor1);
                            factor1.addFactor(factors.get(j));
                            factor1.setExponent(exponent1);
                            factors.set(i,factor1);
                            factors.remove(j);
                            j--;
                        }
                    }
                }
            }
        }
        
    }
    /*
    public void combineLikeExponents()
    {
        for (int i=0; i<factors.size(); i++)
        {
            Expression factor1 = factors.get(i);
            if (factor1.getExponent()!=null && factor1.getExponent()!=createExpressionWithFactor(new Num(1)) && factor1.getExponent()!=createExpressionWithFactor(new Num(-1)))
            {
                Expression exponent1 = createDeepCopy(factor1.getExponent());
                for (int j=i+1; j<factors.size(); j++)
                {
                    Expression factor2 = factors.get(j);
                    if (factor2.getExponent()!=null)
                    {
                        if (exponent1.equals(createDeepCopy(factor2.getExponent())))
                        {
                            factor1.setExponent(null);
                            factor2.setExponent(null);
                            Expression newFactor = createExpressionWithFactor(factor1);
                            ((Term)newFactor.getFirstTerm()).addFactor(factor2);
                            newFactor.setExponent(exponent1);
                            newFactor.printExpressionSymbolically();
                            newFactor.printExpression();
                            newFactor.getExponent().printExpression();
                            addFactor(newFactor);
                            factors.remove(i);
                            factors.remove(j);
                            i--;
                            j--;
                            break;
                        }
                    }
                }
            }
        }
        
    }
    */
    
    /*This, in conjunction with simplifyExpression, is used to simplify an expression object tree by recursively simplifying branches*/
    public void simplifyTerm()
    {
        simplifyAbsoluteValues();
        factors = removeEmptyExpressions(factors);
        expandExpressionsInTerm();
        factorizeNums();
        combineLikeBases();
        simplifyNumsInTerm();
        expandRemainingExpressions();
        //combineLikeExponents();
    }
    
    //this is for testing and should be removed after program finalization
    public void printTerm()
    {
        for (Expression factor : factors)
        {
            if (factor.isInstanceOfFunction())
            {
                Function function = (Function) factor;
                System.out.print(function.getType());
                if (function.getArgument() instanceof Num)
                    System.out.print(" function with numerical argument "+((Num)function.getArgument()).getValue());
                else
                    System.out.print(" function with a nonnumerical argument");
                if (function.getExponent()!=null)
                {
                    if (function.getExponent() instanceof Num)
                        System.out.println(" and exponent "+((Num)function.getExponent()).getValue());
                    else 
                        System.out.println(" and a nonnumber exponent");
                }
                else
                    System.out.println(" and exponent null (1)");
                if (function.getArgument()!=null)
                {
                    System.out.println("**argument**");
                    function.getArgument().printExpressionFull();
                    System.out.println("**end of argument**");
                }
            }
            else if (factor instanceof Variable)
            {
                Variable variable = (Variable) factor;
                System.out.print(variable.getSymbol());
                if (variable.getExponent()!=null)
                {
                    if (variable.getExponent() instanceof Num)
                        System.out.println(" variable with exponent "+((Num)variable.getExponent()).getValue());
                    else 
                        System.out.println(" variable with a nonnumber exponent");
                }
                else
                    System.out.println(" variable with exponent null (1)");

            }
            else if (factor instanceof Num)
            {
                Num number = (Num) factor;
                System.out.print(number.getValue());
                if (number.getExponent()!=null)
                {
                    if (number.getExponent() instanceof Num)
                        System.out.println(" number with exponent "+((Num)number.getExponent()).getValue());
                    else 
                        System.out.println(" number with a nonnumber exponent");
                }
                else
                    System.out.println(" number with exponent null (1)");
                    
            }
            else if (factor instanceof Term)
            {
                Term term = (Term) factor;
                if (term.getExponent()!=null)
                {
                    if (term.getExponent() instanceof Num)
                        System.out.print("term with exponent "+((Num)term.getExponent()).getValue());
                    else 
                        System.out.print("term with a nonnumber exponent");
                }
                else
                    System.out.print("term with exponent null (1)");
                if (term.getFactors().size()==0)
                    System.out.println(" and no factors");
                else
                    System.out.println("\nfactors:");
                term.printTerm();
                
            }
            else
            {
                if (factor.getExponent()!=null)
                {
                    if (factor.getExponent() instanceof Num)
                        System.out.println("expression with exponent "+((Num)factor.getExponent()).getValue());
                    else
                        System.out.println("expression with a nonnumber exponent");
                }
                else
                    System.out.println("expression with exponent null (1)");
                if (factor.getTerms()!=null)
                {
                    System.out.println("**expression**");
                    factor.printExpressionFull();
                    System.out.println("**end of expression**");
                }
                else
                {
                    System.out.println("null terms");
                }
            }
            if (factor.getExponent()!= null)
            {
                System.out.println("**exponent**");
                //System.out.println();
                factor.getExponent().printExpressionFull();
                System.out.println("**end of exponent**");
                //System.out.println();
            }
        
        }
    }
    

}