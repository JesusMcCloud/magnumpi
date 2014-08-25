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
package com.googlecode.dex2jar.reader;

import java.util.Map;

import com.googlecode.dex2jar.DexException;
import com.googlecode.dex2jar.DexLabel;
import com.googlecode.dex2jar.Method;
import com.googlecode.dex2jar.OdexOpcodes;
import com.googlecode.dex2jar.visitors.DexCodeVisitor;
import com.googlecode.dex2jar.visitors.OdexCodeVisitor;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
/* default */class DexOpcodeAdapter implements OdexOpcodes, DexInternalOpcode {
    private DexCodeVisitor dcv;
    private DexFileReader dex;

    private Map<Integer, DexLabel> labels;
    private int offset;

    /**
     * @param dex
     * @param labels
     */
    /* package */DexOpcodeAdapter(DexFileReader dex, Map<Integer, DexLabel> labels, DexCodeVisitor dcv) {
        super();
        this.dex = dex;
        this.labels = labels;
        this.dcv = dcv;
    }

    private DexLabel getLabel(int offset) {
        return labels.get(this.offset + offset);
    }

    /* package */void offset(int currentOffset) {
        this.offset = currentOffset;
        DexLabel label = getLabel(0);
        if (label != null) {
            dcv.visitLabel(label);
        }
    }

    public void visitFillArrayStmt(int opcode, int aA, int elemWidth, int initLength, Object[] values) {
        dcv.visitFillArrayStmt(opcode, aA, elemWidth, initLength, values);
    }

    public void visitLookupSwitchStmt(int opcode, int aA, int defaultOffset, int[] cases, int[] iLabel) {
        DexLabel[] labels = new DexLabel[iLabel.length];
        for (int i = 0; i < iLabel.length; i++) {
            labels[i] = getLabel(iLabel[i]);
        }
        dcv.visitLookupSwitchStmt(opcode, aA, getLabel(defaultOffset), cases, labels);
    }

    public void visitTableSwitchStmt(int opcode, int aA, int defaultOffset, int first_case, int last_case, int[] iLabel) {
        DexLabel[] labels = new DexLabel[iLabel.length];
        for (int i = 0; i < iLabel.length; i++) {
            labels[i] = getLabel(iLabel[i]);
        }
        dcv.visitTableSwitchStmt(opcode, aA, getLabel(defaultOffset), first_case, last_case, labels);
    }

    /**
     * <pre>
     * OP_GOTO 
     * OP_GOTO_16 
     * OP_GOTO_32
     * </pre>
     * 
     * @param opcode
     * @param offset
     */
    public void x0t(int opcode, int offset) {
        switch (opcode) {
        case OP_GOTO:
        case OP_GOTO_16:
        case OP_GOTO_32:
            dcv.visitJumpStmt(OP_GOTO, getLabel(offset));
            break;
        default:
            throw new RuntimeException("");
        }

    }

    /**
     * <pre>
     * OP_NOP
     * OP_RETURN_VOID
     * </pre>
     * 
     * @param opcode
     */
    public void x0x(int opcode) {
        switch (opcode) {
        case OP_NOP:
            break;
        case OP_RETURN_VOID:
        case OP_RETURN_VOID_BARRIER:
            dcv.visitReturnStmt(OP_RETURN_VOID);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1c(int opcode, int a, int b) {
        switch (opcode) {
        case OP_CONST_STRING:
        case OP_CONST_STRING_JUMBO:
            dcv.visitConstStmt(OP_CONST_STRING, a, dex.getString(b), TYPE_OBJECT);
            break;
        case OP_CONST_CLASS:
        case OP_CONST_CLASS_JUMBO:
            dcv.visitConstStmt(OP_CONST_CLASS, a, dex.getType(b), TYPE_OBJECT);
            break;
        case OP_CHECK_CAST:
        case OP_CHECK_CAST_JUMBO:
            dcv.visitClassStmt(OP_CHECK_CAST, a, dex.getType(b));
            break;
        case OP_NEW_INSTANCE:
        case OP_NEW_INSTANCE_JUMBO:
            dcv.visitClassStmt(OP_NEW_INSTANCE, a, dex.getType(b));
            break;
        case OP_SGET:
        case OP_SGET_JUMBO:
        case OP_SGET_VOLATILE:
        case OP_SGET_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_SINGLE);
            break;
        case OP_SGET_WIDE:
        case OP_SGET_WIDE_JUMBO:
        case OP_SGET_WIDE_VOLATILE:
        case OP_SGET_WIDE_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_WIDE);
            break;
        case OP_SGET_OBJECT:
        case OP_SGET_OBJECT_JUMBO:
        case OP_SGET_OBJECT_VOLATILE:
        case OP_SGET_OBJECT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_OBJECT);
            break;
        case OP_SGET_BOOLEAN:
        case OP_SGET_BOOLEAN_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_BOOLEAN);
            break;
        case OP_SGET_BYTE:
        case OP_SGET_BYTE_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_BYTE);
            break;
        case OP_SGET_CHAR:
        case OP_SGET_CHAR_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_CHAR);
            break;
        case OP_SGET_SHORT:
        case OP_SGET_SHORT_JUMBO:
            dcv.visitFieldStmt(OP_SGET, a, dex.getField(b), TYPE_SHORT);
            break;
        case OP_SPUT:
        case OP_SPUT_JUMBO:
        case OP_SPUT_VOLATILE:
        case OP_SPUT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_SINGLE);
            break;
        case OP_SPUT_WIDE:
        case OP_SPUT_WIDE_JUMBO:
        case OP_SPUT_WIDE_VOLATILE:
        case OP_SPUT_WIDE_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_WIDE);
            break;
        case OP_SPUT_OBJECT:
        case OP_SPUT_OBJECT_JUMBO:
        case OP_SPUT_OBJECT_VOLATILE:
        case OP_SPUT_OBJECT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_OBJECT);
            break;
        case OP_SPUT_BOOLEAN:
        case OP_SPUT_BOOLEAN_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_BOOLEAN);
            break;
        case OP_SPUT_BYTE:
        case OP_SPUT_BYTE_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_BYTE);
            break;
        case OP_SPUT_CHAR:
        case OP_SPUT_CHAR_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_CHAR);
            break;
        case OP_SPUT_SHORT:
        case OP_SPUT_SHORT_JUMBO:
            dcv.visitFieldStmt(OP_SPUT, a, dex.getField(b), TYPE_SHORT);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1h(int opcode, int a, int b) {
        switch (opcode) {
        case OP_CONST_HIGH16:
            dcv.visitConstStmt(OP_CONST, a, b << 16, TYPE_SINGLE);
            break;
        case OP_CONST_WIDE_HIGH16:
            dcv.visitConstStmt(OP_CONST, a, ((long) b) << 48, TYPE_WIDE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1i(int opcode, int a, int b) {
        switch (opcode) {
        case OP_CONST:
            dcv.visitConstStmt(opcode, a, b, TYPE_SINGLE);
            break;
        case OP_CONST_WIDE_32:
            dcv.visitConstStmt(OP_CONST, a, (long) b, TYPE_WIDE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1l(int opcode, int a, long b) {
        switch (opcode) {
        case OP_CONST_WIDE:
            dcv.visitConstStmt(OP_CONST, a, b, TYPE_WIDE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    /**
     * OP_CONST_4
     * 
     * @param opcode
     * @param a
     * @param b
     */
    public void x1n(int opcode, int a, int b) {
        switch (opcode) {
        case OP_CONST_4:
            dcv.visitConstStmt(OP_CONST, a, b, TYPE_SINGLE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1s(int opcode, int a, int b) {
        switch (opcode) {
        case OP_CONST_16:
            dcv.visitConstStmt(OP_CONST, a, b, TYPE_SINGLE);
            break;
        case OP_CONST_WIDE_16:
            dcv.visitConstStmt(OP_CONST, a, (long) b, TYPE_WIDE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1t(int opcode, int a, int offset) {
        switch (opcode) {
        case OP_IF_EQZ:
        case OP_IF_NEZ:
        case OP_IF_LTZ:
        case OP_IF_GEZ:
        case OP_IF_GTZ:
        case OP_IF_LEZ:
            dcv.visitJumpStmt(opcode, a, getLabel(offset));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x1x(int opcode, int a) {
        switch (opcode) {
        case OP_MOVE_RESULT:
            dcv.visitMoveStmt(OP_MOVE_RESULT, a, TYPE_SINGLE);
            break;
        case OP_MOVE_RESULT_WIDE:
            dcv.visitMoveStmt(OP_MOVE_RESULT, a, TYPE_WIDE);
            break;
        case OP_MOVE_RESULT_OBJECT:
            dcv.visitMoveStmt(OP_MOVE_RESULT, a, TYPE_OBJECT);
            break;
        case OP_MOVE_EXCEPTION:
            dcv.visitMoveStmt(OP_MOVE_EXCEPTION, a, TYPE_OBJECT);
            break;
        case OP_RETURN:
            dcv.visitReturnStmt(OP_RETURN, a, TYPE_SINGLE);
            break;
        case OP_RETURN_WIDE:
            dcv.visitReturnStmt(OP_RETURN, a, TYPE_WIDE);
            break;
        case OP_RETURN_OBJECT:
            dcv.visitReturnStmt(OP_RETURN, a, TYPE_OBJECT);
            break;
        case OP_THROW:
            dcv.visitReturnStmt(opcode, a, TYPE_OBJECT);
            break;
        case OP_MONITOR_ENTER:
        case OP_MONITOR_EXIT:
            dcv.visitMonitorStmt(opcode, a);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2b(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_ADD_INT_LIT8:
        case OP_RSUB_INT_LIT8:
        case OP_MUL_INT_LIT8:
        case OP_DIV_INT_LIT8:
        case OP_REM_INT_LIT8:
        case OP_AND_INT_LIT8:
        case OP_OR_INT_LIT8:
        case OP_XOR_INT_LIT8:
        case OP_SHL_INT_LIT8:
        case OP_SHR_INT_LIT8:
        case OP_USHR_INT_LIT8:
            dcv.visitBinopLitXStmt(opcode - (OP_ADD_INT_LIT8 - OP_ADD_INT_LIT_X), a, b, c);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2c(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_INSTANCE_OF:
        case OP_INSTANCE_OF_JUMBO:
            dcv.visitClassStmt(OP_INSTANCE_OF, a, b, dex.getType(c));
            break;
        case OP_NEW_ARRAY:
        case OP_NEW_ARRAY_JUMBO:
            dcv.visitClassStmt(OP_NEW_ARRAY, a, b, dex.getType(c));
            break;
        case OP_IGET:
        case OP_IGET_JUMBO:
        case OP_IGET_VOLATILE:
        case OP_IGET_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_SINGLE);
            break;
        case OP_IGET_WIDE:
        case OP_IGET_WIDE_JUMBO:
        case OP_IGET_WIDE_VOLATILE:
        case OP_IGET_WIDE_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_WIDE);
            break;
        case OP_IGET_OBJECT:
        case OP_IGET_OBJECT_JUMBO:
        case OP_IGET_OBJECT_VOLATILE:
        case OP_IGET_OBJECT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_OBJECT);
            break;
        case OP_IGET_BOOLEAN:
        case OP_IGET_BOOLEAN_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_BOOLEAN);
            break;
        case OP_IGET_BYTE:
        case OP_IGET_BYTE_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_BYTE);
            break;
        case OP_IGET_CHAR:
        case OP_IGET_CHAR_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_CHAR);
            break;
        case OP_IGET_SHORT:
        case OP_IGET_SHORT_JUMBO:
            dcv.visitFieldStmt(OP_IGET, a, b, dex.getField(c), TYPE_SHORT);
            break;
        case OP_IPUT:
        case OP_IPUT_JUMBO:
        case OP_IPUT_VOLATILE:
        case OP_IPUT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_SINGLE);
            break;
        case OP_IPUT_WIDE:
        case OP_IPUT_WIDE_JUMBO:
        case OP_IPUT_WIDE_VOLATILE:
        case OP_IPUT_WIDE_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_WIDE);
            break;
        case OP_IPUT_OBJECT:
        case OP_IPUT_OBJECT_JUMBO:
        case OP_IPUT_OBJECT_VOLATILE:
        case OP_IPUT_OBJECT_VOLATILE_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_OBJECT);
            break;
        case OP_IPUT_BOOLEAN:
        case OP_IPUT_BOOLEAN_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_BOOLEAN);
            break;
        case OP_IPUT_BYTE:
        case OP_IPUT_BYTE_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_BYTE);
            break;
        case OP_IPUT_CHAR:
        case OP_IPUT_CHAR_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_CHAR);
            break;
        case OP_IPUT_SHORT:
        case OP_IPUT_SHORT_JUMBO:
            dcv.visitFieldStmt(OP_IPUT, a, b, dex.getField(c), TYPE_SHORT);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2s(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_ADD_INT_LIT16:
        case OP_RSUB_INT:
        case OP_MUL_INT_LIT16:
        case OP_DIV_INT_LIT16:
        case OP_REM_INT_LIT16:
        case OP_AND_INT_LIT16:
        case OP_OR_INT_LIT16:
        case OP_XOR_INT_LIT16:
            dcv.visitBinopLitXStmt(opcode - (OP_ADD_INT_LIT16 - OP_ADD_INT_LIT_X), a, b, c);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2t(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_IF_EQ:
        case OP_IF_NE:
        case OP_IF_LT:
        case OP_IF_GE:
        case OP_IF_GT:
        case OP_IF_LE:
            dcv.visitJumpStmt(opcode, a, b, getLabel(c));
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x2x(int opcode, int a, int b) {
        switch (opcode) {
        case OP_MOVE:
        case OP_MOVE_FROM16:
        case OP_MOVE_16:
            dcv.visitMoveStmt(OP_MOVE, a, b, TYPE_SINGLE);
            break;
        case OP_MOVE_WIDE:
        case OP_MOVE_WIDE_FROM16:
        case OP_MOVE_WIDE_16:
            dcv.visitMoveStmt(OP_MOVE, a, b, TYPE_WIDE);
            break;
        case OP_MOVE_OBJECT:
        case OP_MOVE_OBJECT_FROM16:
        case OP_MOVE_OBJECT_16:
            dcv.visitMoveStmt(OP_MOVE, a, b, TYPE_OBJECT);
            break;
        case OP_ARRAY_LENGTH:
            dcv.visitUnopStmt(OP_ARRAY_LENGTH, a, b, TYPE_INT);
            break;
        case OP_NEG_INT:
        case OP_NOT_INT:
            dcv.visitUnopStmt(opcode - (OP_NEG_INT - OP_NEG), a, b, TYPE_INT);
            break;
        case OP_NEG_LONG:
        case OP_NOT_LONG:
            dcv.visitUnopStmt(opcode - (OP_NEG_LONG - OP_NEG), a, b, TYPE_LONG);
            break;
        case OP_NEG_FLOAT:
            dcv.visitUnopStmt(OP_NEG, a, b, TYPE_FLOAT);
            break;
        case OP_NEG_DOUBLE:
            dcv.visitUnopStmt(OP_NEG, a, b, TYPE_DOUBLE);
            break;
        case OP_INT_TO_LONG:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_LONG);
            break;
        case OP_INT_TO_FLOAT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_FLOAT);
            break;
        case OP_INT_TO_DOUBLE:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_DOUBLE);
            break;
        case OP_LONG_TO_INT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_LONG, TYPE_INT);
            break;
        case OP_LONG_TO_FLOAT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_LONG, TYPE_FLOAT);
            break;
        case OP_LONG_TO_DOUBLE:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_LONG, TYPE_DOUBLE);
            break;
        case OP_FLOAT_TO_INT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_FLOAT, TYPE_INT);
            break;
        case OP_FLOAT_TO_LONG:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_FLOAT, TYPE_LONG);
            break;
        case OP_FLOAT_TO_DOUBLE:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_FLOAT, TYPE_DOUBLE);
            break;
        case OP_DOUBLE_TO_INT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_DOUBLE, TYPE_INT);
            break;
        case OP_DOUBLE_TO_LONG:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_DOUBLE, TYPE_LONG);
            break;
        case OP_DOUBLE_TO_FLOAT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_DOUBLE, TYPE_FLOAT);
            break;
        case OP_INT_TO_BYTE:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_BYTE);
            break;
        case OP_INT_TO_CHAR:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_CHAR);
            break;
        case OP_INT_TO_SHORT:
            dcv.visitUnopStmt(OP_X_TO_Y, a, b, TYPE_INT, TYPE_SHORT);
            break;
        case OP_ADD_INT_2ADDR:
        case OP_SUB_INT_2ADDR:
        case OP_MUL_INT_2ADDR:
        case OP_DIV_INT_2ADDR:
        case OP_REM_INT_2ADDR:
        case OP_AND_INT_2ADDR:
        case OP_OR_INT_2ADDR:
        case OP_XOR_INT_2ADDR:
        case OP_SHL_INT_2ADDR:
        case OP_SHR_INT_2ADDR:
        case OP_USHR_INT_2ADDR:
            dcv.visitBinopStmt(opcode - (OP_ADD_INT_2ADDR - OP_ADD), a, a, b, TYPE_INT);
            break;
        case OP_ADD_LONG_2ADDR:
        case OP_SUB_LONG_2ADDR:
        case OP_MUL_LONG_2ADDR:
        case OP_DIV_LONG_2ADDR:
        case OP_REM_LONG_2ADDR:
        case OP_AND_LONG_2ADDR:
        case OP_OR_LONG_2ADDR:
        case OP_XOR_LONG_2ADDR:
        case OP_SHL_LONG_2ADDR:
        case OP_SHR_LONG_2ADDR:
        case OP_USHR_LONG_2ADDR:
            dcv.visitBinopStmt(opcode - (OP_ADD_LONG_2ADDR - OP_ADD), a, a, b, TYPE_LONG);
            break;
        case OP_ADD_FLOAT_2ADDR:
        case OP_SUB_FLOAT_2ADDR:
        case OP_MUL_FLOAT_2ADDR:
        case OP_DIV_FLOAT_2ADDR:
        case OP_REM_FLOAT_2ADDR:
            dcv.visitBinopStmt(opcode - (OP_ADD_FLOAT_2ADDR - OP_ADD), a, a, b, TYPE_FLOAT);
            break;
        case OP_ADD_DOUBLE_2ADDR:
        case OP_SUB_DOUBLE_2ADDR:
        case OP_MUL_DOUBLE_2ADDR:
        case OP_DIV_DOUBLE_2ADDR:
        case OP_REM_DOUBLE_2ADDR:
            dcv.visitBinopStmt(opcode - (OP_ADD_DOUBLE_2ADDR - OP_ADD), a, a, b, TYPE_DOUBLE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x3x(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_CMPL_FLOAT:
            dcv.visitCmpStmt(OP_CMPL, a, b, c, TYPE_FLOAT);
            break;
        case OP_CMPG_FLOAT:
            dcv.visitCmpStmt(OP_CMPG, a, b, c, TYPE_FLOAT);
            break;
        case OP_CMPL_DOUBLE:
            dcv.visitCmpStmt(OP_CMPL, a, b, c, TYPE_DOUBLE);
            break;
        case OP_CMPG_DOUBLE:
            dcv.visitCmpStmt(OP_CMPG, a, b, c, TYPE_DOUBLE);
            break;
        case OP_CMP_LONG:
            dcv.visitCmpStmt(OP_CMP, a, b, c, TYPE_LONG);
            break;
        case OP_AGET:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_SINGLE);
            break;
        case OP_AGET_WIDE:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_WIDE);
            break;
        case OP_AGET_OBJECT:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_OBJECT);
            break;
        case OP_AGET_BOOLEAN:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_BOOLEAN);
            break;
        case OP_AGET_BYTE:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_BYTE);
            break;
        case OP_AGET_CHAR:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_CHAR);
            break;
        case OP_AGET_SHORT:
            dcv.visitArrayStmt(OP_AGET, a, b, c, TYPE_SHORT);
            break;
        case OP_APUT:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_SINGLE);
            break;
        case OP_APUT_WIDE:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_WIDE);
            break;
        case OP_APUT_OBJECT:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_OBJECT);
            break;
        case OP_APUT_BOOLEAN:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_BOOLEAN);
            break;
        case OP_APUT_BYTE:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_BYTE);
            break;
        case OP_APUT_CHAR:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_CHAR);
            break;
        case OP_APUT_SHORT:
            dcv.visitArrayStmt(OP_APUT, a, b, c, TYPE_SHORT);
            break;
        case OP_ADD_INT:
        case OP_SUB_INT:
        case OP_MUL_INT:
        case OP_DIV_INT:
        case OP_REM_INT:
        case OP_AND_INT:
        case OP_OR_INT:
        case OP_XOR_INT:
        case OP_SHL_INT:
        case OP_SHR_INT:
        case OP_USHR_INT:
            dcv.visitBinopStmt(opcode - (OP_ADD_INT - OP_ADD), a, b, c, TYPE_INT);
            break;
        case OP_ADD_LONG:
        case OP_SUB_LONG:
        case OP_MUL_LONG:
        case OP_DIV_LONG:
        case OP_REM_LONG:
        case OP_AND_LONG:
        case OP_OR_LONG:
        case OP_XOR_LONG:
        case OP_SHL_LONG:
        case OP_SHR_LONG:
        case OP_USHR_LONG:
            dcv.visitBinopStmt(opcode - (OP_ADD_LONG - OP_ADD), a, b, c, TYPE_LONG);
            break;
        case OP_ADD_FLOAT:
        case OP_SUB_FLOAT:
        case OP_MUL_FLOAT:
        case OP_DIV_FLOAT:
        case OP_REM_FLOAT:
            dcv.visitBinopStmt(opcode - (OP_ADD_FLOAT - OP_ADD), a, b, c, TYPE_FLOAT);
            break;
        case OP_ADD_DOUBLE:
        case OP_SUB_DOUBLE:
        case OP_MUL_DOUBLE:
        case OP_DIV_DOUBLE:
        case OP_REM_DOUBLE:
            dcv.visitBinopStmt(opcode - (OP_ADD_DOUBLE - OP_ADD), a, b, c, TYPE_DOUBLE);
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void x5c(int opcode, int a, int c, int d, int e, int f, int g, int b) {
        int args[];
        switch (a) {
        case 0:
            args = new int[0];
            break;
        case 1:
            args = new int[] { c };
            break;
        case 2:
            args = new int[] { c, d };
            break;
        case 3:
            args = new int[] { c, d, e };
            break;
        case 4:
            args = new int[] { c, d, e, f };
            break;
        case 5:
            args = new int[] { c, d, e, f, g };
            break;
        default:
            throw new RuntimeException("");
        }
        switch (opcode) {
        case OP_FILLED_NEW_ARRAY:
            dcv.visitFilledNewArrayStmt(opcode, args, dex.getType(b));
            break;
        case OP_INVOKE_VIRTUAL:
        case OP_INVOKE_SUPER:
        case OP_INVOKE_DIRECT:
        case OP_INVOKE_STATIC:
        case OP_INVOKE_INTERFACE:
        case OP_INVOKE_DIRECT_EMPTY:
            Method m = dex.getMethod(b);
            if (OP_INVOKE_DIRECT_EMPTY == opcode) {
                int[] nArgs;
                try {
                    nArgs = reBuildArgs(OP_INVOKE_DIRECT, args, m);
                } catch (Exception ex) {
                    throw new DexException(ex, "while rebuild argements for 0xF0 OP_INVOKE_DIRECT_EMPTY @0x%04x,"
                            + " this is typically because of a wrong apiLevel. current apiLevel is %d.", this.offset,
                            dex.apiLevel);
                }
                dcv.visitMethodStmt(OP_INVOKE_DIRECT, nArgs, m);
            } else {
                int[] nArgs = reBuildArgs(opcode, args, m);
                dcv.visitMethodStmt(opcode, nArgs, m);
            }
            break;
        default:
            throw new RuntimeException("");
        }
    }

    public void xrc(final int opcode, int a, int b, int c) {
        int args[] = new int[a];
        for (int i = 0; i < a; i++) {
            args[i] = c + i;
        }
        switch (opcode) {
        case OP_FILLED_NEW_ARRAY_RANGE:
        case OP_FILLED_NEW_ARRAY_JUMBO:
            dcv.visitFilledNewArrayStmt(OP_FILLED_NEW_ARRAY, args, dex.getType(b));
            break;
        case OP_INVOKE_VIRTUAL_RANGE:
        case OP_INVOKE_SUPER_RANGE:
        case OP_INVOKE_DIRECT_RANGE:
        case OP_INVOKE_STATIC_RANGE:
        case OP_INVOKE_INTERFACE_RANGE:
        case OP_INVOKE_VIRTUAL_JUMBO:
        case OP_INVOKE_SUPER_JUMBO:
        case OP_INVOKE_DIRECT_JUMBO:
        case OP_INVOKE_STATIC_JUMBO:
        case OP_INVOKE_INTERFACE_JUMBO:
        case OP_INVOKE_OBJECT_INIT_RANGE:
        case OP_INVOKE_OBJECT_INIT_JUMBO:
            int nOpcode;
            if (opcode == OP_INVOKE_OBJECT_INIT_RANGE || opcode == OP_INVOKE_OBJECT_INIT_JUMBO) {
                nOpcode = OP_INVOKE_DIRECT;
            } else {
                nOpcode = opcode
                        - (((opcode >> 4 == 0xFF) ? OP_INVOKE_VIRTUAL_JUMBO : OP_INVOKE_VIRTUAL_RANGE) - OP_INVOKE_VIRTUAL);
            }
            Method m = dex.getMethod(b);
            if (opcode == OP_INVOKE_OBJECT_INIT_RANGE) {// print more detail error message for 0xF0
                int[] nArgs;
                try {
                    nArgs = reBuildArgs(nOpcode, args, m);
                } catch (Exception ex) {
                    throw new DexException(ex, "while rebuild argements for 0xF0 OP_INVOKE_OBJECT_INIT_RANGE @0x%04x,"
                            + " this is typically because of a wrong apiLevel. current apiLevel is %d.", this.offset,
                            dex.apiLevel);
                }
                dcv.visitMethodStmt(nOpcode, nArgs, m);
            } else {
                int[] nArgs = reBuildArgs(nOpcode, args, m);
                dcv.visitMethodStmt(nOpcode, nArgs, m);
            }
            break;
        default:
            throw new RuntimeException("");
        }
    }

    private int[] reBuildArgs(int opcode, int[] args, Method m) {
        int realSize = m.getParameterTypes().length + (opcode == OP_INVOKE_STATIC ? 0 : 1);
        if (realSize != args.length) {// there are some double or float in args
            int[] nArgs = new int[realSize];
            int i = 0;
            int j = 0;
            if (opcode != OP_INVOKE_STATIC) {
                nArgs[i++] = args[j++];
            }
            for (String t : m.getParameterTypes()) {
                nArgs[i++] = args[j];
                j += "J".equals(t) || "D".equals(t) ? 2 : 1;
            }
            return nArgs;
        } else {
            return args;
        }
    }

    public void x0bc(int opcode, int a, int b) {
        switch (opcode) {
        case OP_THROW_VERIFICATION_ERROR:

            Object ref;
            switch (a >> 6) {
            case 0:// type;
                ref = dex.getType(b);
                break;
            case 1:// field;
                ref = dex.getField(b);
                break;
            case 2:// method;
                ref = dex.getMethod(b);
                break;
            default:
                throw new RuntimeException();
            }
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitReturnStmt(opcode, a & 0x3F, ref);
            }
            break;
        }
    }

    public void x2cs(int opcode, int a, int b, int c) {
        switch (opcode) {
        case OP_IGET_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IGET_QUICK, a, b, c, TYPE_SINGLE);
            }
            break;
        case OP_IGET_WIDE_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IGET_QUICK, a, b, c, TYPE_WIDE);
            }
            break;
        case OP_IGET_OBJECT_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IGET_QUICK, a, b, c, TYPE_OBJECT);
            }
            break;
        case OP_IPUT_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IPUT_QUICK, a, b, c, TYPE_SINGLE);
            }
            break;
        case OP_IPUT_WIDE_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IPUT_QUICK, a, b, c, TYPE_WIDE);
            }
            break;
        case OP_IPUT_OBJECT_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitFieldStmt(OP_IPUT_QUICK, a, b, c, TYPE_OBJECT);
            }
            break;
        }
    }

    public void x5mi(int opcode, int a, int c, int d, int e, int f, int g, int b) {
        int args[];
        switch (a) {
        case 0:
            args = new int[0];
            break;
        case 1:
            args = new int[] { c };
            break;
        case 2:
            args = new int[] { c, d };
            break;
        case 3:
            args = new int[] { c, d, e };
            break;
        case 4:
            args = new int[] { c, d, e, f };
            break;
        case 5:
            args = new int[] { c, d, e, f, g };
            break;
        default:
            throw new RuntimeException("");
        }
        switch (opcode) {
        case OP_EXECUTE_INLINE:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitMethodStmt(opcode, args, b);
            }
            break;
        }
    }

    public void x5ms(int opcode, int a, int c, int d, int e, int f, int g, int b) {
        int args[];
        switch (a) {
        case 0:
            args = new int[0];
            break;
        case 1:
            args = new int[] { c };
            break;
        case 2:
            args = new int[] { c, d };
            break;
        case 3:
            args = new int[] { c, d, e };
            break;
        case 4:
            args = new int[] { c, d, e, f };
            break;
        case 5:
            args = new int[] { c, d, e, f, g };
            break;
        default:
            throw new RuntimeException("");
        }
        switch (opcode) {
        case OP_INVOKE_VIRTUAL_QUICK:
        case OP_INVOKE_SUPER_QUICK:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitMethodStmt(opcode, args, b);
            }
            break;
        }
    }

    public void xrms(int opcode, int a, int b, int c) {
        int args[] = new int[a];
        for (int i = 0; i < a; i++) {
            args[i] = c + i;
        }
        switch (opcode) {
        case OP_INVOKE_VIRTUAL_QUICK_RANGE:
        case OP_INVOKE_SUPER_QUICK_RANGE:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitMethodStmt(opcode - (OP_INVOKE_SUPER_QUICK_RANGE - OP_INVOKE_SUPER_QUICK),
                        args, b);
            }
            break;
        }
    }

    public void xrmi(int opcode, int a, int b, int c) {
        int args[] = new int[a];
        for (int i = 0; i < a; i++) {
            args[i] = c + i;
        }
        switch (opcode) {
        case OP_EXECUTE_INLINE_RANGE:
            if (dcv instanceof OdexCodeVisitor) {
                ((OdexCodeVisitor) dcv).visitMethodStmt(OP_EXECUTE_INLINE, args, b);
            }
            break;
        }
    }

    public void x0sc(int opcode, int a, int b) {
        // TODO Auto-generated method stub

    }

}
