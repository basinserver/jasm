
const fs = require('fs');

const spec = JSON.parse(fs.readFileSync(process.argv[2], 'UTF-8'));

const typeMap = {
    varref: 'Local',
    constref: 'Constant',
    offset: 'Label',
    uint8: 'int',
    byte: 'int',
    short: 'int',
    atype: 'AType',
    lit0: null,
    arg_count: 'int',
}

const readWidth = {
    1: 'in.readByte()',
    2: 'in.readShort()',
    4: 'in.readInt()'
}

const readUWidth = {
    1: 'in.readUnsignedByte()',
    2: 'in.readUnsignedShort()',
    4: 'in.readInt()'
}

const readMap = {
    varref: s => `method.getOrMakeLocal(${readUWidth[s]})`,
    constref: s => `constants.get(${readUWidth[s]})`,
    offset: s => `labelMaker.apply((int) (${readWidth[s]}) + pc)`,
    uint8: 'in.readUnsignedByte()',
    byte: null,
    short: null,
    atype: 'AType.from(in.readUnsignedByte())',
    lit0: null,
    arg_count: null,
}


const writeWidth = {
    1: x => `out.write(${x})`,
    2: x => `out.writeShort(${x})`,
    4: x => `out.writeInt(${x})`,
}

const writeMap = {
    varref: x => `${x}.index`,
    constref: x => `constantIndexer.apply(${x})`,
    offset: x => `labelIndexer.apply(${x}) - pc`,
    uint8: x => `((byte) ${x})`,
    byte: null,
    short: null,
    atype: x => `(${x}).index`,
    lit0: null,
    arg_count: null,
}

const customPushes = {
    invokeinterface: '(((Method) this.indexbyte.value).descriptor.returnType == JType.voidT ? 0 : 1)',
    invokestatic: '(((Method) this.indexbyte.value).descriptor.returnType == JType.voidT ? 0 : 1)',
    invokevirtual: '(((Method) this.indexbyte.value).descriptor.returnType == JType.voidT ? 0 : 1)',
    invokedynamic: '(((Method) this.indexbyte.value).descriptor.returnType == JType.voidT ? 0 : 1)',
    invokespecial: '(((Method) this.indexbyte.value).descriptor.returnType == JType.voidT ? 0 : 1)',
}

const customPops = {
    invokeinterface: '1 + ((Method) this.indexbyte.value).descriptor.parameters.size()',
    invokestatic: '((Method) this.indexbyte.value).descriptor.parameters.size()',
    invokevirtual: '1 + ((Method) this.indexbyte.value).descriptor.parameters.size()',
    invokedynamic: '0 /* unsupported */',
    invokespecial: '1 + ((Method) this.indexbyte.value).descriptor.parameters.size()',
};

let classes = {};

const upperFirst = (str) => str.slice(0, 1).toUpperCase() + str.slice(1);

let opcodeTable = [];
opcodeTable[171] = 'Lookupswitch';
opcodeTable[170] = 'Tableswitch';
opcodeTable[196] = 'Wide';

let formList = {};

Object.keys(spec).forEach(name => {
        let ins = spec[name];
        if (ins.manual) {
            return;
        }
        ins.args.forEach(x => {
            if (x.name === 'const') {
                x.name = 'const_';
            } else if (x.name === 'byte') {
               x.name = 'byte_';
           }
        });
        ins.branching = false;
        if (ins.args.filter(x => x.name === 'branchbyte').length > 0) {
            ins.branching = true;
        }
        for (let form of ins.forms) {
            opcodeTable[parseInt(form.opcode)] = upperFirst(form.name);
            form.instruction = ins;
            formList[upperFirst(form.name)] = form;
            classes[upperFirst(form.name)] = `
package com.protryon.jasm.instruction.instructions;

import com.protryon.jasm.instruction.Instruction;
import com.protryon.jasm.Local;
import com.protryon.jasm.Constant;
import com.protryon.jasm.AType;
import com.protryon.jasm.JType;
import com.protryon.jasm.Method;
import com.protryon.jasm.instruction.psuedoinstructions.Label;
import com.shapesecurity.functional.F;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class ${upperFirst(form.name)} extends Instruction {

    ${ins.args.map(arg => {
        let jtype = typeMap[arg.type];
        if (jtype) {
            return `public ${jtype} ${arg.name};\n`
        }
        return '';
    }).join('')}
    @Override
    public String name() {
        return "${form.name}";
    }

    @Override
    public int opcode() {
        return ${form.opcode};
    }

    @Override
    public void read(boolean wide, ArrayList<Constant> constants, Method method, F<Integer, Label> labelMaker, int pc, DataInputStream in) throws IOException {
        ${ins.args.map(arg => {
            let mapped = readMap[arg.type];
            if (mapped != null && typeof mapped === 'string') {
                return `this.${arg.name} = ${mapped};\n`;
            } else if (arg.type === 'lit0') {
                return `${readUWidth[arg.width]};\n`
            } else {
                let remapped = mapped || (s => readWidth[s]);
                if (ins.wideable) {
                    return `
                    if (wide) {
                        this.${arg.name} = ${remapped(arg.wwidth)};
                    } else {
                        this.${arg.name} = ${remapped(arg.width)};
                    }\n`
                } else {
                    return `this.${arg.name} = ${remapped(arg.width)};\n`
                }
            }

        }).join('\n')}
    }

    @Override
    public void write(boolean wide, DataOutputStream out, F<Label, Integer> labelIndexer, F<Constant, Integer> constantIndexer, int pc) throws IOException {
        ${ins.args.map(arg => {
            let mapped = writeMap[arg.type];
            if (mapped != null && typeof mapped === 'string') {
                return `${mapped(`this.${arg.name}`)};\n`;
            } else if (arg.type === 'lit0') {
                return `${writeWidth[arg.width](0)};\n`
            } else {
                let remapped = mapped || (x => x);
                if (ins.wideable) {
                    return `
                    if (wide) {
                        ${writeWidth[arg.wwidth](remapped(`this.${arg.name}`))};
                    } else {
                        ${writeWidth[arg.width](remapped(`this.${arg.name}`))};
                    }\n`
                } else {
                    return `${writeWidth[arg.width](remapped(`this.${arg.name}`))};\n`;
                }
            }
        }).join('\n')}
    }

    @Override
    public int pushes() {
${name in customPushes ? `return ${customPushes[name]};` :
        `${ins.pushed.map(x => '        // ' + x).join('\n')}
        return ${ins.pushed.length};`}
    }

    @Override
    public int pops() {
${name in customPops ? `return ${customPops[name]};` :
        `${ins.popped.map(x => '        // ' + x).join('\n')}
        return ${ins.popped.length};`}
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.name());
        ${ins.args.map(arg => {
            if (arg.type === 'lit0') return '';
            return `builder.append(" ").append(this.${arg.name});`;
        }).join('\n')}
        return builder.toString();
    }

    @Override
    public Instruction fromString(String str) {
        return null;
    }
    
    @Override
    public boolean isControl() {
        return ${ins.branching ? 'true' : 'false'};
    }

}
        `;
    }
});

