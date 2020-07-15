package com.custom.sqlSession;

import com.custom.io.Resources;
import com.custom.pojo.Configuration;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class XMLConfigBuilder {

    private Configuration configuration;

    public XMLConfigBuilder() {
        this.configuration = new Configuration();
    }

    /**
     * 该方法使用dom4j对配置文件进行解析，封装Configuration
     */
    public Configuration parseConfig(InputStream inputStream) throws DocumentException, PropertyVetoException {
        Document document = new SAXReader().read(inputStream);
        // <configuration>
        Element rootElement = document.getRootElement();
        List<Element> list = rootElement.selectNodes("//property");
        Properties properties = new Properties();
        for (Element element : list) {
            String name = element.attributeValue("name");
            String value = element.attributeValue("value");
            properties.setProperty(name, value);
        }


        // c3p0连接池
        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass((String) properties.get("driverClass"));
        comboPooledDataSource.setJdbcUrl((String) properties.get("jdbcUrl"));
        comboPooledDataSource.setUser((String) properties.get("username"));
        comboPooledDataSource.setPassword((String) properties.get("password"));

        configuration.setDataSource(comboPooledDataSource);

        // mapper标签
        List<Element> mapperList = rootElement.selectNodes("//mapper");
        for (Element element : mapperList) {
            String mapperPath = element.attributeValue("resource");
            InputStream resourceAsStream = Resources.getResourceAsStream(mapperPath);
            XMLMapBuilder xmlMapBuilder = new XMLMapBuilder(configuration);
            xmlMapBuilder.parse(resourceAsStream);

        }
        return configuration;

    }
}
