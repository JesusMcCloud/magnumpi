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
package com.googlecode.dex2jar.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class FieldAdapter implements FieldVisitor {
    protected FieldVisitor fv;

    /**
     * @param fv
     */
    public FieldAdapter(FieldVisitor fv) {
        super();
        this.fv = fv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.FieldVisitor#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return fv.visitAnnotation(desc, visible);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.FieldVisitor#visitAttribute(org.objectweb.asm.Attribute )
     */
    @Override
    public void visitAttribute(Attribute attr) {
        fv.visitAttribute(attr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.FieldVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        fv.visitEnd();
    }

}
