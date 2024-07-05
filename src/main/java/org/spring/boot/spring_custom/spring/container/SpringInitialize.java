package org.spring.boot.spring_custom.spring.container;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SpringInitialize {
    private static Log logger = LogFactory.getLog( "");

    public static BeanContainer initializer(String path){



        BeanContainer beanContainer = new BeanContainer(path);
        try {

            SpringHttpServer server=new SpringHttpServer(beanContainer);
            logger.info("....................................................................application context success all of change ....................................................");

            server.start();
            logger.info("......................................................................................let go....................................................");

            return beanContainer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
