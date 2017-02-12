package pingpong;
import rmi.RMIException;
import rmi.*;
public class PingServerImp implements PingServer{
  public String ping(int idNumber) throws RMIException{
    return "Pong" + idNumber;
  }
}
