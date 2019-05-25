
const fs = require('fs');

const spec = JSON.parse(fs.readFileSync(process.argv[2], 'UTF-8'));

const typeMap = {
    varref: 'Int',
    constref: 'Constant<*>',
    offset: 'Label',
    uint8: 'Int',
    byte: 'Int',
    short: 'Int',
    atype: 'AType',
    lit0: null,
    arg_count: 'Int',
};

const readWidth = {
    1: 'inputStream.readByte().toInt()',
    2: 'inputStream.readShort().toInt()',
    4: 'inputStream.readInt()'
};

const readUWidth = {
    1: 'inputStream.readUnsignedByte()',
    2: 'inputStream.readUnsignedShort()',
    4: 'inputStream.readInt()'
};

const readMap = {
    varref: s => `${readUWidth[s]}`,
    constref: s => `constants.get(${readUWidth[s]})`,
    offset: s => `labelMaker.invoke((${readWidth[s]}) + pc)`,
    uint8: 'inputStream.readUnsignedByte()',
    byte: null,
    short: null,
    atype: 'AType.from(inputStream.readUnsignedByte())',
    lit0: null,
    arg_count: null,
};


const writeWidth = {
    1: x => `out.write(${x})`,
    2: x => `out.writeShort(${x})`,
    4: x => `out.writeInt(${x})`,
};

const writeMap = {
    varref: x => `${x}`,
    constref: x => `constantIndexer.invoke(${x})`,
    offset: x => `labelIndexer.invoke(${x}) - pc`,
    uint8: x => `(${x})`,
    byte: null,
    short: null,
    atype: x => `(${x}).index`,
    lit0: null,
    arg_count: null,
};

const customPushes = {
    invokeinterface: 'if (((this.indexbyte!!.value as Method).descriptor.returnType == JType.voidT)) 0 else 1',
    invokestatic: 'if (((this.indexbyte!!.value as Method).descriptor.returnType == JType.voidT)) 0 else 1',
    invokevirtual: 'if (((this.indexbyte!!.value as Method).descriptor.returnType == JType.voidT)) 0 else 1',
    invokedynamic: 'if (((this.indexbyte!!.value as Method).descriptor.returnType == JType.voidT)) 0 else 1',
    invokespecial: 'if (((this.indexbyte!!.value as Method).descriptor.returnType == JType.voidT)) 0 else 1',
};

