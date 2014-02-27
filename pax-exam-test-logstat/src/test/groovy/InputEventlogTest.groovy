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
import TestUTCommon;
/**
 * Testcase for process input from csv file
 * @author nguyenxuanluong
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class InputEventlogTest {
	public InputEventlogTest() {
	}

	@Inject
	private org.osgi.framework.BundleContext context;

	@Configuration
	public Option[] config() {
		String wd = System.getProperty("user.dir");
		return options(
		cleanCaches(true),
		frameworkStartLevel(6),
		// felix log level
		systemProperty("felix.log.level").value("4"), // 4 = DEBUG
		// setup properties for fileinstall bundle.
		systemProperty("felix.home").value(wd),
		//
		// Pax-exam make this test code into OSGi bundle at runtime, so
		// we need "groovy-all" bundle to use this groovy test code.
		mavenBundle("org.codehaus.groovy", "groovy-all", "2.2.1").startLevel(2),
		// wrappedBundle(mavenBundle("org.jruby", "jruby-complete", "1.7.10")),
		mavenBundle("org.jruby", "jruby-complete", "1.7.10").startLevel(2),
		mavenBundle("org.wiperdog", "logstat", "1.0").startLevel(3),
		junitBundles()
		);
	}

	private String cfgFile = null;
	private LogStat svc;
	private String currentDir = "";
	private String logs_test_dir = "";
	TestUTCommon test_common = new TestUTCommon();
	HashMap<String , Object> input_conf = new HashMap<String, Object>();
	HashMap<String , Object> output_conf = new HashMap<String, Object>();
	HashMap<String , Object> filter_conf = new HashMap<String, Object>();
	HashMap<String , Object> conf = new HashMap<String, Object>();


	@Before
	public void prepare() {

		//Generate event-log data for test .this command will be create an eventlog in Application catagory ,the
		//Log type is ERROR OR WARNING and the description of event
		def create_evlog_cmd = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 1 /T ERROR /D "1-This is an error event of LOGSTAT application" '
		def create_evlog_cmd2 = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 2 /T ERROR /D "2-This is another error event of LOGSTAT application"'
		def create_evlog_cmd3 = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 3 /T WARNING /D "2-This is a warning event of LOGSTAT application" '
		def proc = create_evlog_cmd.execute();
		proc.waitFor();
		proc = create_evlog_cmd2.execute();
		proc.waitFor();
		proc = create_evlog_cmd3.execute();
		proc.waitFor();

		currentDir = System.getProperty("user.dir");
		logs_test_dir = currentDir + "/src/test/resources/data_test/testEventlog";
		svc = context.getService(context.getServiceReference(LogStat.class.getName()));
		output_conf.put("type", "file");
		filter_conf = [
			filter_type :"",
			filter_conf :[
				"data_field" : [
					"source_name",
					"type",
					"time",
					"message"
				],
				"filter" : [
					"message" : '^(?!\\s*$).+',
					"source_name" : '(LOGSTAT)'
				]
			]
		]
		input_conf.put("input_type", "eventlog");
	}

	@After
	public void finish() {
	}
	/**
	 * Test for case : full parameters
	 * Input : eventlogs generate from @prepare step	  
	 * Expected : output file contains 3 records with format 
	 * {"source_name"=>"LOGSTAT", "type"=>[event_type], "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 *            the event logs store in Application catagory
	 *            the evemt logs has the time generated >= @from_time_generate params	 *            
	 * details in \src\test\data_test\testEventlog\expected\testEventlog0.output
	 */
	@Test
	public void testEventlog0() {
		try{
			def from_time_generated = "2014-02-26"
			input_conf.put("event_log_type","Application");
			input_conf.put("from_time_generated","from_time_generated");
			test_common.cleanData("src/test/resources/data_test/testEventlog/output/testEventlog0.output")
			output_conf.put("destination", "src/test/resources/data_test/testEventlog/output/testEventlog0.output");
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testEventlog/expected/testEventlog0.output","src/test/resources/data_test/testEventlog/output/testEventlog0.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : Missing 'event_log_type' parameter
	 * Input : eventlogs generate from @prepare step  
	 * Expected : output file contains 3 records with format 
	 * {"source_name"=>"LOGSTAT", "type"=>[event_type], "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 *             the event logs store in Application catagory
	 *             the evemt logs has the time generated >= @from_time_generate params
	 * details in \src\test\data_test\testEventlog\expected\testEventlog1.output
	 */
	@Test
	public void testEventlog1() {
		try{
			def from_time_generated = "2014-02-26"
			test_common.cleanData("src/test/resources/data_test/testEventlog/output/testEventlog1.output")
			output_conf.put("destination", "src/test/resources/data_test/testEventlog/output/testEventlog1.output");
			input_conf.put("from_time_generated",from_time_generated);
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testEventlog/expected/testEventlog1.output","src/test/resources/data_test/testEventlog/output/testEventlog1.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : Missing 'event_log_type' & 'from_time_generated' parameters
	 * Input : eventlogs generate from @prepare step
	 * Expected : output file contains 3 records with format 
	 * {"source_name"=>"LOGSTAT", "type"=>[event_type], "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 * details in \src\test\data_test\testEventlog\expected\testEventlog2.output
	 */
	@Test
	public void testEventlog2() {
		try{
			test_common.cleanData("src/test/resources/data_test/testEventlog/output/testEventlog2.output")
			output_conf.put("destination", "src/test/resources/data_test/testEventlog/output/testEventlog2.output");
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testEventlog/expected/testEventlog2.output","src/test/resources/data_test/testEventlog/output/testEventlog2.output"))
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : Missing 'event_log_type' & 'from_time_generated' parameters ,
	 * the filter was changed to get only eventlogs has the event source is 'LOGSTAT' and type is 'ERROR'
	 * Input : eventlogs generate from @prepare step
	 * Expected : output file contains 2 records with format
	 * {"source_name"=>"LOGSTAT", "type"=>"ERROR", "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 * details in \src\test\data_test\testEventlog\expected\testEventlog3.output
	 */
	@Test
	public void testEventlog3() {
		try{
			test_common.cleanData("src/test/resources/data_test/testEventlog/output/testEventlog3.output")
			output_conf.put("destination", "src/test/resources/data_test/testEventlog/output/testEventlog3.output");
			filter_conf = [
				filter_type :"",
				filter_conf :[
					"data_field" : [
						"source_name",
						"type",
						"time",
						"message"
					],
					"filter" : [
						"message" : '^(?!\\s*$).+',
						"source_name" : '(LOGSTAT)',
						"type" : "(ERROR)"
					]
				]
			]
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testEventlog/expected/testEventlog3.output","src/test/resources/data_test/testEventlog/output/testEventlog3.output"))
		} catch(Exception ex){
			println ex
		}
	}
}

