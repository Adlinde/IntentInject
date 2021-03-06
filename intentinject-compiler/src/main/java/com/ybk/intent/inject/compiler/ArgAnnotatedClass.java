package com.ybk.intent.inject.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;


public class ArgAnnotatedClass {

    public TypeElement mClassElement;
    public List<ArgExtraField> extras;
    public List<ArgExtraArrayStringField> extrasArrayStr;
    public List<ArgExtraArrayIntField> extrasArrayInt;
    public List<ArgExtraArrayParcelableField> extrasArrayPar;
    public Elements mElementUtils;

    public ArgAnnotatedClass(TypeElement classElement, Elements elementUtils) {
        this.mClassElement = classElement;
        this.extras = new ArrayList<>();
        this.extrasArrayStr = new ArrayList<>();
        this.extrasArrayInt = new ArrayList<>();
        this.extrasArrayPar = new ArrayList<>();
        this.mElementUtils = elementUtils;
    }

    public String getFullClassName() {
        return mClassElement.getQualifiedName().toString();
    }

    public void addField(ArgExtraField field) {
        extras.add(field);
    }

    public void addField(ArgExtraArrayStringField field) {
        extrasArrayStr.add(field);
    }

    public void addField(ArgExtraArrayIntField field) {
        extrasArrayInt.add(field);
    }

    public void addField(ArgExtraArrayParcelableField field) {
        extrasArrayPar.add(field);
    }

    /**
     * @return 生成 xxx_Builder
     */
    public JavaFile generateExtras() {
        List<MethodSpec.Builder> methods = new ArrayList<>();
        String packageName = mElementUtils.getPackageOf(mClassElement).getQualifiedName().toString();
        //inject
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mClassElement.asType()), "host", Modifier.FINAL);
        injectMethodBuilder.addCode(
                "$T bundle=host.getArguments();\n" +
                        "if(bundle != null){\n", TypeUtil.BUNDLE);
        for (ArgExtraField field : extras) {
            injectMethodBuilder.addCode("\tif(bundle.containsKey($S)) ", field.getKey());
            injectMethodBuilder.addCode("host.$N = ($T)bundle.get($S);\n", field.getFieldName(), TypeName.get(field.getFieldType()), field.getKey());
        }
        for (ArgExtraArrayStringField field : extrasArrayStr) {
            injectMethodBuilder.addCode("\tif(bundle.containsKey($S)) ", field.getKey());
            injectMethodBuilder.addCode("host.$N = ($T)bundle.get($S);\n", field.getFieldName(), TypeName.get(field.getFieldType()), field.getKey());
        }
        for (ArgExtraArrayIntField field : extrasArrayInt) {
            injectMethodBuilder.addCode("\tif(bundle.containsKey($S)) ", field.getKey());
            injectMethodBuilder.addCode("host.$N = ($T)bundle.get($S);\n", field.getFieldName(), TypeName.get(field.getFieldType()), field.getKey());
        }
        for (ArgExtraArrayParcelableField field : extrasArrayPar) {
            injectMethodBuilder.addCode("\tif(bundle.containsKey($S)) ", field.getKey());
            injectMethodBuilder.addCode("host.$N = ($T)bundle.get($S);\n", field.getFieldName(), TypeName.get(field.getFieldType()), field.getKey());
        }
        injectMethodBuilder.addCode("}\n");

        //builder
        MethodSpec.Builder builder = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", "Builder"))
                .addStatement("return new Builder(new $T())", mClassElement.asType());

        //Builder
        MethodSpec.Builder injectConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(mClassElement.asType()), "fragment")
                .addStatement("super(fragment)");
        methods.add(injectConstructor);

        //extras
        for (ArgExtraField field : extras) {
            MethodSpec.Builder key = MethodSpec.methodBuilder(String.valueOf(field.getFieldName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(field.getFieldType()), String.valueOf(field.getFieldName()))
                    .returns(ClassName.get("", "Builder"))
                    .addStatement("super.extra($S,$L)", field.getFieldName().toString(), field.getFieldName().toString())
                    .addStatement("return this");
            methods.add(key);
        }
        for (ArgExtraArrayStringField field : extrasArrayStr) {
            MethodSpec.Builder key = MethodSpec.methodBuilder(String.valueOf(field.getFieldName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(field.getFieldType()), String.valueOf(field.getFieldName()))
                    .returns(ClassName.get("", "Builder"))
                    .addStatement("super.putStringArrayList($S,$L)", field.getFieldName().toString(), field.getFieldName().toString())
                    .addStatement("return this");
            methods.add(key);
        }
        for (ArgExtraArrayIntField field : extrasArrayInt) {
            MethodSpec.Builder key = MethodSpec.methodBuilder(String.valueOf(field.getFieldName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(field.getFieldType()), String.valueOf(field.getFieldName()))
                    .returns(ClassName.get("", "Builder"))
                    .addStatement("super.putIntegerArrayList($S,$L)", field.getFieldName().toString(), field.getFieldName().toString())
                    .addStatement("return this");
            methods.add(key);
        }
        for (ArgExtraArrayParcelableField field : extrasArrayPar) {
            MethodSpec.Builder key = MethodSpec.methodBuilder(String.valueOf(field.getFieldName()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(field.getFieldType()), String.valueOf(field.getFieldName()))
                    .returns(ClassName.get("", "Builder"))
                    .addStatement("super.putParcelableArrayList($S,$L)", field.getFieldName().toString(), field.getFieldName().toString())
                    .addStatement("return this");
            methods.add(key);
        }

        //Builder inner class
        TypeSpec.Builder inner = TypeSpec.classBuilder("Builder")
                .superclass(ParameterizedTypeName.get(TypeUtil.FRAGMENT_BUILDER, TypeName.get(mClassElement.asType())))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (MethodSpec.Builder method : methods) {
            inner.addMethod(method.build());
        }

        //outter class
        TypeSpec.Builder outter = TypeSpec.classBuilder(mClassElement.getSimpleName() + "_Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.INJECT, TypeName.get(mClassElement.asType())))
                .addType(inner.build());//add inner class
        outter.addMethod(injectMethodBuilder.build());
        outter.addMethod(builder.build());

        TypeSpec finderClass = outter.build();
        return JavaFile.builder(packageName, finderClass).build();
    }
}
