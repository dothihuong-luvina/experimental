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
import org.jruby.embed.InvokeFailedException;
import org.jruby.embed.ScriptingContainer;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SyslogTest {
    public SyslogTest() {}

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
	String expected;
	String inputStr;
	
	@Before
	public void prepare() {
		input_conf = new HashMap<String, Object>();
		output_conf = new HashMap<String, Object>();
		filter = new HashMap<String, Object>();
		conf = new HashMap<String, Object>();
		output_conf.put("type", "file");		
		def outFile = ["path":wd + "/output.log]
		output_conf.put("config", outFile)
		result = "";
		expected = "";
		inputStr = "";
		// filter data of log
		filter = [
			"filter_type" : "match_field",
			"filter_conf" : [
				"date": "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{2} \\d{2}:\\d{2}:\\d{2}",
				"message": "(vmRHEL55x64).*"
			]
		]
		// get data of sys log
		input_conf.put("input_type", "sys_log");
		// set data for log
		inputStr = readFileOutput(wd + "/src/test/resources/data_test/input/testSyslog/messages");
		FileWriter fw = new FileWriter("/usr/messages")
		fw.write(inputStr);
		fw.close();
		
		try {
			svc = context.getService(context.getServiceReference(LogStat.class.getName()));
		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	@After
	public void finish() {
		new File(wd + "/output.log").delete();
		new File("/usr/messages").delete();
	}
	
	/**
	 * Check func with all of variable is path_conf, log_type, from_time_generated.
	 * Expected: return data with datetime more than datetime of config for test
	 */
    @Test
    public void testSyslog_01() {
		input_conf.put("path_conf", wd + "/src/test/resources/data_test/input/testSyslog/logtest01.conf");
		input_conf.put("log_type", "log.test");
		input_conf.put("from_time_generated", "Feb 26 18:08:33");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		expected = readFileOutput(wd + "/src/test/resources/data_test/input/testSyslog/expected_test01.log");
		assertEquals(expected, result)
    }
	
	/**
	 * Check func with data of file corresponding to path_conf is empty.
	 * Expected: return data is empty
	 */
	@Test
	public void testSyslog_02() {
		input_conf.put("path_conf", wd + "/src/test/resources/data_test/input/testSyslog/logtest02.conf");
		input_conf.put("log_type", "syslog");
		input_conf.put("from_time_generated", "Feb 25 12:53:50");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.length() == 0)
	}
	
	/**
	 * Check func with value of path_conf is null.
	 * Expected: return data is empty
	 */
	@Test
	public void testSyslog_03() {
		input_conf.put("path_conf", null);
		input_conf.put("log_type", "syslog");
		input_conf.put("from_time_generated", "Feb 25 12:53:50");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.length() == 0)
	}
	
	/**
	 * Check func with value of path_conf does not exist.
	 * Expected: not return data
	 */
	@Test
	public void testSyslog_04() {
		input_conf.put("path_conf", "/path/not_exist.conf");
		input_conf.put("log_type", "syslog");
		input_conf.put("from_time_generated", "Feb 25 12:53:50");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		assertFalse(new File(wd + "/output.log").exists())
	}
	
	/**
	 * Check func with value of log_type is null.
	 * Expected: return data is empty
	 */
	@Test
	public void testSyslog_05() {
		input_conf.put("path_conf", wd + "/src/test/resources/data_test/input/testSyslog/logtest01.conf");
		input_conf.put("log_type", null);
		input_conf.put("from_time_generated", "Feb 25 12:53:50");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.length() == 0)
	}
	
	/**
	 * Check func with value of log_type does not exist.
	 * Expected: return data is empty
	 */
	@Test
	public void testSyslog_06() {
		input_conf.put("path_conf", wd + "/src/test/resources/data_test/input/testSyslog/logtest01.conf");
		input_conf.put("log_type", "type_log_not_exist");
		input_conf.put("from_time_generated", "Feb 25 12:53:50");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		assertTrue(result.length() == 0)
	}
	
	/**
	 * Check func with value of from_time_generated is null.
	 * Expected: return all data in log
	 */
	@Test
	public void testSyslog_07() {
		input_conf.put("path_conf", wd + "/src/test/resources/data_test/input/testSyslog/logtest01.conf");
		input_conf.put("log_type", "log.test");
		input_conf.put("from_time_generated", null);

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		// result data
		result = readFileOutput(wd + "/output.log");
		expected = readFileOutput(wd + "/src/test/resources/data_test/input/testSyslog/expected_test02.log");
		assertEquals(expected, result)
	}
	
	/**
	 * Check func with format of from_time_generated incorrect format of datetime.
	 * Expected: not return data
	 */
	@Test
	public void testSyslog_08() {
		input_conf.put("from_time_generated", "incorrect");

		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);

		svc.runLogStat(conf)
		assertFalse(new File(wd + "/output.log").exists())
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