package com.xiaoniucode.etp.client.common.utils;

import com.xiaoniucode.etp.common.utils.StringUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class MavenArchiverUtil {
    private final static Logger logger= LoggerFactory.getLogger(MavenArchiverUtil.class);
    private final static String MAVEN_ARCHIVER_PATH = "target/maven-archiver/pom.properties";
    private final static String POM_PATH = "etp-client/pom.xml";
    @Getter
    private static final String version;

    static {
        version = initVersion();
    }

    private static String initVersion() {
        try {
            File propFile = new File(MAVEN_ARCHIVER_PATH);
            if (propFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propFile)) {
                    props.load(fis);
                    String v = props.getProperty("version");
                    if (StringUtils.hasText(v)) {
                        return v.trim();
                    }
                }
            } else {
                return readFromPomXml();
            }
        } catch (Exception e) {
        }
        logger.warn("没有获取到客户端版本号");
        return null;
    }

    private static String readFromPomXml() {
        try {
            File pomFile = new File(POM_PATH);
            if (pomFile.exists()) {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(pomFile);
                doc.getDocumentElement().normalize();

                NodeList versionNodes = doc.getElementsByTagName("version");
                if (versionNodes.getLength() > 0) {
                    String v = versionNodes.item(0).getTextContent().trim();
                    if (StringUtils.hasText(v)) {
                        return v.trim();
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
