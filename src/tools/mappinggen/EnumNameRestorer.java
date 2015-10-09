package tools.mappinggen;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKESPECIAL;
import com.sun.org.apache.bcel.internal.generic.Instruction;
import com.sun.org.apache.bcel.internal.generic.LDC;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.bcel.internal.generic.PUTSTATIC;

import tools.Tool;
import tools.utils.MappingUtils;
import tools.utils.RootLevelJarIterate;
import tools.utils.Utils;

public class EnumNameRestorer implements Tool {

	private String[] args;
	public EnumNameRestorer(String args[]) {
		this.args = args;
	}

	@Override
	public void run() {
		String jarfilename = args[0];
		String mappingsfilename = args[1];
		try (PrintWriter writer = new PrintWriter(mappingsfilename); JarFile file = new JarFile(jarfilename)) {
			for (JarEntry entry : new RootLevelJarIterate(file)) {
				String original = Utils.stripClassEnding(entry.getName());
				JavaClass clazz = new ClassParser(file.getInputStream(entry), original).parse();
				if (isEnumClass(clazz)) {
					Method staticInit = getCLInit(clazz);
					//skip enums with no static init method
					if (staticInit == null) {
						continue;
					}
					ConstantPoolGen cpGen = new ClassGen(clazz).getConstantPool();
					MethodGen methodGen = new MethodGen(staticInit, clazz.getClassName(), cpGen);
					Iterator<Instruction> instrIter = Arrays.asList(methodGen.getInstructionList().getInstructions()).iterator();
					while (instrIter.hasNext()) {
						//first goes NEW
						Instruction instr = instrIter.next();
						if (!(instr instanceof NEW)) {
							break;
						}
						//but it may actually be another new, so we check if it is for enum constant
						if (!((NEW) instr).getLoadClassType(cpGen).getClassName().equals(clazz.getClassName())) {
							break;
						}
						//then goes dup, skip it
						instrIter.next();
						//LDC with our real enum name
						String realName = (String) ((LDC) instrIter.next()).getValue(cpGen);
						//now skip everything, until we reach invokespecial with <init> for this enum field
						while (true) {
							Instruction nextInstr = instrIter.next();
							if (nextInstr instanceof INVOKESPECIAL) {
								INVOKESPECIAL ispecial = ((INVOKESPECIAL) nextInstr);
								if (ispecial.getMethodName(cpGen).equals("<init>") && (ispecial.getClassName(cpGen).equals(clazz.getClassName()))) {
									break;
								}
							}
						}
						//next is putstatic with our obufscated field name
						PUTSTATIC putstatic = (PUTSTATIC) instrIter.next();
						String obfName = putstatic.getFieldName(cpGen);
						//now print the mapping
						writer.println(MappingUtils.createSRG(clazz.getClassName(), obfName, realName));
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Method getCLInit(JavaClass clazz) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals("<clinit>")) {
				return method;
			}
		}
		return null;
	}

	private boolean isEnumClass(JavaClass clazz) {
		return ((clazz.getModifiers() & 0x00004000) != 0);
	}

}
