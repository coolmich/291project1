package pingpong;
import java.net.*;
import rmi.Stub;

public class Client{
      public static void main(String args[]){
        try{
          InetSocketAddress address = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
          System.out.println(address.getHostString() + address.getPort());
          PingServerFactory factory = Stub.create(PingServerFactory.class, address);
          PingServer server = factory.makePingServer();
          int failed = 0;
          String return_string = server.ping(1);
          System.out.println(server.ping(1));
          if(! return_string.equals("Pong1")) failed ++;
          return_string = server.ping(2);
          System.out.println(server.ping(2));
          if(! return_string.equals("Pong2")) failed ++;
          return_string = server.ping(3);
          System.out.println(server.ping(3));
          if(! return_string.equals("Pong3")) failed ++;
          return_string = server.ping(4);
          System.out.println(server.ping(4));
          if(! return_string.equals("Pong4")) failed ++;
          System.out.println("4 Tests Completed, "+ failed+" TestsFailed");


        }
        catch(Exception e){
          e.printStackTrace();
        }
    }

}
