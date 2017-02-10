package rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub implements Serializable
{
    private static String ARGNULL = "At least one of the arguments is null";
    private static String REMOTECLASSERR = "Not a remote interface";
    private static String NONINTERFACE = "Not an interface";
    private static String ILLEGALSTATE1 = "The skeleton has not been assigned and address " +
            "by the user and has not yet been started";
    private static String ILLEGALSTATE2 = "NO port has been assigned to the skeleton";
    private static String CANNOTIMPLM = "Class implementing the interface cannot initiate a stub";
    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException
    {
        checkClass(c);
        checkSkeleton(skeleton, 1);
        checkHost(skeleton.getAddress());
        StubInvocationHandler handler = new StubInvocationHandler(skeleton.getAddress(), c);
        try {
            return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
        }catch(Exception e){
            throw new Error(CANNOTIMPLM);
        }
    }

    public static <T>void checkClass(Class<T> c)
            throws NullPointerException, Error
    {
        if(c == null) throw new NullPointerException(ARGNULL);
        if(!c.isInterface()) throw new Error(NONINTERFACE);
        for(Method method: c.getDeclaredMethods()){
            if(!Arrays.asList(method.getExceptionTypes()).contains(RMIException.class)){
                throw new Error(REMOTECLASSERR);
            }
        }
    }

    public static <T>void checkSkeleton(Skeleton<T> skeleton, int from){
        if(skeleton == null) throw new NullPointerException(ARGNULL);
        InetSocketAddress add = skeleton.getAddress();
        if(from == 1 && add == null ){//&& !skeleton.isRunning()) {
            throw new IllegalStateException(ILLEGALSTATE1);
        }
        if(from == 2 && (add == null || add.getPort() == -1)) {
            throw new IllegalStateException(ILLEGALSTATE2);
        }
    }

    public static void checkHost(InetSocketAddress address) throws UnknownHostException{
        if(address.getAddress().getHostAddress().equals("0.0.0.0") && address.getPort() != -1){
            address.getAddress().getLocalHost();
        }
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
        checkClass(c);
        checkSkeleton(skeleton, 2);
        if(hostname == null){
            throw new NullPointerException(ARGNULL);
        }
        StubInvocationHandler handler =
                new StubInvocationHandler(new InetSocketAddress(hostname, skeleton.getAddress().getPort()), c);
        try {
            return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
        }catch(Exception e){
            throw new Error(CANNOTIMPLM);
        }
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        checkClass(c);
        if(address == null){
            throw new NullPointerException(ARGNULL);
        }
        StubInvocationHandler handler = new StubInvocationHandler(address, c);
        try {
            return (T) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, handler);
        }catch(Exception e){
            throw new Error(CANNOTIMPLM);
        }
    }


    private static class StubInvocationHandler implements InvocationHandler {

        private InetSocketAddress address;
        private Class interfaceClass;

        public Class getInterfaceClass() {return interfaceClass;}
        public InetSocketAddress getInetSocketAddress() {return address;}

        public StubInvocationHandler(InetSocketAddress addr, Class c){
            address = addr;
            interfaceClass = c;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            switch (method.getName()) {
                case "equals":
                    if (args[0] instanceof Proxy) {
                        StubInvocationHandler otherHandler = (StubInvocationHandler) Proxy.getInvocationHandler(args[0]);
                        return otherHandler.getInterfaceClass().equals(interfaceClass) &&
                                otherHandler.getInetSocketAddress().equals(address);
                    } else {
                        return false;
                    }
                case "hashCode":
                    return interfaceClass.hashCode() * 41 + address.hashCode() * 53;
                case "toString":
                    return "Remote interface: " + interfaceClass.getCanonicalName() + "; host&port: " + address.toString();
                default:
                    try {
                        System.out.println("Going to invoke method "+method.getName()+" in Stub");
                        Socket socket = new Socket(address.getHostString(), address.getPort());
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.flush();
                        outputStream.writeObject(method.getName());
                        outputStream.writeObject(method.getParameterTypes());
                        outputStream.writeObject(args);
                        outputStream.flush();
                        System.out.println("Finish sending method meta data in Stub to addr: "+address);

                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        String status = (String) inputStream.readObject();
                        if (status.equals("OK")) {
                            if (!method.getReturnType().equals(Void.TYPE)) {
                                Object ret = inputStream.readObject();
                                outputStream.close();
                                inputStream.close();
                                socket.close();
                                return ret;
                            }
                            return null;
                        } else {
                            Exception e = (Exception) inputStream.readObject();
                            outputStream.close();
                            inputStream.close();
                            socket.close();
                            throw e;
                        }
                    } catch (Exception e) {
                        if(!e.getClass().equals(RMIException.class) &&
                                (Arrays.asList(method.getExceptionTypes()).contains(e.getClass()))){
                            throw e;
                        }
                        throw new RMIException(e.getCause());
                    }
            }
        }

    }

}