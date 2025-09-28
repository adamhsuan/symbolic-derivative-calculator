import java.util.*;
public class Calculator
{
    /*
    This is a derivitive calculator, a program that takes a mathematical expression and returns
    its derivitive. The main structure of the program is outlined below.
    
    A slideshow outlining how the calculator works at a high level is here:
    https://docs.google.com/presentation/d/14XaiFJIbZxPJq2-HeSK_Dl1nfehQIuyWaqItdpnzXF8/edit?usp=sharing
    
    Calculator class: this is the backbone of the program
        - Deals with the front end: takes user expression and outputs final derivitive
        - obtains final simplified derivitive using getDerivative and simplifyExpression
        - methods: convertStringToExpression and convertExpressionToString
    */
    public static ArrayList<String> output = new ArrayList<String>();
    public static void main(String[] args)
    {        
        //This is the front end loop that continuously asks the user for an expression and returns its derivitive
        Scanner input = new Scanner(System.in);
        String userInput;
        while (true)
        {
            System.out.println("Enter function to take derivitive of (for example, f(x) = e^x-sin(2x)). Enter 'i' for more information. \n");
            //System.out.println("Hit ENTER to take derivitive again.\n");
            System.out.print("f(x) = ");
            userInput = input.nextLine();
            if (userInput.equals("i"))
                System.out.println("\nThis is a symbolic derivitive calculator. You may enter a mathematical expression such as the one above that consists of any of the following symbols and elementary functions: \n\n-digits\n-letters for variables\n-standard mathematical operations + - * / ^\n-parenthesis ( ) and absolute values | |\n-trig functions sin, cos, tan, cot, sec, csc \n-inverse trig functions: arcsin, arccos, arctan, arccot, arcsec, arccsc\n-sqrt() and ln()\n\nNote:\n-functions can be written with or without parenthesis. For example, you can write sin(7x) or sin7x, but keep in mind that with no parenthesis the arguemnt is only the next factor, which in the example is 7, which doesn't include the x.\n\nHere are some more examples: \n\nf(x) = x^x^x\n\nf'(x) = x^x^x-1+x+x^x+1*ln(x)+(ln(x))^2*x^x+1\n\n");
            else
                break;
        }
        Expression expression = convertStringToExpression(userInput);
        Expression.setMainExpression(expression);
        expression.simplifyExpression();
        System.out.println();
        int n = 1;
        while (true)
        {
            expression = expression.takeDerivitive("x");
            expression.simplifyExpression();

            Expression.setMainExpression(expression);
            expression.simplifyExpression();
            for (String string : output)
            {
                System.out.println(string);
            }
            String derivitiveString = convertExpressionToString(expression);
            if (n==1)
                System.out.print("f'(x) = ");
            else if (n==2)
                System.out.print("f''(x) = ");
            else
                System.out.print("f^("+n+")(x) = ");
            System.out.print(derivitiveString);
            System.out.println();
            System.out.println();
            System.out.print("Hit enter to take the "+((int)n+1)+" derivitive. Or enter another function to take derivitive of: ");
            userInput = input.nextLine();
            System.out.println();
            if (!(userInput.equals("")))
            {
                System.out.println("f(x) = "+userInput+"\n");
                expression = convertStringToExpression(userInput);
                expression.simplifyExpression();

                n=0;
            }
            n++;

        }
    }
    /*
    convertStringToExpression takes a string and returns it as a very unsimplified expression object.
    It does so by looping through every character of the string and adding it to where the "cursor" is
    located in the expression, which is given by currentExpression. CurrentExpression is an empty expression
    that goes where the next thing should be located. Mathematical operators change the location of
    currentExpression.
    
    For example, lets say convertStringToExpression is reading 5x+7. It begins by creating an expression with
    a term and places the currentExpression placeholder inside that term. It reads 5 and swaps
    currentExpression with a Num object of value 5, then adds currentExpression to the term again. Read x,
    swap, add. Now it reads + and creates a new term, moving currentExpression to that term... and it
    continues like that. 
    */
    public static Expression convertStringToExpression(String string)
    {
        string+=" ";
        Expression currentExpression = new Expression();
        Expression expression = new Expression();
        expression.addFactor(currentExpression);
        boolean isNum = false;
        int currentNum = 0;
        int decimalDigits = -1;
        boolean openAbsoluteValue = true;
        for (int i=0; i<string.length(); i++)
        {
            char character = string.charAt(i);
            if (character==' ')
            {
                currentExpression.setIsNoParenthesisArgument(false);
            }
            if ((!(Character.isDigit(character) || character=='.') && isNum==true))
            {
                Expression number;
                if (decimalDigits==-1)
                    number = new Num(currentExpression.getExponent(),currentNum);
                else
                {
                    number = new Expression(currentExpression.getExponent());
                    number.addFactor(new Num(currentNum));
                    number.addFactor(new Num(Expression.createExpressionWithFactor(new Num(-1)),(int)Math.pow(10,decimalDigits)));
                }
                Term outerTerm = (Term) currentExpression.getOuterExpression();
                outerTerm.addFactor(number);
                if (currentExpression.getIsNoParenthesisArgument())
                {
                    outerTerm = (Term) outerTerm.getOuterExpression().getOuterExpression();

                }
                if (character=='^')
                    currentExpression = number;
                else
                {
                    currentExpression = new Expression();
                    outerTerm.addFactor(currentExpression);
                    currentNum = 0;
                    isNum=false;
                }
                currentExpression.setOuterExpression(outerTerm);
                decimalDigits = -1;
            }
            if (Character.isDigit(character))
            {
                isNum = true;
                currentNum = currentNum*10+(int)(character-'0');
                if (decimalDigits!=-1)
                    decimalDigits++;
        
            }
            else if (character == '.')
            {
                decimalDigits = 0;
            }
            else if (character == '/')
            {
                
                Expression exponentExpression = new Expression();
                exponentExpression.addFactor(new Num(-1));
                currentExpression.setExponent(exponentExpression);
            }
            else if (character == '+' || character == '-')
            {
                Expression outerExpression = currentExpression.getOuterExpression().getOuterExpression();
                currentExpression = new Expression();
                if (character=='-')
                {
                    Term newTerm = new Term(new ArrayList<Expression>());
                    newTerm.addFactor(new Num(-1));
                    newTerm.addFactor(currentExpression);
                    outerExpression.addTerm(newTerm);
                }
                else
                    outerExpression.addFactor(currentExpression);
            }
            else if (Character.isLetter(character))
            {
                boolean isFunction = false;
                for (String functionName : new String[]{"sin","cos","tan","cot","sec","csc","arcsin","arccos","arctan","arccot","arcsec","arccsc","ln","sqrt"})
                {
                    if (i+functionName.length()<string.length() && string.substring(i,i+functionName.length()).equals(functionName))
                    {
                        isFunction = true;
                        i+=functionName.length()-1;
                        Term outerTerm = (Term) currentExpression.getOuterExpression();
                        Function function = new Function(currentExpression.getExponent(),functionName);
                        outerTerm.addFactor(function);
                        Expression argument = new Expression();
                        function.setArgument(argument);
                        currentExpression = new Expression();
                        argument.addFactor(currentExpression);
                        if (string.charAt(i+1)=='(')
                            i++;
                        else
                        {
                            currentExpression.setIsNoParenthesisArgument(true);
                        }
                    }
                }
                if (isFunction==false)
                {
                    Term outerTerm = (Term) currentExpression.getOuterExpression();
                    Variable variable = new Variable(currentExpression.getExponent(),Character.toString(character));
                    outerTerm.addFactor(variable);
                    if (currentExpression.getIsNoParenthesisArgument())
                    {
                        outerTerm = (Term) outerTerm.getOuterExpression().getOuterExpression();

                    }
                    variable.setOuterExpression(outerTerm);
                    currentExpression = new Expression();
                    if (i+1<string.length() && string.charAt(i+1)=='^')
                    {
                        currentExpression = variable;
                    }
                    else
                    {
                        outerTerm.addFactor(currentExpression);
                    }

                }
            }
            else if (character == '(' || character == '|' && openAbsoluteValue == true)
            {
                Expression newExpression = new Expression(currentExpression.getExponent());
                currentExpression.getOuterExpression().addFactor(newExpression);
                currentExpression = new Expression();
                newExpression.addFactor(currentExpression);
                if (character == '|')
                {
                    newExpression.setIsAbsoluteValue(true);
                    openAbsoluteValue = false;
                }
                else
                    openAbsoluteValue = true;
            }
            else if (character == ')' || character == '|' && openAbsoluteValue == false)
            {
                Expression outerExpression = currentExpression.getOuterExpression().getOuterExpression();
                if (i+1<string.length() && string.charAt(i+1)=='^')
                {
                    currentExpression = outerExpression;
                }
                else
                {
                    currentExpression = new Expression();
                    ((Term) outerExpression.getOuterExpression()).addFactor(currentExpression);
                }                
                //verify this works
                if (!(character == ')' && outerExpression.getOuterExpression().getIsAbsoluteValue()))
                    openAbsoluteValue = true;
            }
            else if (character =='^')
            {   
                if (isNum=true)
                {
                    Expression base = currentExpression;
                    currentNum = 0;
                    isNum=false;
                }

                Expression base = currentExpression;
                base.setOuterExpression(currentExpression.getOuterExpression());
                Num negativeOne = new Num(-1);
                if (currentExpression.getExponent()!=null)
                {
                    currentExpression.setExponent(negativeOne);
                }
                Expression exponent = currentExpression.getExponent();
                currentExpression = new Expression();
                if (exponent==negativeOne)
                {
                    exponent=new Expression();
                    base.setExponent(exponent);
                    Term exponentTerm = new Term();
                    exponent.addTerm(exponentTerm);
                    exponentTerm.addFactor(negativeOne);
                    exponentTerm.addFactor(currentExpression);
                }
                else
                {
                    exponent = Expression.createExpressionWithFactor(currentExpression);
                    base.setExponent(exponent);
                }
                if (string.charAt(i+1)=='(')
                    i++;
                else
                    currentExpression.setIsNoParenthesisArgument(true);
                

            }
        }
        return expression;
    }
    
