/*
 * Copyright (c) 2009-2012 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.dex2jar.ir.expr;

import org.objectweb.asm.Type;

import com.googlecode.dex2jar.ir.Value;
import com.googlecode.dex2jar.ir.Value.E2Expr;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a Binop expression, value = op1 vt op2
 * 
 * @see VT#ADD
 * @see VT#AND
 * @see VT#LCMP
 * @see VT#FCMPG
 * @see VT#DCMPG
 * @see VT#FCMPL
 * @see VT#DCMPL
 * @see VT#DIV
 * @see VT#EQ
 * @see VT#GE
 * @see VT#GT
 * @see VT#LE
 * @see VT#LT
 * @see VT#MUL
 * @see VT#NE
 * @see VT#OR
 * @see VT#REM
 * @see VT#SHL
 * @see VT#SHR
 * @see VT#SUB
 * @see VT#USHR
 * @see VT#XOR
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class BinopExpr extends E2Expr {
    public Type type;

    public BinopExpr(VT vt, Value op1, Value op2, Type type) {
        super(vt, new ValueBox(op1), new ValueBox(op2));
        this.type = type;
    }

    @Override
    public Value clone() {
        return new BinopExpr(vt, op1.value.clone(), op2.value.clone(), type);
    }

    @Override
    public String toString() {
        return "(" + op1 + " " + super.vt + " " + op2 + ")";
    }
}
