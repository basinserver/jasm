package com.protryon.jasm;

import com.shapesecurity.functional.data.HashTable;
import com.shapesecurity.functional.data.Maybe;

public class LocalContext {

    public HashTable<Integer, Local> locals = HashTable.emptyUsingEquality();

    public Local getOrMakeLocal(int index) {
        Maybe<Local> localMaybe = locals.get(index);
        if (localMaybe.isJust()) {
            return localMaybe.fromJust();
        }
        Local newLocal = new Local(index);
        this.locals = this.locals.put(index, newLocal);
        return newLocal;
    }

    public Local getOrMakeLocal(int index, JType type) {
        Maybe<Local> localMaybe = locals.get(index);
        if (localMaybe.isJust()) {
            if (type != null && (localMaybe.fromJust().type == null || !type.assignableTo(localMaybe.fromJust().type))) {
                return this.updateLocal(localMaybe.fromJust().resetType(type));
            }
            return localMaybe.fromJust();
        }
        Local newLocal = new Local(index, type);
        this.locals = this.locals.put(index, newLocal);
        return newLocal;
    }

    public Local updateLocal(Local local) {
        this.locals = this.locals.put(local.index, local);
        return local;
    }

    public LocalContext fork() {
        LocalContext localContext = new LocalContext();
        localContext.locals = this.locals;
        return localContext;
    }

}
