package tech.tresearchgroup.palila.controller;

import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.HttpRequest;
import io.activej.serializer.BinarySerializer;
import j2html.tags.DomContent;
import jdk.jshell.spi.ExecutionControl;
import tech.tresearchgroup.palila.controller.cache.local.GenericLocalCAO;
import tech.tresearchgroup.palila.controller.cache.local.GenericLocalPageCAO;
import tech.tresearchgroup.palila.controller.components.AutoCompleteDropDownBoxComponent;
import tech.tresearchgroup.palila.controller.components.DatePickerComponent;
import tech.tresearchgroup.palila.controller.components.EditableFieldComponent;
import tech.tresearchgroup.palila.controller.components.EditableScrollingComponent;
import tech.tresearchgroup.palila.controller.database.GenericDAO;
import tech.tresearchgroup.palila.controller.search.GenericSAO;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicFormObject;
import tech.tresearchgroup.palila.model.Card;
import tech.tresearchgroup.palila.model.SecurityLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BaseController extends BasicController {
    protected final GenericDAO genericDAO;
    protected final GenericLocalCAO genericLocalCAO;
    protected final GenericSAO genericSAO;
    protected final GenericLocalPageCAO genericPageCAO = new GenericLocalPageCAO();
    protected final GenericDAO loggingDAO;
    protected final Gson gson;
    protected final Class theClass;
    protected final BinarySerializer serializer;
    protected final Index index;
    protected final int REINDEX_BATCH_SIZE;
    protected final String SEARCH_COLUMN;
    protected final HikariDataSource hikariDataSource;
    protected final byte[] sample;
    String simpleName;

    public BaseController(HikariDataSource hikariDataSource,
                          Gson gson,
                          Client client,
                          Class theClass,
                          BinarySerializer serializer,
                          int reindexSize,
                          String searchColumn,
                          Object sample) throws Exception {
        this.hikariDataSource = hikariDataSource;
        this.genericDAO = new GenericDAO(hikariDataSource, theClass);
        this.loggingDAO = new GenericDAO(hikariDataSource, SecurityLog.class);
        this.genericLocalCAO = new GenericLocalCAO();
        this.genericSAO = new GenericSAO(gson);
        this.gson = gson;
        this.theClass = theClass;
        this.simpleName = theClass.getSimpleName().toLowerCase();
        this.serializer = serializer;
        this.REINDEX_BATCH_SIZE = reindexSize;
        this.SEARCH_COLUMN = searchColumn;
        this.index = client.index(theClass.getSimpleName());
        this.sample = CompressionController.compress(gson.toJson(sample).getBytes());
    }

    public Object getFromForm(HttpRequest httpRequest) throws InvocationTargetException, InstantiationException, IllegalAccessException, ExecutionControl.NotImplementedException {
        Field[] fields = theClass.getDeclaredFields();
        BasicFormObject newObject = (BasicFormObject) ReflectionMethods.getNewInstance(theClass);
        for (Field field : fields) {
            Class fieldClass = field.getType();
            String data = httpRequest.getPostParameter(simpleName + "-" + field.getName());
            if (data != null) {
                Method setter = ReflectionMethods.getSetter(field, theClass, fieldClass);
                if (Date.class.equals(fieldClass)) {
                    setter.invoke(newObject, java.sql.Date.valueOf(data));
                } else if (Long.class.equals(fieldClass)) {
                    setter.invoke(newObject, Long.parseLong(data));
                } else if (Integer.class.equals(fieldClass)) {
                    setter.invoke(newObject, Integer.parseInt(data));
                } else if (String.class.equals(fieldClass)) {
                    setter.invoke(newObject, data);
                } else if (Float.class.equals(fieldClass)) {
                    setter.invoke(newObject, Float.valueOf(data));
                } else if (Byte.class.equals(fieldClass)) {
                    setter.invoke(newObject, Byte.valueOf(data));
                } else if (Character.class.equals(fieldClass)) {
                    throw new ExecutionControl.NotImplementedException("Character object parsing from form");
                } else if (Double.class.equals(fieldClass)) {
                    setter.invoke(newObject, Double.valueOf(data));
                } else if (long.class.equals(fieldClass)) {
                    setter.invoke(newObject, Long.valueOf(data));
                } else if (int.class.equals(fieldClass)) {
                    setter.invoke(newObject, Integer.parseInt(data));
                } else if (float.class.equals(fieldClass)) {
                    setter.invoke(newObject, Float.valueOf(data));
                } else if (byte.class.equals(fieldClass)) {
                    setter.invoke(newObject, Byte.valueOf(data));
                } else if (char.class.equals(fieldClass)) {
                    throw new ExecutionControl.NotImplementedException("char object parsing from form");
                } else if (boolean.class.equals(fieldClass)) {
                    setter.invoke(newObject, Boolean.valueOf(data));
                } else if (double.class.equals(fieldClass)) {
                    setter.invoke(newObject, Double.valueOf(data));
                } else if (field.getType().isEnum()) {

                } else if (field.getType().isArray()) {
                    if (BaseSettings.debug) {
                        //System.out.println("Array: " + field.getType());
                    }
                } else if (field.getType().isInterface()) {
                    if (BaseSettings.debug) {
                        //System.out.println("Interface: " + field.getType());
                    }
                } else {
                    if (BaseSettings.debug) {
                        //System.out.println("Object: " + field.getType());
                    }
                }
            }
        }
        return newObject;
    }

    public List<DomContent> toForm(boolean editable, Object object) throws InvocationTargetException, IllegalAccessException, ExecutionControl.NotImplementedException, NoSuchMethodException, InstantiationException {
        Field[] fields = theClass.getDeclaredFields();
        List<DomContent> contentList = new LinkedList<>();
        List<DomContent> sideScrollers = new LinkedList<>();
        for (Field field : fields) {
            Class fieldClass = field.getType();
            Method getter = ReflectionMethods.getGetter(field, theClass);
            Object getterData = null;
            if(object != null) {
                getterData = getter.invoke(object);
            }
            if(Date.class.equals(fieldClass)) {
                if(field.getName().equals("created") || field.getName().equals("updated")) {
                    contentList.add(DatePickerComponent.render(false, field.getName(), String.valueOf(getterData), simpleName + "-" + field.getName()));
                } else {
                    contentList.add(DatePickerComponent.render(editable, field.getName(), String.valueOf(getterData), simpleName + "-" + field.getName()));
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
                if(field.getName().equals("id")) {
                    contentList.add(EditableFieldComponent.render(false, field.getName(), String.valueOf(getterData), simpleName + "-" + field.getName()));
                } else {
                    contentList.add(EditableFieldComponent.render(editable, field.getName(), String.valueOf(getterData), simpleName + "-" + field.getName()));
                }
            } else if (Character.class.equals(fieldClass) || char.class.equals(fieldClass)) {
                throw new ExecutionControl.NotImplementedException("Character object parsing from form");
            } else if (field.getType().isEnum()) {
                List<String> enumValues = ReflectionMethods.getEnumValues(fieldClass);
                contentList.add(AutoCompleteDropDownBoxComponent.render(editable, field.getName(), simpleName + "-" + field.getName(), String.valueOf(getterData), enumValues));
            } else if (field.getType().isArray()) {
                if (BaseSettings.debug) {
                    System.out.println("Array: " + field.getName());
                }
            } else if (field.getType().isInterface()) {
                List<BasicFormObject> formObjects = (List<BasicFormObject>) getterData;
                List<Card> cards = new LinkedList<>();
                if(formObjects != null) {
                    for (BasicFormObject basicFormObject : formObjects) {
                        cards.add(basicFormObject.toCard());
                    }
                }
                sideScrollers.add(EditableScrollingComponent.render(editable, field.getName(), cards, "/add/person", BaseSettings.cardWidth));
            } else {
                if (BaseSettings.debug) {
                    System.out.println("Object: " + field.getName());
                }
            }
        }
        contentList.addAll(sideScrollers);
        return contentList;
    }
}
