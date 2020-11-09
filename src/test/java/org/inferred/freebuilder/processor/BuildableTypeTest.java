package org.inferred.freebuilder.processor;

import static org.inferred.freebuilder.processor.BuildableType.maybeBuilder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.inferred.freebuilder.FreeBuilder;
import org.inferred.freebuilder.processor.source.testing.ModelRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Optional;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

@RunWith(JUnit4.class)
public class BuildableTypeTest {

  @Rule public final ModelRule model = new ModelRule();

  @Test
  public void simpleFreebuilderAnnotatedType() {
    TypeElement type = model.newType(
        "package com.example;",
        "@" + FreeBuilder.class.getName(),
        "public interface DataType {",
        "  int value();",
        "  class Builder extends DataType_Builder { }",
        "}");
    Optional<DeclaredType> result = maybeBuilder(
        (DeclaredType) type.asType(), model.elementUtils(), model.typeUtils());
    assertTrue(result.isPresent());
  }

  @Test
  public void freebuilderAnnotatedTypeWithHiddenBuildMethod() {
    TypeElement type = model.newType(
        "package com.example;",
        "@" + FreeBuilder.class.getName(),
        "public interface DataType {",
        "  int value();",
        "  class Builder extends DataType_Builder {",
        "    protected DataType build();",
        "  }",
        "}");
    Optional<DeclaredType> result = maybeBuilder(
        (DeclaredType) type.asType(), model.elementUtils(), model.typeUtils());
    assertFalse(result.isPresent());
  }
}
