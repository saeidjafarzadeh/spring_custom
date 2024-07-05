package org.spring.boot.spring_custom.spring.container;

import com.github.lalyos.jfiglet.FigletFont;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Properties;

public class SpringHttpServer {

    private static Log logger = LogFactory.getLog( "");
    private BeanContainer beanContainer;

    public SpringHttpServer(BeanContainer beanContainer){
        logger.info("....................................................................start run http server ....................................................");


        this.beanContainer=beanContainer;
    }

    public void  start() throws IOException {
        String port;
        Properties properties=new Properties();
       try (InputStream inputStream=new FileInputStream("src/main/resources/Application.Properties")){
           properties.load(inputStream);
          port= properties.getProperty("server.port");

       }
       HttpHandler handler=new HttpHandler() {
           @Override
           public void handle(HttpExchange exchange) throws IOException {
               String path=exchange.getRequestURI().getPath();
               String methodType=exchange.getRequestMethod();
               Method method=beanContainer.getEndPointMethod(path,methodType);
               if (method!=null){
                   try {
                       Object controller=beanContainer.getBean(method.getDeclaringClass());
                       Object[] parameters=beanContainer.getMethodParameters(exchange,method);
                       String response=(String) method.invoke(controller,parameters);
                       logger.info("....................................................................http method finded with status 200 ok ....................................................");

                       exchange.sendResponseHeaders(200,response.getBytes().length);
                       OutputStream outputStream=exchange.getResponseBody();
                       outputStream.write(response.getBytes());
                       outputStream.close();
                   }catch (Exception e){
                       logger.info("....................................................................http method finded with status 500 internal server ....................................................");

                       exchange.sendResponseHeaders(500,0);
                   }
               }else {
                   logger.info("....................................................................http method not founded with status 404 ....................................................");

                   exchange.sendResponseHeaders(404,0);
               }



           }
       };


        HttpServer server=HttpServer.create(new InetSocketAddress(Integer.parseInt(port)),0);
        server.createContext("/", handler);
        server.setExecutor(null);
        server.start();
        logger.info("..................................................................started server on port: "+port+"....................................................");


    }




}
