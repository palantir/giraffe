/**
 * Copyright 2015 Palantir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.giraffe.file.base.attribute;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotated method gets or sets a named attribute of a file. Getter methods
 * must take no arguments and return some object. Setter methods must take a
 * single argument; any return value is ignored.
 * <p>
 * This annotation takes a single optional argument, the attribute name. If
 * omitted, the annotated method's name is used. For getter methods, the method
 * name is used directly. For setter methods, any leading "set" is removed and
 * the new first letter converted to lower case.
 *
 * @author bkeyes
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
    /**
     * Returns the name of the attribute the annotated method gets or sets. If
     * the returned name is empty, the name of the annotated method is used.
     */
    String value() default "";
}
