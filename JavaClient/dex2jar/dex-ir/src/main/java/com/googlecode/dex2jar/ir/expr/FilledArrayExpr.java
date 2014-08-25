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
import com.googlecode.dex2jar.ir.Value.EnExpr;
import com.googlecode.dex2jar.ir.Value.VT;
import com.googlecode.dex2jar.ir.ValueBox;

/**
 * Represent a FILLED_ARRAY expression.
 * 
 * @see VT#FILLED_ARRAY
 */
public class FilledArrayExpr extends EnExpr {

    public Type type;

    public FilledArrayExpr(ValueBox[] datas, Type type) {
        super(VT.FILLED_ARRAY, datas);
        this.type = type;
    }

    @Override
    public Value clone() {
        ValueBox[] nOps = new ValueBox[ops.length];
        for (int i = 0; i < nOps.length; i++) {
            nOps[i] = new ValueBox(ops[i].value.clone());
        }
        return new FilledArrayExpr(nOps, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append('{');
        for (int i = 0; i < ops.length; i++) {
            sb.append(ops[i]).append(", ");
        }
        if (ops.length > 0) {
            sb.setLength(sb.length() - 2); // remove tail ", "
        }
        sb.append('}');
        return sb.toString();
    }
}
