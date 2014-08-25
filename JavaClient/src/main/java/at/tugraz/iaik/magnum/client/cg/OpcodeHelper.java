/*******************************************************************************
 * Copyright 2013 Alexander Jesner, Bernd Prünster
 * Copyright 2013, 2014 Bernd Prünster
 *
 *     This file is part of Magnum PI.
 *
 *     Magnum PI is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Magnum PI is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Magnum PI.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.iaik.magnum.client.cg;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;



public final class OpcodeHelper {
  public static boolean isInvokeInstruction(Instruction inst) {
    Opcode opcode = inst.getOpcode();
    
    return 
        opcode.equals(Opcode.INVOKE_DIRECT) ||
        opcode.equals(Opcode.INVOKE_DIRECT_RANGE) ||
        opcode.equals(Opcode.INVOKE_INTERFACE) ||
        opcode.equals(Opcode.INVOKE_INTERFACE_RANGE) ||
        opcode.equals(Opcode.INVOKE_STATIC) ||
        opcode.equals(Opcode.INVOKE_STATIC_RANGE) ||
        opcode.equals(Opcode.INVOKE_SUPER) ||
        opcode.equals(Opcode.INVOKE_SUPER_RANGE) ||
        opcode.equals(Opcode.INVOKE_VIRTUAL) ||
        opcode.equals(Opcode.INVOKE_VIRTUAL_RANGE);  
  }
}
