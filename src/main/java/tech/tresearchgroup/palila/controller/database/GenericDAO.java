package tech.tresearchgroup.palila.controller.database;

import com.zaxxer.hikari.HikariDataSource;
import tech.tresearchgroup.palila.controller.ReflectionMethods;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.BasicObjectInterface;

import java.lang.reflect.*;
import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GenericDAO implements GenericDatabaseAccessObject {
    private final HikariDataSource hikariDataSource;

    public GenericDAO(HikariDataSource hikariDataSource, Class theClass) throws SQLException {
        this.hikariDataSource = hikariDataSource;
        if (!tableExists(theClass)) {
            if (!createSQLTables(theClass)) {
                System.out.println("Failed to create: " + theClass.getSimpleName().toLowerCase() + " tables!");
            }
        }
    }

    @Override
    public boolean create(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Connection connection = hikariDataSource.getConnection();
        Class<? extends Object> theClass = object.getClass();
        StringBuilder statementBuilder = new StringBuilder();
        Field[] fields = theClass.getDeclaredFields();
        statementBuilder.append("INSERT INTO `").append(theClass.getSimpleName().toLowerCase()).append("` VALUES (");
        for (int i = 0; i != fields.length; i++) {
            if (!fields[i].getType().toString().equals("interface java.util.List")) {
                statementBuilder.append("?");
                if (i != (fields.length - 1)) {
                    statementBuilder.append(", ");
                }
            }
        }
        statementBuilder.append(")");
        PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder.toString());
        addFields(fields, theClass, object, preparedStatement);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        boolean returnThis = preparedStatement.executeUpdate() != 0;
        connection.commit();
        connection.close();
        if (hasRelationships(fields)) {
            Object objectWithId = getObjectId(object);
            Method getId = ReflectionMethods.getId(theClass);
            Long id = (Long) getId.invoke(objectWithId);
            Method setId = ReflectionMethods.setId(theClass, Long.class);
            setId.invoke(object, id);
            createRelationships(fields, theClass, object);
        }
        return returnThis;
    }

    private boolean hasRelationships(Field[] fields) {
        for (Field field : fields) {
            if (field.getType().toString().equals("interface java.util.List")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object read(Long id, Class theClass) throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from " + theClass.getSimpleName().toLowerCase() + " WHERE `id` = " + id + ";");
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();
        if (resultSet.next()) {
            return getFromResultSet(resultSet, theClass.getConstructors()[0].newInstance());
        }
        return null;
    }

    @Override
    public List readAll(Class theClass, boolean full) throws SQLException {
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from " + theClass.getSimpleName().toLowerCase() + "");
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        List<BasicObjectInterface> returnThis = getAllFromResultSet(resultSet, theClass, full);
        connection.close();
        return returnThis;
    }

    @Override
    public List readPaginated(int resultCount, int page, Class theClass, boolean full) throws SQLException {
        String statement;
        String simpleName = theClass.getSimpleName().toLowerCase();
        if (page == 0) {
            if (resultCount == 0) {
                statement = "SELECT * FROM " + simpleName;
            } else {
                statement = "SELECT * FROM " + simpleName + " LIMIT " + resultCount;
            }
        } else {
            statement = "SELECT * FROM " + simpleName + " LIMIT " + resultCount + "," + page;
        }
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        return getAllFromResultSet(resultSet, theClass, full);
    }

    public List readMany(List<Long> ids, Class theClass, boolean full) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String simpleClassName = theClass.getSimpleName().toLowerCase();
        statement.append("SELECT * FROM ").append(simpleClassName).append(" WHERE ");
        for (int i = 0; i != ids.size(); i++) {
            statement.append("id = ").append(ids.get(i));
            if (i != (ids.size() - 1)) {
                statement.append(" OR ");
            }
        }
        statement.append(";");
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        return getAllFromResultSet(resultSet, theClass, full);
    }

    @Override
    public List readOrderedBy(int resultCount, int page, Class theClass, String orderBy, boolean ascending, boolean full) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String ascDesc = "DESC";
        if (ascending) {
            ascDesc = "ASC";
        }
        statement.append("SELECT * FROM ").append(theClass.getSimpleName().toLowerCase());
        if (orderBy != null && !orderBy.equals("none") && orderBy.length() > 0) {
            statement.append(" ORDER BY ").append(orderBy).append(" ").append(ascDesc);
        }
        if (page == 0) {
            if (resultCount != 0) {
                statement.append(" LIMIT ").append(resultCount);
            }
        } else {
            statement.append(" LIMIT ").append(resultCount).append(",").append(page * resultCount);
        }
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        return getAllFromResultSet(resultSet, theClass, full);
    }

    public ResultSet readManyOrderedBy(int resultCount, int page, List<Class> theClassList, List<String> orderByList, boolean ascending) throws SQLException {
        StringBuilder statement = new StringBuilder();
        String ascDesc = "DESC";
        if (ascending) {
            ascDesc = "ASC";
        }
        for (int classCounter = 0; classCounter != theClassList.size(); classCounter++) {
            Class theClass = theClassList.get(classCounter);
            String theClassSimpleName = theClass.getSimpleName().toLowerCase();
            for (int i = 0; i != orderByList.size(); i++) {
                String orderBy = orderByList.get(i);
                if (page == 0) {
                    if (resultCount == 0) {
                        statement
                            .append("(SELECT `id`, '")
                            .append(theClassSimpleName.toLowerCase())
                            .append("-")
                            .append(orderBy).append("' AS mediaType FROM ")
                            .append(theClassSimpleName)
                            .append(" ORDER BY ")
                            .append(orderBy)
                            .append(" ")
                            .append(ascDesc)
                            .append(")");
                    } else {
                        statement
                            .append("(SELECT `id`, '")
                            .append(theClassSimpleName.toLowerCase())
                            .append("-")
                            .append(orderBy).append("' AS mediaType FROM ")
                            .append(theClassSimpleName)
                            .append(" ORDER BY ")
                            .append(orderBy)
                            .append(" ")
                            .append(ascDesc)
                            .append(" LIMIT ")
                            .append(resultCount).append(")");
                    }
                } else {
                    statement
                        .append("(SELECT `id`, '")
                        .append(theClassSimpleName.toLowerCase())
                        .append("-")
                        .append(orderBy).append("' AS mediaType FROM ")
                        .append(theClassSimpleName)
                        .append(" ORDER BY ")
                        .append(orderBy)
                        .append(" ")
                        .append(ascDesc)
                        .append(" LIMIT ")
                        .append(resultCount)
                        .append(",")
                        .append(page * resultCount)
                        .append(")");
                }
                if (i != (orderByList.size() - 1)) {
                    statement.append(" UNION ALL ");
                }
            }
            if (classCounter != (theClassList.size() - 1)) {
                if (classCounter != (theClassList.size() - 1)) {
                    statement.append(" UNION ALL ");
                }
            }
        }
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(statement));
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        return resultSet;
    }

    @Override
    public boolean update(Object object) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Class<? extends Object> theClass = object.getClass();
        StringBuilder statementBuilder = new StringBuilder();
        Field[] fields = theClass.getDeclaredFields();
        statementBuilder.append("UPDATE ").append(theClass.getSimpleName().toLowerCase()).append(" SET ");
        for (int i = 0; i != fields.length; i++) {
            statementBuilder.append(fields[i].getName()).append(" = ?");
            if (i != (fields.length - 1)) {
                statementBuilder.append(", ");
            }
        }
        Method getId = ReflectionMethods.getId(object.getClass());
        Long id = (Long) getId.invoke(object);
        statementBuilder.append(" WHERE id = ").append(id);
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder.toString());
        addFields(fields, theClass, object, preparedStatement);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        boolean returnThis = preparedStatement.executeUpdate() == 0;
        connection.commit();
        connection.close();
        createRelationships(fields, theClass, object);
        return returnThis;
    }

    @Override
    public boolean delete(long id, Class theClass) throws SQLException {
        String statementBuilder = "DELETE FROM " + theClass.getSimpleName().toLowerCase() + " " +
            "WHERE " + "id" + "=?" +
            ")";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(statementBuilder);
        preparedStatement.setLong(1, id);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        boolean returnThis = preparedStatement.execute();
        connection.commit();
        connection.close();
        return returnThis;
    }

    @Override
    public Long getTotal(Class theClass) throws SQLException {
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) AS COUNT FROM " + theClass.getSimpleName().toLowerCase());
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        if (resultSet.next()) {
            Long returnThis = resultSet.getLong("COUNT");
            connection.close();
            return returnThis;
        }
        connection.close();
        return null;
    }

    @Override
    public Long getTotalPages(int maxResultsSize, Class theClass) throws SQLException {
        long total = getTotal(theClass);
        if (total != 0) {
            return total / maxResultsSize;
        }
        return 0L;
    }

    @Override
    public List databaseSearch(int maxResultsSize, String query, String returnColumn, String searchColumn, Class theClass) throws SQLException {
        String statement;
        if (returnColumn.equals("*")) {
            statement = "SELECT * FROM " + theClass.getSimpleName().toLowerCase() + " where " + searchColumn + " LIKE '%" + query + "%' LIMIT " + maxResultsSize;
        } else {
            statement = "SELECT " + returnColumn + " from " + theClass.getSimpleName().toLowerCase() + " where " + searchColumn + " LIKE '%" + query + "%' LIMIT " + maxResultsSize;
        }
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        return getAllFromResultSet(resultSet, theClass, false);
    }

    public void addFields(Field[] fields, Class theClass, Object object, PreparedStatement preparedStatement) throws InvocationTargetException, SQLException, IllegalAccessException {
        int id = 1;
        for (Field field : fields) {
            Class fieldClass = field.getType();
            Method method = ReflectionMethods.getGetter(field, theClass);
            Object fieldObject = method.invoke(object);
            if (fieldObject == null) {
                if (!field.getType().toString().equals("interface java.util.List")) {
                    preparedStatement.setNull(id, Types.NULL);
                }
            } else {
                if (field.getType().toString().equals("class java.util.Date")) {
                    Date date = (Date) fieldObject;
                    preparedStatement.setDate(id, new java.sql.Date(date.getTime()));
                } else if (Long.class.equals(fieldClass)) {
                    preparedStatement.setLong(id, (Long) fieldObject);
                } else if (Integer.class.equals(fieldClass)) {
                    preparedStatement.setInt(id, (Integer) fieldObject);
                } else if (field.getType().toString().equals("class java.lang.String")) {
                    preparedStatement.setString(id, (String) fieldObject);
                } else if (Float.class.equals(fieldClass)) {
                    preparedStatement.setFloat(id, (Float) fieldObject);
                } else if (Byte.class.equals(fieldClass)) {
                    preparedStatement.setByte(id, (Byte) fieldObject);
                } else if (Character.class.equals(fieldClass)) {
                    preparedStatement.setString(id, String.valueOf(fieldObject));
                } else if (Double.class.equals(fieldClass)) {
                    preparedStatement.setDouble(id, (Double) fieldObject);
                } else if (long.class.equals(fieldClass)) {
                    preparedStatement.setLong(id, (long) fieldObject);
                } else if (int.class.equals(fieldClass)) {
                    preparedStatement.setInt(id, (int) fieldObject);
                } else if (float.class.equals(fieldClass)) {
                    preparedStatement.setFloat(id, (Float) fieldObject);
                } else if (byte.class.equals(fieldClass)) {
                    preparedStatement.setByte(id, (byte) fieldObject);
                } else if (char.class.equals(fieldClass)) {
                    preparedStatement.setString(id, String.valueOf((char) fieldObject));
                } else if (boolean.class.equals(fieldClass)) {
                    preparedStatement.setBoolean(id, (boolean) fieldObject);
                } else if (double.class.equals(fieldClass)) {
                    preparedStatement.setDouble(id, (double) fieldObject);
                } else if (fieldClass.isEnum()) {
                    preparedStatement.setString(id, String.valueOf(fieldObject));
                } else {
                    preparedStatement.setNull(id, Types.NULL);
                }
            }
            if (!field.getType().toString().equals("interface java.util.List")) {
                id++;
            }
        }
    }

    private void createRelationships(Field[] fields, Class theClass, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException, NoSuchMethodException {
        boolean returnThis = true;
        for (Field field : fields) {
            Method method = ReflectionMethods.getGetter(field, theClass);
            if (field.getType().toString().equals("interface java.util.List")) {
                List list = (List) method.invoke(object);
                if (list != null) {
                    for (Object listObject : list) {
                        create(listObject);
                    }
                    List withIds = getObjectIds(list);
                    for (Object listObject : withIds) {
                        if (!createRelationship(object, listObject, field.getName())) {
                            returnThis = false;
                        }
                    }
                }
            }
        }
    }

    private boolean createRelationship(Object firstObject, Object secondObject, String secondObjectName) throws SQLException, InvocationTargetException, IllegalAccessException {
        String firstClassName = firstObject.getClass().getSimpleName().toLowerCase();
        String relationTableName = firstClassName + "_" + secondObjectName;
        if (tableExists(firstClassName + "_" + secondObjectName.toLowerCase())) {
            Method firstIdGetter = ReflectionMethods.getId(firstObject.getClass());
            Long firstId = (Long) firstIdGetter.invoke(firstObject);

            Method secondIdGetter = ReflectionMethods.getId(secondObject.getClass());
            Long secondId = (Long) secondIdGetter.invoke(secondObject);
            String stringBuilder = "INSERT INTO " +
                relationTableName +
                "(`" +
                firstClassName +
                "_id`, `" +
                secondObjectName +
                "_id`) VALUES ('" +
                firstId +
                "', '" +
                secondId +
                "');";
            Connection connection = hikariDataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder);
            if (BaseSettings.debug) {
                System.out.println(preparedStatement);
            }
            boolean returnThis = preparedStatement.executeUpdate() != 0;
            connection.close();
            return returnThis;
        } else {
            System.err.println("Failed to create relation because table: " + relationTableName + " doesn't exist.");
        }
        return false;
    }

    public List getObjectIds(List objects) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        List list = new LinkedList();
        for (Object object : objects) {
            list.add(getObjectId(object));
        }
        return list;
    }

    public Object getObjectId(Object object) throws SQLException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<? extends Object> objectClass = object.getClass();
        Field[] fields = objectClass.getDeclaredFields();
        List values = new LinkedList();
        for (int i = 0; i != fields.length; i++) {
            Method getter = ReflectionMethods.getGetter(fields[i], objectClass);
            Object value = getter.invoke(object);
            if (value != null) {
                values.add(fields[i].getName());
                values.add(value);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT id FROM ").append(objectClass.getSimpleName().toLowerCase()).append(" WHERE ");
        for (int i = 0; i != values.size(); i += 2) {
            if (ReflectionMethods.isNotObjectOrArray(values.get(i + 1).getClass())) {
                stringBuilder.append(values.get(i));
                stringBuilder.append(" = '").append(values.get(i + 1)).append("'");
                if (i != (values.size() - 2)) {
                    stringBuilder.append(" AND ");
                }
            }
        }
        stringBuilder.append(";");
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(stringBuilder));
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();
        if (resultSet.next()) {
            long id = resultSet.getLong("id");
            Object newObject = ReflectionMethods.getNewInstance(objectClass);
            Method setId = ReflectionMethods.setId(newObject.getClass(), Long.class);
            setId.invoke(newObject, id);
            return newObject;
        }
        return null;
    }

    public List<BasicObjectInterface> getAllFromResultSet(ResultSet resultSet, Class theClass, boolean full) throws SQLException {
        List<BasicObjectInterface> objects = new LinkedList<>();
        try {
            List interfaceFields = null;
            while (resultSet.next()) {
                BasicObjectInterface object = (BasicObjectInterface) ReflectionMethods.getNewInstance(theClass);
                Field[] fields = object.getClass().getDeclaredFields();
                if (interfaceFields == null) {
                    interfaceFields = applySingularFieldsAndGetObjects(resultSet, object, fields);
                } else {
                    applySingularFieldsAndGetObjects(resultSet, object, fields);
                }
                objects.add(object);
            }
            if (interfaceFields != null && full) {
                addInterfaceFieldsToObject(objects, interfaceFields);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return objects;
    }

    public List<Long> getIdsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Long> objects = new LinkedList<>();
        while (resultSet.next()) {
            objects.add(resultSet.getLong("id"));
        }
        return objects;
    }

    public Object getFromResultSet(ResultSet resultSet, Object object) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        Field[] fields = object.getClass().getDeclaredFields();
        List interfaceFields = applySingularFieldsAndGetObjects(resultSet, object, fields);
        List list = new LinkedList();
        list.add(object);
        addInterfaceFieldsToObject(list, interfaceFields);
        return list.get(0);
    }

    public List applySingularFieldsAndGetObjects(ResultSet resultSet, Object object, Field[] fields) {
        List<Field> interfaceFields = new LinkedList<>();
        for (Field field : fields) {
            try {
                Class fieldClass = field.getType();
                Method method = ReflectionMethods.getSetter(field, object.getClass(), fieldClass);
                if (Date.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDate(field.getName()));
                } else if (Long.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getLong(field.getName()));
                } else if (Integer.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getInt(field.getName()));
                } else if (String.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (Float.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getFloat(field.getName()));
                } else if (Byte.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getByte(field.getName()));
                } else if (Character.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (Double.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDouble(field.getName()));
                } else if (long.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getLong(field.getName()));
                } else if (int.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getInt(field.getName()));
                } else if (float.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getFloat(field.getName()));
                } else if (byte.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getByte(field.getName()));
                } else if (char.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getString(field.getName()));
                } else if (boolean.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getBoolean(field.getName()));
                } else if (double.class.equals(fieldClass)) {
                    method.invoke(object, resultSet.getDouble(field.getName()));
                } else if (field.getType().isEnum()) {
                    String string = resultSet.getString(field.getName());
                    Object valueMethod = ReflectionMethods.getValueOf(fieldClass, string);
                    if (valueMethod != null) {
                        method.invoke(object, valueMethod);
                    }
                } else if (field.getType().isArray()) {
                    if (BaseSettings.debug) {
                        //System.out.println("Array: " + field.getType());
                    }
                } else if (field.getType().isInterface()) {
                    interfaceFields.add(field);
                } else {
                    if (BaseSettings.debug) {
                        long id = resultSet.getLong(field.getName());
                        if (id != 0) {
                            BasicObjectInterface basicObjectInterface = (BasicObjectInterface) ReflectionMethods.getNewInstance(field.getType());
                            basicObjectInterface.setId(id);
                            method.invoke(object, basicObjectInterface);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return interfaceFields;
    }

    public void addInterfaceFieldsToObject(List<BasicObjectInterface> objects, List<Field> fields) throws InvocationTargetException, IllegalAccessException, SQLException, InstantiationException {
        if (fields.size() == 0) {
            return;
        }
        StringBuilder selectString = new StringBuilder();
        selectString.append("id, ");
        StringBuilder joins = new StringBuilder();
        String declaringClassName = fields.get(0).getDeclaringClass().getSimpleName().toLowerCase();
        for (int i = 0; i != fields.size(); i++) {
            Field field = fields.get(i);
            //SELECT
            selectString.append(field.getName());
            selectString.append("_id");
            if ((fields.size() - 1) != i) {
                selectString.append(", ");
            }
            //JOIN
            joins.append("LEFT JOIN ");
            joins.append(declaringClassName.toLowerCase());
            joins.append("_");
            joins.append(field.getName());
            joins.append(" ON ");
            joins.append(declaringClassName.toLowerCase());
            joins.append(".id");
            joins.append(" = ");
            joins.append(declaringClassName.toLowerCase());
            joins.append("_");
            joins.append(field.getName().toLowerCase());
            joins.append(".");
            joins.append(declaringClassName);
            joins.append("_id ");
        }
        //WHERE
        StringBuilder whereClauses = new StringBuilder();
        String declaringClassLower = declaringClassName.toLowerCase();
        whereClauses.append("WHERE ").append(declaringClassLower).append(".id = ").append(objects.get(0).getId());
        for (int i = 1; i != objects.size(); i++) {
            whereClauses.append(" OR ").append(declaringClassLower).append(".id = ").append(objects.get(i).getId());
            if (i == (objects.size() - 1)) {
                whereClauses.append(";");
            }
        }
        String statement = "SELECT " + selectString + " FROM " + declaringClassName + " " + joins + whereClauses;
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(statement);
        if (BaseSettings.debug) {
            System.out.println(preparedStatement);
        }
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        connection.close();
        //Extract to objects
        while (resultSet.next()) {
            Long objectColumnId = resultSet.getLong("id");
            Object object = null;
            for (BasicObjectInterface itObject : objects) {
                if (itObject.getId().equals(objectColumnId)) {
                    object = itObject;
                    break;
                }
            }
            for (Field field : fields) {
                long columnId = resultSet.getLong(field.getName().toLowerCase() + "_id");
                if (columnId != 0) {
                    Method getList = ReflectionMethods.getGetter(field, object.getClass());
                    List list = (List) getList.invoke(object);
                    if (list == null) {
                        list = new LinkedList();
                    }
                    try {
                        ParameterizedType pt = (ParameterizedType) field.getGenericType();
                        Type subType = pt.getActualTypeArguments()[0];
                        BasicObjectInterface basicObjectInterface = (BasicObjectInterface) ReflectionMethods.getNewInstance(Class.forName(subType.getTypeName()));
                        basicObjectInterface.setId(columnId);
                        list.add(basicObjectInterface);
                        Method listSetter = ReflectionMethods.getSetter(field, object.getClass(), List.class);
                        listSetter.invoke(object, list);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public boolean tableExists(Class theClass) throws SQLException {
        return tableExists(theClass.getSimpleName().toLowerCase());
    }

    public boolean tableExists(String tableName) throws SQLException {
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SHOW TABLES LIKE '" + tableName + "';");
        ResultSet resultSet = preparedStatement.executeQuery();
        connection.close();
        return resultSet.next();
    }

    public boolean createSQLTables(Class theClass) {
        Field[] fields = theClass.getDeclaredFields();
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(theClass.getSimpleName().toLowerCase()).append("` (");
        for (Field field : fields) {
            Class fieldClass = field.getType();
            if (Date.class.equals(fieldClass)) {
                if (field.getName().equals("created") || field.getName().equals("updated")) {
                    sql.append("`").append(field.getName()).append("` datetime(6) NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6), ");
                } else {
                    sql.append("`").append(field.getName()).append("` datetime(6) NULL, ");
                }
            } else if (Long.class.equals(fieldClass) || long.class.equals(fieldClass)) {
                if (field.getName().equals("id")) {
                    sql.append("`id` bigint(20) NULL AUTO_INCREMENT, ");
                } else {
                    sql.append("`").append(field.getName()).append("` bigint(20) NULL, ");
                }
            } else if (Integer.class.equals(fieldClass) || int.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` int(11) NULL, ");
            } else if (String.class.equals(fieldClass) || fieldClass.isEnum() || field.getType().isEnum()) {
                sql.append("`").append(field.getName()).append("` varchar(255) NULL, ");
            } else if (Float.class.equals(fieldClass) || float.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` float NULL, ");
            } else if (Byte.class.equals(fieldClass) || byte.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` binary(50) NULL, ");
            } else if (Character.class.equals(fieldClass) || char.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` char(50) NULL, ");
            } else if (Double.class.equals(fieldClass) || double.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` double NULL, ");
            } else if (boolean.class.equals(fieldClass)) {
                sql.append("`").append(field.getName()).append("` bit(1) NULL, ");
            } else if (field.getType().isArray()) {
                System.out.println("ARRAYS ARE UNSUPPORTED");
            } else if (field.getType().isInterface()) {
                String simpleLowerClass = theClass.getSimpleName().toLowerCase();
                String simpleLowerFieldClass = field.getName().toLowerCase();
                StringBuilder constraintTables = new StringBuilder();
                constraintTables.append("CREATE TABLE IF NOT EXISTS `").append(simpleLowerClass).append("_").append(simpleLowerFieldClass).append("` (");
                constraintTables.append("`").append(simpleLowerClass).append("_id` bigint(20) NULL,");
                constraintTables.append("`").append(simpleLowerFieldClass).append("_id` bigint(20) NULL,");
                constraintTables.append("KEY `").append(simpleLowerClass).append("` (`").append(simpleLowerClass).append("_id`),");
                constraintTables.append("KEY `").append(simpleLowerFieldClass).append("` (`").append(simpleLowerFieldClass).append("_id`)");
                constraintTables.append(") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
                try {
                    Connection connection = hikariDataSource.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(constraintTables.toString());
                    if (BaseSettings.debug) {
                        System.out.println(preparedStatement);
                    }
                    preparedStatement.execute();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println(sql);
                    return false;
                }
            } else {
                sql.append("`").append(field.getName()).append("` bigint(20) NULL, ");
            }
        }
        sql.append("PRIMARY KEY (`id`)");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        try {
            Connection connection = hikariDataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            if (BaseSettings.debug) {
                System.out.println(sql);
            }
            preparedStatement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(sql);
            return false;
        }
        return true;
    }
}