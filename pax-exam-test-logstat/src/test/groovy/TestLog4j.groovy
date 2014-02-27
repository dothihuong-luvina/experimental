
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

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestLog4j {
    public TestLog4j() {}

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

	@Before
	public void prepare() {
		input_conf = new HashMap<String, Object>();
		output_conf = new HashMap<String, Object>();
		filter = new HashMap<String, Object>();
		conf = new HashMap<String, Object>();
		output_conf.put("type", "file");
		output_conf.put("destination", wd + "/output.log");
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
		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	@After
	public void finish() {
		new File(wd + "/output.log").delete();
	}
	
	/**
	 * Check output with input config contains one variable is port.
	 * Func will be get default of timeout for run.
	 * Expected: return data contains message of log level err. 
	 */
    @Test
    public void testLog4j_01() {
		input_conf.put("port", 4712);
		input_conf.put("timeout", 15);
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf);
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message!!"'))
		assertTrue(result.contains('"message"=>"Log4j debug message!!"'))
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
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message"'))
		Thread.sleep(3000)
	}
	
	/**
	 * Check output with input config contains two variable is port and host
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_03() {
		input_conf.put("port", 2808);
		input_conf.put("host", "10.0.1.189");
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message"'))
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
		input_conf.put("host", "10.0.1.189");
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message"'))
		Thread.sleep(3000)
	}
	
	/**
	 * Check output with input config contains one variable is port and value of port is out of range
	 * Expected: return data is null.
	 */
	@Test
	public void testLog4j_05() {
		input_conf.put("port", 66666);
		input_conf.put("timeout", 15);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/output.log")).exists())
		Thread.sleep(3000)
	}
	
	/**
	 * Check output with input config contains port is null
	 * Expected: return data is null.
	 */
	@Test
	public void testLog4j_06() {
		input_conf.put("port", null);
		input_conf.put("timeout", 15);
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		assertFalse((new File(wd + "/output.log")).exists())
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
		input_conf.put("host", "10.0.1.289");
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/output.log")).exists())
		Thread.sleep(3000)
	}
	
	/**
	 * Check output with value of host is null.
	 * Expected: return data contains message of log level err.
	 */
	@Test
	public void testLog4j_08() {
		input_conf.put("port", 2808);
		input_conf.put("port", 15);
		input_conf.put("host", null);
		
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		
		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.contains('"message"=>"Log4j error message"'))
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

