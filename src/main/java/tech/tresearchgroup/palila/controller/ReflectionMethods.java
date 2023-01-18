package tech.tresearchgroup.palila.controller;

import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicFormObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionMethods {
    public static List<String> getNonDefaultFieldNames(Class theClass) {
        Field[] fields = theClass.getDeclaredFields();
        Field[] defaultFields = BasicFormObject.class.getDeclaredFields();
        List<String> names = new LinkedList<>();
        for (Field field : fields) {
            boolean isDefault = false;
            for (Field defaultField : defaultFields) {
                System.out.println(field.getType());
                if (defaultField.getName().equals(field.getName()) && isNotObjectOrArray(field.getType())) {
                    isDefault = true;
                    break;
                }
            }
            if (!isDefault) {
                names.add(field.getName());
            }
        }
        return names;
    }

    public static boolean isNotObjectOrArray(Class theClass) {
        return Date.class.equals(theClass) ||
            Long.class.equals(theClass) ||
            Integer.class.equals(theClass) ||
            String.class.equals(theClass) ||
            Float.class.equals(theClass) ||
            Byte.class.equals(theClass) ||
            Character.class.equals(theClass) ||
            Double.class.equals(theClass) ||
            long.class.equals(theClass) ||
            int.class.equals(theClass) ||
            float.class.equals(theClass) ||
            byte.class.equals(theClass) ||
            char.class.equals(theClass) ||
            boolean.class.equals(theClass) ||
            double.class.equals(theClass) ||
            theClass.isEnum();
    }

    public static Method getId(Class theClass) {
        try {
            return theClass.getMethod("getId");
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                System.out.println("Failed to execute: getId on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static Method getGetter(Field field, Class theClass) {
        String cap = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            if (boolean.class.equals(field.getType())) {
                return theClass.getMethod("is" + cap);
            }
            return theClass.getMethod("get" + cap);
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                System.out.println("Failed to execute: get" + cap + " on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    /**
     * Gets the getter function of an objects attribute
     *
     * @param field          the fields
     * @param theClass       the class you're targeting
     * @param parameterClass the class of the parameter you're providing (e.g. if I pass a string, I used String.class)
     * @return the method
     * @throws NoSuchMethodException if the method doesn't exist
     */
    public static Method getSetter(Field field, Class theClass, Class parameterClass) {
        String cap = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        try {
            return theClass.getMethod("set" + cap, parameterClass);
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                System.out.println("Failed to execute: set" + cap + " on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static Method setId(Class theClass, Class parameterClass) {
        try {
            return theClass.getMethod("setId", parameterClass);
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                System.out.println("Failed to execute: setId on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static Object getValueOf(Class theClass, String data) throws InvocationTargetException, IllegalAccessException {
        try {
            if (Objects.equals(data, null)) {
                return null;
            }
            return theClass.getMethod("valueOf", String.class).invoke(theClass, data);
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                System.out.println("Failed to execute: valueOf on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static List<String> getEnumValues(Class theClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        List<String> stringList = new LinkedList<>();
        if(theClass != null) {
            Method getValues = theClass.getMethod("values");
            Object[] values = (Object[]) getValues.invoke(ReflectionMethods.getNewInstance(theClass));
            for (Object enumValue : values) {
                Method getName = enumValue.getClass().getMethod("name");
                stringList.add(StringController.toCamelCase(String.valueOf(getName.invoke(enumValue))));
            }
        }
        return stringList;
    }

    public static Object getNewInstance(Class theClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        try {
            return theClass.getConstructors()[0].newInstance();
        } catch (ArrayIndexOutOfBoundsException e) {
            if (BaseSettings.debug) {
                System.out.println(theClass.getSimpleName() + " does not have a constructor.");
            }
        }
        return null;
    }
}
