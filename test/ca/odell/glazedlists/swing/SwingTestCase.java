/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;
// reflection for invoking methods by name
import java.lang.reflect.*;
// swing utilities for interacting with the event dispatch thread
import javax.swing.SwingUtilities;
import javax.swing.event.*;

/**
 * Utility class for running JUnit tests with Swing code.
 *
 * <p>This class has the following limitations:
 *  <li>Extending classes must create a method called <code>testGui()</code> which
 *      contains the single statement, <code>super.testGui()</code>. This provides
 *      an entry point for normal JUnit to run on the Swing thread.
 *  <li>Extending classes must not define any other <code>testXXX()</code> methods. Instead they
 *      shall define only <code>guiTestXXX()</code> methods.
 *  <li>If one test fails, they all fail.
 *
 * <p>This is a hack, but it makes testing our GUI code much easier and more valid.
 *
 * <p>Planned extensions include support to run code simultaneously with the
 * event dispatch thread to support testing Swing in a concurrent environment.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class SwingTestCase extends TestCase {
    
    /** test only methods that start with this prefix */
    public static final String TEST_METHOD_PREFIX = "guiTest";
    public static final String SET_UP_METHOD = "guiSetUp";
    public static final String TEAR_DOWN_METHOD = "guiTearDown";
    public static final String JUNIT_BAD_PREFIX = "test";
    public static final String JUNIT_OK_METHOD = "testGui";
    
    /** useful empty arrays */
    private static final Class[] DECLARE_NO_PARAMETERS = new Class[0];
    private static final Object[] SEND_NO_PARAMETERS = new Class[0];
    
    /**
     * Prepare for the test.
     */
    public final void setUp() {
        // do nothing
    }

    /**
     * Clean up after the test.
     */
    public final void tearDown() {
        // do nothing
    }

    /**
     * Run all methods from the specified test class that start with the prefix
     * "guiTest". These test methods will be executed from the Swing event dispatch
     * thread.
     */
    public void testGui() {
        try {
            SwingUtilities.invokeAndWait(new TestOnSwingThread(getClass()));
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
    private class TestOnSwingThread implements Runnable {
        private Class guiClass;
        public TestOnSwingThread(Class guiClass) {
            this.guiClass = guiClass;
        }
        public void run() {
            try {
                // create an instance with the appropriate methods
                Object instance = guiClass.getConstructor(DECLARE_NO_PARAMETERS).newInstance(new Object[0]);
                Method setUp = null;
                try {
                    setUp = guiClass.getMethod(SET_UP_METHOD, DECLARE_NO_PARAMETERS);
                } catch(NoSuchMethodException e) { 
                    // do nothing
                }
                Method tearDown = null;
                try { 
                    tearDown = guiClass.getMethod(TEAR_DOWN_METHOD, DECLARE_NO_PARAMETERS);
                } catch(NoSuchMethodException e) {
                    // do nothing
                }
                
                // run all test methods
                Method[] allMethods = guiClass.getDeclaredMethods();
                for(int m = 0; m < allMethods.length; m++) {
                    if(!allMethods[m].getName().equals(JUNIT_OK_METHOD) && allMethods[m].getName().startsWith(JUNIT_BAD_PREFIX)) throw new IllegalStateException("No testXXX() methods allowed in decendents of SwingTestCase, use guiTestXXX() instead.");
                    if(!allMethods[m].getName().startsWith(TEST_METHOD_PREFIX)) continue;
                    
                    System.out.println("Running " + allMethods[m].getName());
                    if(setUp != null) setUp.invoke(instance, SEND_NO_PARAMETERS);
                    allMethods[m].invoke(instance, SEND_NO_PARAMETERS);
                    if(tearDown != null) tearDown.invoke(instance, SEND_NO_PARAMETERS);
                }
            } catch(InvocationTargetException e) {
                Assert.fail(e.getCause().getClass() + ": \"" + e.getCause().getMessage() + "\"");
            } catch(NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch(InstantiationException e) {
                throw new RuntimeException(e);
            } catch(IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Ensures that all waiting Swing events have been handled before proceeding.
     * This hack method can be used when unit testing classes that interact with
     * the Swing event dispatch thread.
     *
     * <p>This guarantees that all events in the event dispatch queue before this
     * method is called will have executed. It provides no guarantee that the event
     * dispatch thread will be empty upon return.
     */
    private void flushEventDispatchThread() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}