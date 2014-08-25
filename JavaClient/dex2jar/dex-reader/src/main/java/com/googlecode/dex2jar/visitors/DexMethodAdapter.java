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
package com.googlecode.dex2jar.visitors;


/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexMethodAdapter implements DexMethodVisitor {
    protected DexMethodVisitor mv;

    /**
     * @param mv
     */
    public DexMethodAdapter(DexMethodVisitor mv) {
        super();
        this.mv = mv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexAnnotationAble#visitAnnotation(java.lang.String, boolean)
     */
    @Override
    public DexAnnotationVisitor visitAnnotation(String name, boolean visible) {
        return mv.visitAnnotation(name, visible);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitCode()
     */
    @Override
    public DexCodeVisitor visitCode() {
        return mv.visitCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitEnd()
     */
    @Override
    public void visitEnd() {
        mv.visitEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.googlecode.dex2jar.visitors.DexMethodVisitor#visitParameterAnnotation (int)
     */
    @Override
    public DexAnnotationAble visitParameterAnnotation(int index) {
        return mv.visitParameterAnnotation(index);
    }

}
