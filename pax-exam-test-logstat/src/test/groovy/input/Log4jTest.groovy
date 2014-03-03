package test.groovy.input;
import javax.inject.Inject;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.junit.runner.JUnitCore;
import org.osgi.service.cm.ManagedService;
import service.LogStat;
import test.groovy.common.TestUTCommon;
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class Log4jTest {
	public Log4jTest() {
	}

	@Inject
	private org.osgi.framework.BundleContext context;
	String wd = System.getProperty("user.dir");
	@Configuration
	public Option[] config() {
		return options(
		cleanCaches(true),
		frameworkStartLevel(6),
		// felix log level
		systemProperty("felix.log.level").value("4"), // 4 = DEBUG
		// setup properties for fileinstall bundle.
		systemProperty("felix.home").value(wd),
		// Pax-exam make this test code into OSGi bundle at runtime, so
		// we need "groovy-all" bundle to use this groovy test code.
		mavenBundle("org.codehaus.groovy", "groovy-all", "2.2.1").startLevel(2),
		mavenBundle("org.jruby", "jruby-complete", "1.7.10").startLevel(2),
		mavenBundle("org.wiperdog", "logstat", "1.0").startLevel(3),
		junitBundles()
		);
	}

	private LogStat svc;
	HashMap<String , Object> input_conf;
	HashMap<String , Object> output_conf;
	HashMap<String , Object> filter;
	HashMap<String , Object> conf;
	BufferedReader br;
	String line;
	String result;
	TestUTCommon test_common = new TestUTCommon();
	/**
	 * Note :  Run this project src\test\resources\data_test\input\testLog4j\Log4jEx (A log4j server to send
	 * logs to logstat via socket) before testing   
	 */
	
	@Before
	public void prepare() {
		input_conf = new HashMap<String, Object>();
		output_conf = new HashMap<String, Object>();
		filter = new HashMap<String, Object>();
		conf = new HashMap<String, Object>();
		output_conf.put("type", "file");
		output_conf.put("destination","src/test/resources/data_test/input/testLog4j/output/output.log");
		result = "";
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"priority" : "(ERROR)"
			]
		]
		// get data of log4j
		input_conf.put("input_type", "log4j");
		try {
			svc = context.getService(context.getServiceReference(LogStat.class.getName()));
//			def cmd_run_log4j = ' cmd /c cd "src/test/resources/data_test/Log4jEx/src/com/java" & javac -cp .;log4j-1.2.14.jar Log4jSocketAppenderExample.java '
//			def proc = cmd_run_log4j.execute();
//			proc.waitFor();
//			//cmd_run_log4j = 'start cmd /c cd "src/test/resources/data_test/Log4jEx/src/com/java" & cmd /c java -cp .;log4j-1.2.14.jar Log4jSocketAppenderExample '
//			cmd_run_log4j = 'cmd /c java -cp  .;src/test/resources/data_test/Log4jEx/src/com/java;log4j-1.2.14.jar;. Log4jSocketAppenderExample '
//			proc = cmd_run_log4j.execute();

		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	@After
	public void finish() {
		test_common.cleanData("")
	}

	/**
	 * Check output with input config contains one variable is port.
	 * Func will be get default of timeout for run.
	 * Expected: return data contains message of log level err. 
	 */
	@Test
	public void testLog4j_01() {
		input_conf.put("port", 2808);
		input_conf.put("timeout", 15);
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf);
		// result data
		result = readFileOutput("src/test/resources/data_test/input/testLog4j/output/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		//assertTrue(result.contains('"message"=>"Log4j debug message!!"'))
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains two variable is port and timeout.
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_02() {
		input_conf.put("port", 2808);
		input_conf.put("timeout", 15);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput("src/test/resources/data_test/input/testLog4j/output/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains two variable is port and host
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_03() {
		input_conf.put("port", 2808);
		input_conf.put("host", "localhost");
		input_conf.put("timeout", 15);		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput("src/test/resources/data_test/input/testLog4j/output/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains all of variable is port, timeout and host
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_04() {
		input_conf.put("port", 2808);
		input_conf.put("timeout", 15);
		input_conf.put("host", "localhost");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput("src/test/resources/data_test/input/testLog4j/output/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains one variable is port and value of port is out of range
	 * Expected: Generate error message : "[Logstat]: port out of range:66666" 
	 */
	@Test
	public void testLog4j_05() {
		input_conf.put("port", 66666);
		input_conf.put("timeout", 15);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File("src/test/resources/data_test/input/testLog4j/output/output.log")).exists())
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains port is null
	 * Expected:  Generate error message : "[Logstat]: Port is required" 
	 */
	@Test
	public void testLog4j_06() {
		input_conf.put("port", null);
		input_conf.put("timeout", 15);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		assertFalse((new File("src/test/resources/data_test/input/testLog4j/output/output.log")).exists())
		Thread.sleep(3000)
	}

	/**
	 * Check output with input config contains two variable is port, host. Value of host does not exist.
	 * Expected: return data is null.
	 */
	@Test
	public void testLog4j_07() {
		input_conf.put("port", 2808);
		input_conf.put("port", 15);
		input_conf.put("host", "localhost");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		assertFalse((new File("src/test/resources/data_test/input/testLog4j/output/output.log")).exists())
		Thread.sleep(3000)
	}

	/**
	 * Check output with value of host is null.
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_08() {
		input_conf.put("port", 2808);
		input_conf.put("timeout", 15);
		input_conf.put("host", null);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput("src/test/resources/data_test/input/testLog4j/output/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		Thread.sleep(3000)
	}

	/**
	 * @param filePath
	 *            path to file contains data output for test
	 * @return data output need to compare
	 * @throws IOException
	 */
	public String readFileOutput(String filePath) throws IOException {
		// get data output need to compare, read from file
		try {
			String output = "";
			br = new BufferedReader(new FileReader(filePath));
			while ((line = br.readLine()) != null) {
				if (output != "") {
					output += "\n" + line;
				} else {
					output += line;
				}
			}
			return output;
		} catch (IOException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}

