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
package com.googlecode.dex2jar.ir.stmt;

import java.util.Map;

import org.objectweb.asm.Label;

import com.googlecode.dex2jar.ir.stmt.Stmt.E0Stmt;

/**
 * Represent a Label statement
 * 
 * @see ST#LABEL
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class LabelStmt extends E0Stmt {

    public String displayName;
    public Label label;
    public int lineNumber = -1;

    public LabelStmt(Label label) {
        super(ST.LABEL);
        this.label = label;
    }

    @Override
    public LabelStmt clone(Map<LabelStmt, LabelStmt> map) {
        return cloneLabel(map, this);
    }

    public String getDisplayName() {
        return displayName == null ? label.toString() : displayName;
    }

    @Override
    public String toString() {
        if (lineNumber >= 0) {
            return getDisplayName() + ": // line " + lineNumber;
        } else {
            return getDisplayName() + ":";
        }
    }

}
