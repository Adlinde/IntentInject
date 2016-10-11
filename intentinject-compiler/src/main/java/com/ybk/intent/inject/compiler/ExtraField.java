package com.ybk.intent.inject.compiler;

import com.ybk.intent.inject.compiler.annotation.Extra;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class ExtraField {
    private VariableElement mFieldElement;
    private String key;

    public ExtraField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(String.format("Only fields can be annotated with @%s", Extra.class.getSimpleName()));
        }

        mFieldElement = (VariableElement) element;
        Extra extra = mFieldElement.getAnnotation(Extra.class);
        key = extra.value();

//        if (mResId == null) {
//            throw new IllegalArgumentException(String.format("value() in %s for field %s is not valid !", BindView.class.getSimpleName(), mFieldElement.getSimpleName()));
//        }
    }

    public Name getFieldName() {
        return mFieldElement.getSimpleName();
    }

    public String getKey() {
        return key;
    }

    public TypeMirror getFieldType() {
        return mFieldElement.asType();
    }
}
