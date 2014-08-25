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
package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.Value.E0Expr;

/**
 * TODO DOC
 * 
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class Local extends E0Expr {
    public int _ls_index;
    public int _ls_read_count;

    public ValueBox _ls_vb;
    public int _ls_write_count;
    public String name;

    public Local(String name) {
        super(Value.VT.LOCAL);
        this.name = name;
    }

    @Override
    public Value clone() {
        return this;
    }

    @Override
    public String toString() {
        return name;
    }
}
