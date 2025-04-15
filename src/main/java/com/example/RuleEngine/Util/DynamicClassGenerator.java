package com.example.RuleEngine.Util;

import javassist.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class DynamicClassGenerator {

//    public static Object createDynamicPersonClass(String className, Map<String, Object> fields) throws Exception {
//        // Create a new class using Javassist
//        ClassPool pool = ClassPool.getDefault();
//        CtClass dynamicClass = pool.makeClass(className);
//
//        // Add fields to the class
//        for (Map.Entry<String, Object> entry : fields.entrySet()) {
//            String fieldName = entry.getKey();
//            Object fieldValue = entry.getValue();
//
//            // Determine the field type
//            CtClass fieldType;
//            if (fieldValue instanceof Integer) {
//                fieldType = CtClass.intType;
//            } else if (fieldValue instanceof Double) {
//                fieldType = CtClass.doubleType;
//            } else if (fieldValue instanceof Boolean) {
//                fieldType = CtClass.booleanType;
//            } else {
//                fieldType = pool.get("java.lang.String");
//            }
//
//            // Add the field to the class
//            CtField field = new CtField(fieldType, fieldName, dynamicClass);
//            field.setModifiers(Modifier.PUBLIC);
//            dynamicClass.addField(field);
//        }
//
//        // Create the class
//        Class<?> generatedClass = dynamicClass.toClass();
//
//        // Create an instance of the class
//        Object instance = generatedClass.getDeclaredConstructor().newInstance();
//
//        // Set the field values
//        for (Map.Entry<String, Object> entry : fields.entrySet()) {
//            String fieldName = entry.getKey();
//            Object fieldValue = entry.getValue();
//
//            generatedClass.getField(fieldName).set(instance, fieldValue);
//        }
//
//        return instance;
//    }

//    public static Object createDynamicPersonClass(String className, Map<String, Object> fields) throws Exception {
//        // Create a dynamic class using ByteBuddy
//        DynamicType.Builder<Object> builder = new ByteBuddy().subclass(Object.class).name(className);
//
//        // Add fields to the class
//        for (Map.Entry<String, Object> entry : fields.entrySet()) {
//            String fieldName = entry.getKey().replaceAll("\\." , "_");
//            Object fieldValue = entry.getValue();
//
//            // Add a field, a getter method, and a setter method for each field
//            builder = builder
//                    .defineField(fieldName, fieldValue.getClass(), net.bytebuddy.description.modifier.Visibility.PUBLIC)
//                    .defineMethod("get" + capitalize(fieldName), fieldValue.getClass(), net.bytebuddy.description.modifier.Visibility.PUBLIC)
//                    .intercept(FixedValue.value(fieldValue))
//                    .defineMethod("set" + capitalize(fieldName), void.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
//                    .withParameter(fieldValue.getClass())
//                    .intercept(net.bytebuddy.implementation.MethodDelegation.to(new Object() {
//                        public void set(Object value) throws Exception {
//                            // Dynamically set the field value
//                            java.lang.reflect.Field field = this.getClass().getField(fieldName);
//                            field.set(this, value);
//                        }
//                    }));
//        }
//
//        // Build and load the class
//        Class<?> dynamicClass = builder.make()
//                .load(DynamicClassGenerator.class.getClassLoader())
//                .getLoaded();
//
//        // Create an instance of the class
//        return dynamicClass.getDeclaredConstructor().newInstance();
//    }
//
//    private static String capitalize(String str) {
//        if (str == null || str.isEmpty()) {
//            return str;
//        }
//        return str.substring(0, 1).toUpperCase() + str.substring(1);
//    }

    public static Class<?> createDynamicPersonClass(String className, Map<String, Object> fields) throws Exception {
        // Create a dynamic class using ByteBuddy
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .name(className);

        // Define fields and their accessors
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldName = entry.getKey().replaceAll("\\.", "_");

            builder = builder
                    .defineField(fieldName, String.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .defineMethod("get" + capitalize(fieldName), String.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .intercept(net.bytebuddy.implementation.FieldAccessor.ofField(fieldName))
                    .defineMethod("set" + capitalize(fieldName), void.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .withParameter(String.class)
                    .intercept(net.bytebuddy.implementation.FieldAccessor.ofField(fieldName));
        }

        // Build and load the class
        Class<?> dynamicClass = builder.make()
                .load(DynamicClassGenerator.class.getClassLoader())
                .getLoaded();
        return dynamicClass;

        // Create an instance of the class
      /*  Object instance = dynamicClass.getDeclaredConstructor().newInstance();

        // Set field values
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                String fieldName = entry.getKey().replaceAll("\\.", "_");
                Object fieldValue = entry.getValue();

                Field field = dynamicClass.getField(fieldName);
                field.set(instance, fieldValue != null ? fieldValue.toString() : null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field value: " + entry.getKey(), e);
            }
        }

        return instance;*/
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }


    public static Object[] addFieldAndSetValue(Class<?> personClass, Object person, String fieldName, Object fieldValue) throws Exception {
        try {
            // Check if the field already exists
            Field field = personClass.getField(fieldName);
            field.set(person, fieldValue != null ? fieldValue.toString() : null);
        } catch (NoSuchFieldException e) {
            // Dynamically create a new class with the additional field
            DynamicType.Builder<?> builder = new ByteBuddy()
                    .subclass(personClass)
                    .defineField(fieldName, String.class, Visibility.PUBLIC);

            // Load the new class
            Class<?> modifiedClass = builder.make()
                    .load(personClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent())
                    .getLoaded();

            // Create a new instance of the modified class
            Object newPerson = modifiedClass.getDeclaredConstructor().newInstance();

            // Copy existing fields to the new instance
            for (Field existingField : personClass.getFields()) {
                existingField.set(newPerson, existingField.get(person));
            }

            // Set the new field value
            Field newField = modifiedClass.getField(fieldName);
            newField.set(newPerson, fieldValue != null ? fieldValue.toString() : null);

            // Return the updated class and object
            return new Object[]{modifiedClass, newPerson};
        }
        // Return the original class and object if no modification was needed
        return new Object[]{personClass, person};
    }

    public static Class<?> createDynamicPersonClassUsingSetOfKeys(String className, Set<String> attributes) throws Exception {
        // Create a dynamic class using ByteBuddy
        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .name(className);

        // Define fields and their accessors
        for (String attribute : attributes) {
            String fieldName = attribute.replace(".", "_");

            builder = builder
                    .defineField(fieldName, String.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .defineMethod("get" + capitalize(fieldName), String.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .intercept(net.bytebuddy.implementation.FieldAccessor.ofField(fieldName))
                    .defineMethod("set" + capitalize(fieldName), void.class, net.bytebuddy.description.modifier.Visibility.PUBLIC)
                    .withParameter(String.class)
                    .intercept(net.bytebuddy.implementation.FieldAccessor.ofField(fieldName));
        }

        // Build and load the class
        Class<?> dynamicClass = builder.make()
                .load(DynamicClassGenerator.class.getClassLoader())
                .getLoaded();
        return dynamicClass;
    }

}