# DecompileTools

This repository offers tools that help on decompiling Minecraft.

<<<<<<< HEAD


## Usage 📖

1. Create SRG mappings for obfuscated enum names
EnumConstantsRestore {jarfile} {output}

2. Create SRG mappings to rename all classes at root level to the following format class_{class}, for inner: class_{class}_in_class_{outer}
ClassesRename {jarfile} {output}

3. Create finder entries for usage with classfinder. Will scan every class ConstantPool, and then will create a mapping that allows to identify this class among the others.
ClassFinderGenerate {jarfile} {entrynamestart} {output}

4. Apply finder entries to jar to generate SRG names based on found classes
ClassFinderFind {jarfile} {finderentriesfile} {output}

## Tools 🛠️

We have some tools:

- CFR. *A java decompiler*.
- FabricMC Enigma. *A java deobfuscator & compiler, I do not recommend it because it crashes*.
- J-Decompiler. *A java decompiler, some times doesn't work at all*.
- SpecialSource. *A java deobfuscator, I recommend it*.
