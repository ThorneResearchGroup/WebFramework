package tech.tresearchgroup.palila.controller;

import io.activej.http.HttpRequest;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import j2html.tags.DomContent;
import jdk.jshell.spi.ExecutionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tresearchgroup.cao.controller.GenericCAO;
import tech.tresearchgroup.cao.model.CacheTypesEnum;
import tech.tresearchgroup.palila.controller.components.*;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicFormObject;
import tech.tresearchgroup.palila.model.Card;

import java.lang.reflect.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ReflectionMethods {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionMethods.class);

    public static List<String> getNonDefaultFieldNames(Class theClass) {
        Field[] fields = theClass.getDeclaredFields();
        List<String> names = new LinkedList<>();
        for (Field field : fields) {
            if (isNotArray(field.getType()) && !isObject(field.getType())) {
                names.add(field.getName());
            }
        }
        return names;
    }

    public static boolean isNotArray(Class theClass) {
        return !theClass.isInterface() && !theClass.equals(List.class);
    }

    public static boolean isObject(Class theClass) {
        return !Date.class.equals(theClass) &&
            !Long.class.equals(theClass) &&
            !Integer.class.equals(theClass) &&
            !String.class.equals(theClass) &&
            !Float.class.equals(theClass) &&
            !Byte.class.equals(theClass) &&
            !Character.class.equals(theClass) &&
            !Double.class.equals(theClass) &&
            !long.class.equals(theClass) &&
            !int.class.equals(theClass) &&
            !float.class.equals(theClass) &&
            !byte.class.equals(theClass) &&
            !char.class.equals(theClass) &&
            !boolean.class.equals(theClass) &&
            !double.class.equals(theClass) &&
            !theClass.isEnum() &&
            !theClass.isArray() &&
            !theClass.isInterface();
    }

    public static Method getId(Class theClass) {
        try {
            return theClass.getMethod("getId");
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                logger.info("Failed to execute: getId on: " + theClass.getSimpleName());
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
                logger.info("Failed to execute: get" + cap + " on: " + theClass.getSimpleName());
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
                logger.info("Failed to execute: set" + cap + " on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static Method setId(Class theClass, Class parameterClass) {
        try {
            return theClass.getMethod("setId", parameterClass);
        } catch (NoSuchMethodException e) {
            if (BaseSettings.debug) {
                logger.info("Failed to execute: setId on: " + theClass.getSimpleName());
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
                logger.info("Failed to execute: valueOf on: " + theClass.getSimpleName());
            }
        }
        return null;
    }

    public static Class getListClass(Field field, String[] inPackages, GenericCAO genericCAO) {
        try {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            String[] parts = pt.getActualTypeArguments()[0].getTypeName().split("\\.");
            String mediaName = parts[parts.length - 1].toLowerCase();
            return findClass(mediaName, inPackages, genericCAO);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static List<String> getEnumValues(GenericCAO genericCAO, Class theClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        List<String> stringList = (List<String>) genericCAO.read(CacheTypesEnum.STATIC, theClass.getSimpleName());
        if (stringList != null) {
            return stringList;
        }
        stringList = new LinkedList<>();
        if (theClass != null) {
            Method getValues = theClass.getMethod("values");
            Object[] values = (Object[]) getValues.invoke(ReflectionMethods.getNewInstance(theClass));
            for (Object enumValue : values) {
                Method getName = enumValue.getClass().getMethod("name");
                stringList.add(StringController.toCamelCase(String.valueOf(getName.invoke(enumValue))));
            }
        }
        genericCAO.create(CacheTypesEnum.STATIC, theClass.getSimpleName(), stringList);
        return stringList;
    }

    public static Object getNewInstance(Class theClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor[] constructors = theClass.getConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
        }
        return null;
    }

    public static Class findClass(String className, String[] inPackages, GenericCAO genericCAO) throws ClassNotFoundException {
        for (String packageName : inPackages) {
            List<String> classNames = (List<String>) genericCAO.read(CacheTypesEnum.STATIC, packageName);
            if (classNames == null) {
                classNames = new LinkedList<>();
                try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).enableClassInfo().scan()) {
                    classNames.addAll(scanResult.getAllClasses().getNames());
                }
                genericCAO.create(CacheTypesEnum.STATIC, packageName, classNames);
            }
            for (String name : classNames) {
                if (name.replace(packageName + ".", "").toLowerCase().equals(className)) {
                    return Class.forName(name);
                }
            }
        }
        return null;
    }

    public static List<String> getClassNames(String[] inPackages, GenericCAO genericCAO) {
        List<String> theClasses = new LinkedList<>();
        for (String packageName : inPackages) {
            List<String> classNames = (List<String>) genericCAO.read(CacheTypesEnum.STATIC, packageName);
            if (classNames == null) {
                classNames = new LinkedList<>();
                try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).enableClassInfo().scan()) {
                    classNames.addAll(scanResult.getAllClasses().getNames());
                }
                genericCAO.create(CacheTypesEnum.STATIC, packageName, classNames);
            }
            theClasses.addAll(classNames);
        }
        return theClasses;
    }

    public static Object getObjectFromForm(Class theClass, HttpRequest httpRequest) throws Exception {
        if (theClass != null) {
            Object object = ReflectionMethods.getNewInstance(theClass);
            Field[] fields = theClass.getDeclaredFields();
            for (Field field : fields) {
                String postField = httpRequest.getPostParameter(field.getName());
                if (postField != null) {
                    if (!postField.equals("")) {
                        if (field.getType().isInterface()) {
                            logger.info("Lists unsupported");
                        } else {
                            Method setter = ReflectionMethods.getSetter(field, theClass, field.getType());
                            setter.invoke(object, postField);
                        }
                    }
                }
            }
            return object;
        }
        return null;
    }

    public static List<DomContent> toFormObjects(boolean editable, Object object, Class theClass, GenericCAO genericCAO) throws InvocationTargetException, IllegalAccessException, ExecutionControl.NotImplementedException, NoSuchMethodException, InstantiationException {
        if (object == null) {
            List<DomContent> data = (List<DomContent>) genericCAO.read(CacheTypesEnum.DOM, "form-" + theClass.getSimpleName());
            if (data != null) {
                return data;
            }
        }
        Field[] fields = theClass.getDeclaredFields();
        List<DomContent> contentList = new LinkedList<>();
        List<DomContent> sideScrollers = new LinkedList<>();
        contentList.add(MediaTypeFieldComponent.render(theClass));
        for (Field field : fields) {
            Class fieldClass = field.getType();
            Method getter = ReflectionMethods.getGetter(field, theClass);
            Object getterData = null;
            if (object != null) {
                getterData = getter.invoke(object);
            }
            if (Date.class.equals(fieldClass)) {
                if (field.getName().equals("created") || field.getName().equals("updated")) {
                    contentList.add(DatePickerComponent.render(false, field.getName(), String.valueOf(getterData), field.getName()));
                } else {
                    contentList.add(DatePickerComponent.render(editable, field.getName(), String.valueOf(getterData), field.getName()));
                }
            } else if (String.class.equals(fieldClass) ||
                Long.class.equals(fieldClass) ||
                Integer.class.equals(fieldClass) ||
                Float.class.equals(fieldClass) ||
                Byte.class.equals(fieldClass) ||
                Double.class.equals(fieldClass) ||
                long.class.equals(fieldClass) ||
                int.class.equals(fieldClass) ||
                float.class.equals(fieldClass) ||
                byte.class.equals(fieldClass) ||
                boolean.class.equals(fieldClass) ||
                double.class.equals(fieldClass)) {
                if (field.getName().equals("id")) {
                    contentList.add(EditableFieldComponent.render(false, field.getName(), String.valueOf(getterData), field.getName()));
                } else {
                    contentList.add(EditableFieldComponent.render(editable, field.getName(), String.valueOf(getterData), field.getName()));
                }
            } else if (Character.class.equals(fieldClass) || char.class.equals(fieldClass)) {
                throw new ExecutionControl.NotImplementedException("Character object parsing from form");
            } else if (field.getType().isEnum()) {
                List<String> enumValues = ReflectionMethods.getEnumValues(genericCAO, fieldClass);
                contentList.add(AutoCompleteDropDownBoxComponent.render(editable, field.getName(), field.getName(), String.valueOf(getterData), enumValues));
            } else if (field.getType().isArray()) {
                if (BaseSettings.debug) {
                    logger.info("Array: " + field.getName());
                }
            } else if (field.getType().isInterface()) {
                if (object != null) {
                    List<BasicFormObject> formObjects = (List<BasicFormObject>) getterData;
                    List<Card> cards = new LinkedList<>();
                    if (formObjects != null) {
                        for (BasicFormObject basicFormObject : formObjects) {
                            cards.add(basicFormObject.toCard());
                        }
                    }
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    Type subType = pt.getActualTypeArguments()[0];
                    String[] subTypeSplit = subType.getTypeName().split("\\.");
                    String withoutPackage = subTypeSplit[subTypeSplit.length - 1].toLowerCase();
                    sideScrollers.add(EditableScrollingComponent.render(editable, field.getName(), cards, "/add/" + withoutPackage, BaseSettings.cardWidth));
                }
            } else {
                if (BaseSettings.debug) {
                    logger.info("Object: " + field.getName());
                }
            }
        }
        contentList.addAll(sideScrollers);
        if (object == null) {
            genericCAO.create(CacheTypesEnum.DOM, "form-" + theClass.getSimpleName(), contentList);
        }
        return contentList;
    }
}
