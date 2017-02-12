package pingpong;
import java.net.*;
import rmi.*;

public class Server{
      public static void main(String args[]){
        try{
        PingServerFactoryImp factory = new PingServerFactoryImp();
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8000);
        Skeleton<PingServerFactory> factory_skeleton = new Skeleton<PingServerFactory>(PingServerFactory.class, factory, address);
        factory_skeleton.start();
        while(true);
        }
        catch(Exception e){
          e.printStackTrace();
        }
    }

}
