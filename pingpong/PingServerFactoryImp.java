package pingpong;
import rmi.RMIException;
import rmi.*;
public class PingServerFactoryImp implements PingServerFactory{
  public PingServer makePingServer() throws RMIException{
    try{
      PingServer server = new PingServerImp();
      return server;
    }
    catch(Exception e){
      e.printStackTrace();
      throw new RMIException(e);
    }
   }
  }