for (let name in classes) {
    fs.writeFileSync(process.argv[3] + '/instructions/' + name + '.java', classes[name]);
}

const table = `
package com.protryon.jasm.instruction;

import com.protryon.jasm.instruction.instructions.*;

import java.util.function.Supplier;

public final class OpcodeTable {

    private OpcodeTable() {

    }

    public static Supplier<Instruction>[] suppliers = new Supplier[] {
${opcodeTable.map(x => "        " + x + "::new,").join('\n')}
    };

}
`
fs.writeFileSync(process.argv[3] + '/OpcodeTable.java', table);


const tupleTs = {
    0: 'void',
    1: 'T',
    2: 'Pair<T, T>',
    3: 'Tuple3<T, T, T>',
};

const tupleApplications = {
    0: x => '',
    1: x => `stack = stack.cons(${x});`,
    2: x => `stack = stack.cons(${x}.left).cons(${x}.right);`,
    3: x => `stack = stack.cons(${x}.a).cons(${x}.b).cons(${x}.c);`,
};

const alwaysManual = ['Multianewarray', 'Athrow', 'Dup_x2', 'Dup2', 'Dup2_x1', 'Dup2_x2', 'Pop2'];

const reducer = `
package com.protryon.jasm.instruction;

import com.protryon.jasm.instruction.instructions.*;
import com.protryon.jasm.*;
import com.shapesecurity.functional.*;

public abstract class StackReducer<T> extends ManualStackReducer<T> {

${Object.keys(formList).map(name => {
    let form = formList[name];
    let ins = form.instruction;
    if (typeof ins.popped == 'string' || alwaysManual.includes(name)) {
        return '';
    }
    return `    public abstract ${tupleTs[ins.pushed.length]} reduce${name}(${name} instruction${ins.popped.map(arg => ', T ' + arg).filter(a => a.length > 0).join('')});\n`;
}).filter(x => x.length > 0).join('\n')}

}
`
fs.writeFileSync(process.argv[3] + '/StackReducer.java', reducer);

const director = `
package com.protryon.jasm.instruction;

import java.util.LinkedList;
import com.protryon.jasm.instruction.instructions.*;
import com.shapesecurity.functional.*;
import com.shapesecurity.functional.data.ImmutableList;

public final class StackDirector {

    private StackDirector() {

    }

    public static <T> ImmutableList<T> reduceInstructions(StackReducer<T> reducer, Iterable<Instruction> code, ImmutableList<T> stackPrefix, F<T, Boolean> isDoubled) {
        ImmutableList<T> stack = stackPrefix;
        for (Instruction i : code) {
            switch (i.opcode()) {
${Object.keys(formList).sort((form1, form2) => formList[form1].opcode - formList[form2].opcode).map(name => {
    let form = formList[name];
    let ins = form.instruction;
    if (typeof ins.popped == 'string' || alwaysManual.includes(name)) {
        return '';
    }
return `            case ${form.opcode}: {
${ins.popped.slice(0).reverse().map(x => `                T ${x} = stack.maybeHead().fromJust();\n                stack = stack.maybeTail().fromJust();`).join('\n')}
                ${ins.pushed.length === 0 ? '' : tupleTs[ins.pushed.length] + ' pushed = '}reducer.reduce${name}((${name}) i${ins.popped.map(arg => ', ' + arg).join('')});
                ${tupleApplications[ins.pushed.length]('pushed')}
                break;
            }`;
}).filter(x => x.length > 0).join('\n')}
            default:
                stack = ManualStackDirector.reduceInstruction(reducer, i, stack, isDoubled);
            }
        }
        return stack;
    }
}
`

fs.writeFileSync(process.argv[3] + '/StackDirector.java', director);