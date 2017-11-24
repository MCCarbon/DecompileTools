package tools.renamers;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PUTSTATIC;

import tools.EntryPoint;
import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.MappingUtils;
import tools.utils.Utils;

public class EnumConstNameRestorer implements Tool {

	@Override
	public void run() {
		String args[] = EntryPoint.getArgs();
		String inputJarFileName = args[0];
		String outputSrgMappingsFileName = args[1];
		try (
			PrintWriter outputSrgMappingWriter = new PrintWriter(outputSrgMappingsFileName);
			JarFile inputJarFile = new JarFile(inputJarFileName)
		) {
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
					continue;
				}
				String original = Utils.stripClassEnding(jarEntry.getName());
				JavaClass clazz = new ClassParser(inputJarFile.getInputStream(jarEntry), original).parse();
				if (clazz.isEnum()) {
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
						outputSrgMappingWriter.println(MappingUtils.createSRG(clazz.getClassName(), obfName, realName));
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

}