const customPops = {
    invokeinterface: '1 + (this.indexbyte!!.value as Method).descriptor.parameters.size',
    invokestatic: '(this.indexbyte!!.value as Method).descriptor.parameters.size',
    invokevirtual: '1 + (this.indexbyte!!.value as Method).descriptor.parameters.size',
    invokedynamic: '0 /* unsupported */',
    invokespecial: '1 + (this.indexbyte!!.value as Method).descriptor.parameters.size',
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
package com.protryon.jasm.instruction.instructions

import com.protryon.jasm.instruction.Instruction
import com.protryon.jasm.Local
import com.protryon.jasm.Constant
import com.protryon.jasm.AType
import com.protryon.jasm.JType
import com.protryon.jasm.Method
import com.protryon.jasm.instruction.psuedoinstructions.Label

import java.io.IOException
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.ArrayList

class ${upperFirst(form.name)} : Instruction() {

    ${ins.args.map(arg => {
        let jtype = typeMap[arg.type];
        if (jtype) {
            return `var ${arg.name} : ${jtype}? = null\n`
        }
        return '';
    }).join('')}
    override val isControl: Boolean
        get() = ${ins.branching ? 'true' : 'false'}
    
    override fun name(): String {
        return "${form.name}"
    }

    override fun opcode(): Int {
        return ${form.opcode};
    }

    @Throws(IOException::class)
    override fun read(wide: Boolean, constants: ArrayList<Constant<*>>, method: Method, labelMaker: (Int)->Label, pc: Int, inputStream: DataInputStream) {
        ${ins.args.map(arg => {
            let mapped = readMap[arg.type];
            if (mapped != null && typeof mapped === 'string') {
                return `this.${arg.name} = ${mapped}\n`;
            } else if (arg.type === 'lit0') {
                return `${readUWidth[arg.width]}\n`;
            } else {
                let remapped = mapped || (s => readWidth[s]);
                if (ins.wideable) {
                    return `
                    this.${arg.name} = if (wide) {
                        ${remapped(arg.wwidth)}
                    } else {
                        ${remapped(arg.width)}
                    }\n`
                } else {
                    return `this.${arg.name} = ${remapped(arg.width)}\n`
                }
            }

        }).join('\n')}
    }

    @Throws(IOException::class)
    override fun write(wide: Boolean, out: DataOutputStream, labelIndexer: (Label)->Int, constantIndexer: (Constant<*>)->Int, pc: Int) {
        ${ins.args.map(arg => {
            let mapped = writeMap[arg.type];
            if (mapped != null && typeof mapped === 'string') {
                return `${mapped(`this.${arg.name}!!`)}\n`;
            } else if (arg.type === 'lit0') {
                return `${writeWidth[arg.width](0)}\n`
            } else {
                let remapped = mapped || (x => x);
                if (ins.wideable) {
                    return `
                    if (wide) {
                        ${writeWidth[arg.wwidth](remapped(`this.${arg.name}!!`))}
                    } else {
                        ${writeWidth[arg.width](remapped(`this.${arg.name}!!`))}
                    }\n`
                } else {
                    return `${writeWidth[arg.width](remapped(`this.${arg.name}!!`))}\n`;
                }
            }
        }).join('\n')}
    }

    override fun pushes(): Int {
${name in customPushes ? `return ${customPushes[name]}` :
        `${ins.pushed.map(x => '        // ' + x).join('\n')}
        return ${ins.pushed.length}`}
    }

    override fun pops(): Int {
${name in customPops ? `return ${customPops[name]}` :
        `${ins.popped.map(x => '        // ' + x).join('\n')}
        return ${ins.popped.length}`}
    }

    override fun toString(): String {
        val builder = StringBuilder(this.name())
        ${ins.args.map(arg => {
            if (arg.type === 'lit0') return '';
            return `builder.append(" ").append(this.${arg.name})`;
        }).join('\n')}
        return builder.toString()
    }

    override fun fromString(str: String): Instruction {
        error("unsupported")
    }
    
}
        `;
    }
});

for (let name in classes) {
    fs.writeFileSync(process.argv[3] + '/instructions/' + name + '.kt', classes[name]);
}

const table = `
package com.protryon.jasm.instruction

import com.protryon.jasm.instruction.instructions.*

import java.util.function.Supplier

object OpcodeTable {

    var suppliers = arrayOf<()->Instruction>(
${opcodeTable.map(x => "        ::" + x + ",").join('\n').replace(/,$/, '')}
    )

}
`;
fs.writeFileSync(process.argv[3] + '/OpcodeTable.kt', table);


const tupleTs = {
    0: '',
    1: ': T',
    2: ': Pair<T, T>',
    3: ': Triple<T, T, T>',
};

const tupleApplications = {
    0: x => '',
    1: x => `stack = stack.cons(${x})`,
    2: x => `stack = stack.cons(${x}.first).cons(${x}.second)`,
    3: x => `stack = stack.cons(${x}.first).cons(${x}.second).cons(${x}.third)`,
};

const alwaysManual = ['Multianewarray', 'Athrow', 'Dup_x2', 'Dup2', 'Dup2_x1', 'Dup2_x2', 'Pop2'];

const reducer = `
package com.protryon.jasm.instruction

import com.protryon.jasm.instruction.instructions.*
import com.protryon.jasm.*

abstract class StackReducer<T> : ManualStackReducer<T>() {

${Object.keys(formList).map(name => {
    let form = formList[name];
    let ins = form.instruction;
    if (typeof ins.popped == 'string' || alwaysManual.includes(name)) {
        return '';
    }
    //abstract fun reduceAaload(instruction: Aaload, arrayref: T, index: T): T
    return `    abstract fun reduce${name}(instruction: ${name}${ins.popped.map(arg => ', ' + arg + ': T').filter(a => a.length > 0).join('')})${tupleTs[ins.pushed.length]}\n`;
}).filter(x => x.length > 0).join('\n')}

}
`;
fs.writeFileSync(process.argv[3] + '/StackReducer.kt', reducer);

const director = `
package com.protryon.jasm.instruction

import java.util.LinkedList
import com.protryon.jasm.instruction.instructions.*
import com.shapesecurity.functional.data.ImmutableList

object StackDirector {

    fun <T> reduceInstructions(reducer: StackReducer<T>, code: Iterable<Instruction>, stackPrefix: ImmutableList<T>, isDoubled: (T)->Boolean): ImmutableList<T> {
        var stack = stackPrefix
        for (i in code) {
            when (i.opcode()) {
${Object.keys(formList).sort((form1, form2) => formList[form1].opcode - formList[form2].opcode).map(name => {
    let form = formList[name];
    let ins = form.instruction;
    if (typeof ins.popped == 'string' || alwaysManual.includes(name)) {
        return '';
    }
return `            ${form.opcode} -> {
${ins.popped.slice(0).reverse().map(x => `                val ${x} = stack.maybeHead().fromJust()\n                stack = stack.maybeTail().fromJust()`).join('\n')}
                ${ins.pushed.length === 0 ? '' : 'val pushed = '}reducer.reduce${name}(i as ${name}${ins.popped.map(arg => ', ' + arg).join('')})
                ${tupleApplications[ins.pushed.length]('pushed')}
            }`;
}).filter(x => x.length > 0).join('\n')}
            else ->
                stack = ManualStackDirector.reduceInstruction(reducer, i, stack, isDoubled)
            }
        }
        return stack;
    }
}
`;

fs.writeFileSync(process.argv[3] + '/StackDirector.kt', director);