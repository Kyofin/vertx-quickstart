package io.vertx.example;

import com.hazelcast.config.ClasspathXmlConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HelloWorldEmbedded {

  public static void main(String[] args) {
    VertxOptions options = new VertxOptions();
    ClusterManager mgr = new HazelcastClusterManager(new ClasspathXmlConfig("hazelcast.xml"));
    options.setClusterManager(mgr);
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Vertx vertx = res.result();
        // Your verticle deployment code goes here
        vertx.deployVerticle(new MyVerticle());

        // 定时每5秒发布消息
        vertx.setPeriodic(15000, id -> {
          // 获取当前ip地址
          InetAddress address = null;
          try {
            address = InetAddress.getLocalHost();
          } catch (UnknownHostException e) {
            e.printStackTrace();
          }
          // 获取当前时间，格式为2022-01-01 12:00:00
          String dateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
          vertx.eventBus().send("cluster", "ping from " + address.getHostName() + " at " + dateTime);
        });
      }
    });


  }

  static class MyVerticle extends io.vertx.core.AbstractVerticle {
    @Override
    public void start() throws Exception {
      System.out.println("Hello World!");
      vertx.eventBus().consumer("cluster", message -> {
        // 获取当前ip地址
        InetAddress address = null;
        try {
          address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
        System.out.println("Received message: " + message.body());
        message.reply("pong from " + address.getHostName());
      });
    }
  }


}
