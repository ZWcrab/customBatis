package com.custom.sqlSession;

import com.custom.pojo.Configuration;
import com.custom.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DefaultSqlSession implements SqlSession {
    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    public <E> List<E> queryList(String statementId, Object... params) throws IllegalAccessException, IntrospectionException, InstantiationException, NoSuchFieldException, SQLException, InvocationTargetException, ClassNotFoundException {
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);
        return (List<E>) list;
    }

    public <T> T queryOne(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        List<Object> list = queryList(statementId, params);
        if (list.size() == 1) {
            return (T) list.get(0);
        } else {
            throw new RuntimeException("查询结果为空或者返回结果过多");
        }
    }

    @Override
    public int doUpdate(String statementId, Object... params) throws IllegalAccessException, ClassNotFoundException, IntrospectionException, InstantiationException, SQLException, InvocationTargetException, NoSuchFieldException {
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        return simpleExecutor.doUpdate(configuration, mappedStatement, params);
    }


    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用JDK动态代理，来为DAO接口生成代理对象并返回
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();

                String statementId = className + "." + methodName;
                Map<String, MappedStatement> mappedStatementMap = configuration.getMappedStatementMap();
                MappedStatement mappedStatement = mappedStatementMap.get(statementId);
                switch (mappedStatement.getStatementType()) {
                    case "select":
                        Type genericReturnType = method.getGenericReturnType();
                        //判断是否进行泛型类型参数化
                        if (genericReturnType instanceof ParameterizedType) {
                            return queryList(statementId, args);
                        }
                        return queryOne(statementId, args);
                    case "update":
                    case "insert":
                    case "delete":
                        return doUpdate(statementId, args);
                    default:
                        throw new RuntimeException("非法标签");
                }


            }
        });
        return (T) proxyInstance;
    }
}
