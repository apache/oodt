/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.pushpull.expressions;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.MethodException;

//JDK imports
import java.util.LinkedList;
import java.util.Stack;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class Method {

    private String name;

    private String infix;

    private LinkedList<Variable> args;

    private LinkedList<String> argNames;

    private LinkedList<Integer> argTypes;

    public final int INT = 0;

    public final int STRING = 1;

    public Method(String name) {
        this.name = name;
        args = new LinkedList<Variable>();
        argNames = new LinkedList<String>();
        argTypes = new LinkedList<Integer>();
    }

    public void addArgSignature(String name, int type) {
        argNames.add(name);
        argTypes.add(new Integer(type));
    }

    public boolean addArg(String name, String value) {
        int nextLoc = args.size();
        if (nextLoc >= 0) {
            switch (argTypes.get(nextLoc).intValue()) {
            case INT:
                addArg(new Variable(null, new Integer(value)));
                break;
            case STRING:
                addArg(new Variable(null, value));
                break;
            default:
                return false;
            }
            return true;
        } else
            return false;
    }

    public void addArg(Variable v) {
        args.addLast(v);
    }

    public void setBehavoir(String infix) {
        this.infix = infix;
    }

    private LinkedList<ValidInput> convert(String infix) throws MethodException {
        try {
            LinkedList<ValidInput> output = new LinkedList<ValidInput>();
            Stack<ValidInput> stack = new Stack<ValidInput>();
            char[] infixArray = infix.toCharArray();
            for (int i = 0; i < infixArray.length; i++) {
                char c = infixArray[i];
                // System.out.println("Next C = " + c);
                switch (c) {
                case '$':
                    StringBuffer variable = new StringBuffer("");
                    boolean globalVar = false;

                    // skip $ by incr i and if true then variable is a global
                    // variable and skip '{' by incr i again
                    if (infixArray[++i] == '{') {
                        globalVar = true;
                        i++;
                    }

                    for (; i < infixArray.length; i++) {
                        char ch = infixArray[i];
                        // System.out.println("ch = " + ch);
                        if ((ch <= 'Z' && ch >= 'A')
                                || (ch <= 'z' && ch >= 'a')
                                || (ch <= '9' && ch >= '0') || ch == '_')
                            variable.append(ch);
                        else
                            break;
                    }

                    if (globalVar) {
                        try {
                            output.addLast(GlobalVariables.hashMap.get(variable
                                    .toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        i--;
                        output.addLast(args.get(argNames.indexOf(variable
                                .toString())));
                    }
                    // System.out.println(output);
                    break;
                case '#':
                    StringBuffer variableIntString = new StringBuffer("");
                    int k = i + 1;
                    for (; k < infixArray.length; k++) {
                        char ch = infixArray[k];
                        if (ch <= '9' && ch >= '0')
                            variableIntString.append(ch);
                        else
                            break;
                    }
                    output.addLast(new Variable(null, new Integer(
                            variableIntString.toString())));
                    i = k - 1;
                    break;
                case '"':
                    StringBuffer variableString = new StringBuffer("");
                    int l = i + 1;
                    for (; l < infixArray.length; l++) {
                        char ch = infixArray[l];
                        if (ch != '"')
                            variableString.append(ch);
                        else
                            break;
                    }
                    output
                            .addLast(new Variable(null, variableString
                                    .toString()));
                    i = l;
                    break;
                case '+':
                case '-':
                case '/':
                case '*':
                    // System.out.println("operators");
                    while (!stack.empty()
                            && hasHigherPrecedence(stack.peek().toString()
                                    .charAt(0), c)) {
                        output.addLast(stack.pop());
                    }
                    stack.push(new Operator(c + ""));
                    // System.out.println("Stack: " + stack);
                    break;
                case ')':
                    while (!stack.empty()) {
                        ValidInput vi = stack.pop();
                        if (vi.toString().charAt(0) == '(')
                            break;
                        output.addLast(vi);
                    }
                    break;
                case '(':
                    stack.push(new Punctuation(c + ""));
                    break;

                }
            }
            while (!stack.empty())
                output.addLast(stack.pop());

            return output;
        } catch (Exception e) {
            throw new MethodException("Failed to convert infix to postfix : "
                    + e.getMessage());
        }
    }

    public Object execute() throws MethodException {
        try {
            Stack<ValidInput> stack = new Stack<ValidInput>();
            LinkedList<ValidInput> postfix = convert(infix);
            for (ValidInput vi : postfix) {
                if (vi instanceof Variable) {
                    stack.push(vi);
                } else if (vi instanceof Operator) {
                    ValidInput first = stack.pop();
                    ValidInput second = stack.pop();
                    switch (vi.toString().charAt(0)) {
                    case '+':
                        if (((Variable) first).isString()
                                || ((Variable) second).isString()) {
                            String value = second.toString() + first.toString();
                            stack.push(new Variable(null, value));
                        } else if (((Variable) first).isInteger()
                                && ((Variable) second).isInteger()) {
                            Integer value = new Integer(((Integer) second
                                    .getValue()).intValue()
                                    + ((Integer) first.getValue()).intValue());
                            stack.push(new Variable(null, value));
                        } else {
                            throw new MethodException(
                                    "Invalid Concatination/Addition types. . .must be String or Integer");
                        }
                        break;
                    case '-':
                        if (((Variable) first).isInteger()
                                && ((Variable) second).isInteger()) {
                            Integer value = new Integer(((Integer) second
                                    .getValue()).intValue()
                                    - ((Integer) first.getValue()).intValue());
                            stack.push(new Variable(null, value));
                        } else {
                            throw new MethodException(
                                    "Invalid Subtraction types. . .must be Integer");
                        }
                        break;
                    case '*':
                        if (((Variable) first).isInteger()
                                && ((Variable) second).isInteger()) {
                            Integer value = new Integer(((Integer) second
                                    .getValue()).intValue()
                                    * ((Integer) first.getValue()).intValue());
                            stack.push(new Variable(null, value));
                        } else {
                            throw new MethodException(
                                    "Invalid Multiplication types. . .must be Integer");
                        }
                        break;
                    case '/':
                        if (((Variable) first).isInteger()
                                && ((Variable) second).isInteger()
                                && ((Integer) ((Variable) first).getValue())
                                        .intValue() > 0) {
                            Integer value = new Integer(((Integer) second
                                    .getValue()).intValue()
                                    / ((Integer) first.getValue()).intValue());
                            stack.push(new Variable(null, value));
                        } else {
                            throw new MethodException(
                                    "Invalid Division types. . .must be Integer and denominator must be greater than 0");
                        }
                        break;
                    }
                }
            }
            return stack.pop().getValue();
        } catch (Exception e) {
            throw new MethodException("Failed to execute method " + name
                    + " : " + e.getMessage());
        }
    }

    // does first have higher precedence than second
    private boolean hasHigherPrecedence(char first, char second) {
        switch (first) {
        case '+':
        case '-':
            switch (second) {
            case '+':
            case '-':
                return true;
            }
            return false;
        case '*':
        case '/':
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
