package com.protryon.jasm;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.protryon.jasm.decompiler.StackEntry;

public final class Local {

    public final JType type;
    public final int index;

    public Local(int index) {
        this(index, null);
    }

    public Local(int index, JType type) {
        this.type = type;
        this.index = index;
    }

    public Local resetType(JType type) {
        if (this.type == null || !type.assignableTo(this.type)) {
            return new Local(index, type);
        }
        return this;
    }

    public StackEntry<Expression> stackify() {
        return new StackEntry<>(this.type, new NameExpr(this.toString()));
    }

    @Override
    public String toString() {
        return "v" + this.index;
    }

}