    /*
    convertExpressionToString does the opposite of convertStringToExpression: it takes an expression
    object and returns a string. It does so with a recursive loop that branches into the main expression
    object (this is a teqnique that re-emerges many times in the program) and, once reaching a node (an
    expression containing no expression terms, factors, arguments, or exponents), converts it into
    a string. Those strings are fed into the previous layer of the object tree, converting those to
    strings, and this occurs all the way up the tree until the entire expression object has been converted
    to a string.
    */
    public static boolean dontPrint = false;
    public static String convertExpressionToString(Expression expression)
    {
        Expression oneHalf = Expression.createExpressionWithFactor(new Num(2));
        oneHalf.getFirstFactor().setExponent(Expression.createExpressionWithFactor(new Num(-1)));
        Expression negativeOneHalf = Expression.createDeepCopy(oneHalf);
        negativeOneHalf.addFactor(new Num(-1));
        if (expression.getExponent() != null)
        {
            if (expression.getExponent().equals(oneHalf) || expression.getExponent().equals(negativeOneHalf))
            {
                Function sqrtExpression = new Function();
                sqrtExpression.setType("sqrt");
                if (expression.getExponent().equals(negativeOneHalf))
                    sqrtExpression.setExponent(Expression.createExpressionWithFactor(new Num(-1)));
                Expression argument = Expression.createDeepCopy(expression);
                argument.setExponent(null);
                argument = Expression.createExpressionWithFactor(argument);
                sqrtExpression.setArgument(argument);
                expression = sqrtExpression;
            }
            
        }
        String string = "";     
        int index0=string.length();
        if (expression.isInstanceOfExpression())
        {
            if (expression.getIsAbsoluteValue())
                string+="|";
            else
                string+="(";
            for (Expression term : expression.getTerms())
            {
                String newString=convertExpressionToString(term);
                if (newString.length()>0&&newString.charAt(0)=='-'&&!(string.equals("(")))
                    string=string.substring(0,string.length()-1);
                string+=newString;
                if (string.length()>0)
                {
                    if (string.charAt(string.length()-1)=='*')
                    {
                        string=string.substring(0,string.length()-1);
                    }
                }
                string+="+";
            }
            if (expression.getTerms().size()>0)
            {
                string=string.substring(0,string.length()-1);
            }
            if (expression.getIsAbsoluteValue())
                string+="|";
            else
                string+=")";
            if (expression.getOuterExpression()==null)
                string=string.substring(1,string.length()-1);
        }
        else if (expression instanceof Term)
        {
            Term denominatorTerm = new Term();
            for (Expression factor : ((Term)expression).getFactors())
            {
                boolean inDenominator=false;
                if (factor.getExponent()!=null&&factor.getExponent().getTerms().size()==1)
                {
                    for (Expression exponentFactor: ((Term)factor.getExponent().getTerms().get(0)).getFactors())
                    {
                        if (exponentFactor instanceof Num && ((Num)exponentFactor).getValue()<0)
                        {
                            Expression newFactor = Expression.createDeepCopy(factor);
                            Term newExponentTerm = (Term) Expression.createDeepCopy(factor.getExponent().getTerms().get(0));
                            newExponentTerm.addFactor(new Num(-1));
                            Expression newExponentExpression = new Expression();
                            newExponentExpression.addTerm(newExponentTerm);
                            //ADD THIS BACK
                            dontPrint = true;
                            newExponentExpression.simplifyExpression();
                            dontPrint = false;
                            newFactor.setExponent(newExponentExpression);
                            denominatorTerm.addFactor(newFactor);
                            inDenominator=true;
                            break;
                        }
                    }
                }
                if (inDenominator==false)
                {
                    if (string.equals("-1"))
                    {
                        string="-";
                    }
                    if (string.length()>0)
                    {
                        if (Character.isDigit(string.charAt(string.length()-1)) && factor instanceof Num)
                        {
                            string+="*";
                        }
                    }
                    if (factor instanceof Num && ((Num)factor).getValue()<0 && !string.equals(""))
                        string+="*";
                    if (!(factor instanceof Num && ((Num)factor).getValue()==1 && string.equals("") && ((Term)expression).getFactors().size()>1))
                        string+=convertExpressionToString(factor);
                        
                }
            }
            //ADD THIS BACK
            dontPrint = true;
            denominatorTerm.simplifyTerm();
            dontPrint = false;
            if (denominatorTerm.getFactors().size()>0&&!(denominatorTerm.getFactors().size()==1&&denominatorTerm.getFactors().get(0) instanceof Num&&((Num)denominatorTerm.getFactors().get(0)).getValue()==1&&denominatorTerm.getFactors().get(0).getExponent()==null))
            {
                if (string.length()>0)
                {
                    if (string.charAt(string.length()-1)=='*')
                        string=string.substring(0,string.length()-1);
                }
                else
                    string+="1";
                string+="/";
                if (denominatorTerm.getFactors().size()>1)
                {
                    string+="(";
                    string+=convertExpressionToString(denominatorTerm);
                    string+=")";
                }
                else
                {
                    string+=convertExpressionToString(denominatorTerm);
                }   
            }
            if (string.length()>0&&string.charAt(string.length()-1)=='*')
                string=string.substring(0,string.length()-1);
        }
        else if (expression instanceof Num)
        {
            string+=((Num)expression).getValue();
        }
        else if (expression instanceof Variable)
        {
            string+=((Variable)expression).getSymbol();
        }
        else if (expression instanceof Function)
        {
            if (expression.getExponent()!=null)
                string+="(";
            string+=((Function)expression).getType();
            //not ideal, fix
            //if (((Function)expression).getType().equals("sqrt"))
                 string+="(";
            String argument = convertExpressionToString(((Function)expression).getArgument());
            if (argument.length()>0 && argument.charAt(0)=='(')
                argument = argument.substring(1,argument.length()-1);
            string+=argument;
            //if (((Function)expression).getType().equals("sqrt"))
                 string+=")";
            if (expression.getExponent()!=null)
                string+=")";
        }
        int index=string.length();
        if (expression.getExponent()!=null)
        {
            string+="^"+convertExpressionToString(expression.getExponent());
            if (string.length()>index+2 && string.charAt(index+1)=='('&&expression.getExponent().getTerms().size()==1 && ((Term)expression.getExponent().getTerms().get(0)).getFactors().size()==1 && ((Term)expression.getExponent().getTerms().get(0)).getFactors().get(0).getExponent()==null)
            {
                string=string.substring(0,index+1)+string.substring(index+2,string.length()-1);
                if (((Term)expression.getExponent().getTerms().get(0)).getFactors().get(0) instanceof Num && ((Num)((Term)expression.getExponent().getTerms().get(0)).getFactors().get(0)).getValue()==1)
                    string=string.substring(0,string.length()-2);
            }
            string+="*";
        }
        return string;
    }
    
    //this is for testing and should be removed after program finalization
    public static void printExpression(Expression expression)
    {
        System.out.println(convertExpressionToString(expression));
    }

}
