/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inferred.freebuilder.processor.util;

import static com.google.common.collect.Iterables.getOnlyElement;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Manages the imports for a source file, and produces short type references by adding extra
 * imports when possible.
 *
 * <p>To ensure we never import common names like 'Builder', nested classes are never directly
 * imported. This is necessarily less readable when types are used as namespaces, e.g. in proto2.
 */
class ImportManager {

  private final SetMultimap<String, QualifiedName> visibleSimpleNames = HashMultimap.create();
  private final ImmutableSet<String> implicitImports = ImmutableSet.of();
  private final Set<String> explicitImports = new TreeSet<String>();

  public Set<String> getClassImports() {
    return Collections.unmodifiableSet(explicitImports);
  }

  public void appendShortened(Appendable a, QualifiedName type) throws IOException {
    appendPackageForTopLevelClass(a, type.getPackage(), type.getSimpleNames().get(0));
    String prefix = "";
    for (String simpleName : type.getSimpleNames()) {
      a.append(prefix).append(simpleName);
      prefix = ".";
    }
  }

  public Optional<QualifiedName> lookup(String shortenedType) {
    String[] simpleNames = shortenedType.split("\\.");
    Set<QualifiedName> possibilities = visibleSimpleNames.get(simpleNames[0]);
    if (possibilities.size() != 1) {
      return Optional.absent();
    }
    QualifiedName result = getOnlyElement(possibilities);
    for (int i = 1; i < simpleNames.length; i++) {
      result = result.nestedType(simpleNames[i]);
    }
    return Optional.of(result);
  }

  private void appendPackageForTopLevelClass(Appendable a, String pkg, CharSequence name)
      throws IOException {
    String qualifiedName = pkg + "." + name;
    if (implicitImports.contains(qualifiedName) || explicitImports.contains(qualifiedName)) {
      // Append nothing
    } else if (visibleSimpleNames.containsKey(name.toString())) {
      a.append(pkg).append(".");
    } else {
      visibleSimpleNames.put(name.toString(), QualifiedName.of(pkg, name.toString()));
      explicitImports.add(qualifiedName);
      // Append nothing
    }
  }
}
