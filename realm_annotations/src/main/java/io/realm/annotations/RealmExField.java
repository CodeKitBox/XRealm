/*
 * Copyright 2018 Realm Inc.
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
package io.realm.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.realm.ex.InitDefVal;

/**
 * Annotation used on fields in Realm model classes. It describes metadata about the field.
 * 注解 value  name 都是 修改内部列名 ，name的优先级大于 value
 * 注解 defValue 是列的默认值
 * 注意： 注解 RealmField 和  RealmExField 不能共存，RealmField 的优先级大于 RealmExField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RealmExField {

    /**
     * Manually set the internal name used by Realm for this field. This will override any
     *  set on the class or the module.
     *
     *  for more information about what setting the name means.
     * @see #name()
     */
    String value() default "";

    /**
     * Manually set the internal name used by Realm for this field. This will override any
     * set on the class or the module.
     *
     * @see for more information about what setting the name means.
     */
    String name() default "";

    /**
     * 列的默认值，在数据库表迁徙的时候，会修改原先存在的数据
     * @return
     */
     Class<?> defValue();
}
