var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

function initializeCoreMod() {
    return {
        'projectile_entity': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.projectile.ProjectileEntity',
                'methodName': 'func_70186_c', // shoot
                'methodDesc': '(DDDFF)V'
            },
            'transformer': function (shoot) {
               log("Found 'shoot' method. Adding nausea inaccuracy...")
               var instructions = shoot.instructions;
               var newInstructions = new InsnList();
               newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
               newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "aurilux/shrouds/common/CommonEventHandler",
                   "nauseaInaccuracy", "(Lnet/minecraft/entity/projectile/ProjectileEntity;)F", false));
               newInstructions.add(new VarInsnNode(Opcodes.FLOAD, 8));
               newInstructions.add(new InsnNode(Opcodes.FADD));
               newInstructions.add(new VarInsnNode(Opcodes.FSTORE, 8))
               instructions.insertBefore(instructions.getFirst(), newInstructions);
               log("Nausea inaccuracy added!")
               return shoot;
           }
        }
    }
}

function log(msg) {
    ASMAPI.log("DEBUG", "Shrouds: " + msg, []);
}