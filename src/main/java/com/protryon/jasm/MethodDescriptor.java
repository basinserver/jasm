package com.protryon.jasm;

import com.shapesecurity.functional.Pair;

import java.util.ArrayList;

public class MethodDescriptor {

    public JType returnType;
    public ArrayList<JType> parameters;

    public MethodDescriptor(JType returnType, ArrayList<JType> parameters) {
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public static MethodDescriptor fromString(Classpath classpath, String str) {
        if (str.charAt(0) != '(') {
            return null;
        }
        int end = str.indexOf(")", 1);
        String params = str.substring(1, end);
        String returnDescriptor = str.substring(end + 1);
        JType returnType = JType.fromDescriptor(classpath, returnDescriptor);
        ArrayList<JType> parameters = new ArrayList<>();
        while (params.length() > 0) {
            Pair<JType, Integer> pair = JType.fromDescriptorWithLength(classpath, params);
            params = params.substring(pair.right);
            parameters.add(pair.left);
        }
        return new MethodDescriptor(returnType, parameters);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        for (JType param : parameters) {
            builder.append(param.toDescriptor());
        }
        builder.append(")");
        builder.append(returnType.toDescriptor());
        return builder.toString();
    }

    public String niceString(String methodName) {
        StringBuilder builder = new StringBuilder();
        builder.append(returnType.niceName);
        builder.append(" ").append(methodName).append("(");
        boolean first = true;
        for (JType param : parameters) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(param.niceName);
        }
        builder.append(")");
        return builder.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof MethodDescriptor)) {
            return false;
        }
        if (!((MethodDescriptor) o).returnType.equals(this.returnType)) {
            return false;
        }
        if (((MethodDescriptor) o).parameters.size() != this.parameters.size()) {
            return false;
        }
        for (int i = 0; i < this.parameters.size(); ++i) {
            if (!this.parameters.get(i).equals(((MethodDescriptor) o).parameters.get(i))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return this.returnType.hashCode() + this.parameters.hashCode();
    }

}
