package com.custom.sqlSession;

import com.custom.pojo.Configuration;
import com.custom.pojo.MappedStatement;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

public class XMLMapBuilder {
    private Configuration configuration;
    public XMLMapBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public void parse(InputStream inputStream) throws DocumentException {
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();
        String namespace = rootElement.attributeValue("namespace");

        buildMappedStatement(rootElement, namespace, "//select");
        buildMappedStatement(rootElement, namespace, "//update");
        buildMappedStatement(rootElement, namespace, "//delete");
        buildMappedStatement(rootElement, namespace, "//insert");

    }

    private void buildMappedStatement(Element rootElement, String namespace, String node) {
        List<Element> elementList = rootElement.selectNodes(node);
        for (Element element : elementList) {
            String id = element.attributeValue("id");
            String resultType = element.attributeValue("resultType");
            String parameterType = element.attributeValue("parameterType");
            String sqlText = element.getTextTrim();
            MappedStatement mappedStatement = new MappedStatement();
            mappedStatement.setId(id);
            mappedStatement.setParameterType(parameterType);
            mappedStatement.setResultType(resultType);
            mappedStatement.setSql(sqlText);
            mappedStatement.setStatementType(element.getName());

            String statementId = namespace + "." + id;
            configuration.getMappedStatementMap().put(statementId, mappedStatement);
        }
    }
}
