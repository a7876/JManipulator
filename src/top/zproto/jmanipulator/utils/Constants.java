package top.zproto.jmanipulator.utils;

import jdk.internal.org.objectweb.asm.Opcodes;

public interface Constants extends Opcodes{
    int VERSION = Opcodes.V1_8;
    int ASM_API = Opcodes.ASM5;
    String SYNTHETIC_CLASS = "$JM$CLASS$";
    String SYNTHETIC_FIELD = "$JM$FIELD$";
}
