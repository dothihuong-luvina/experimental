package test.groovy.input;
import java.sql.Time;

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
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
/**
 * Testcase for process input from csv file
 * @author nguyenxuanluong
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class EventlogTest {
	public EventlogTest() {
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
	def from_time_generated = null

	@Before
	public void prepare() {

		//Generate event-log data for test .this command will be create an eventlog in Application catagory ,the
		//Log type is ERROR OR WARNING and the description of event
		from_time_generated = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
		def create_evlog_cmd = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 1 /T ERROR /D "1-This is an error event of LOGSTAT application" '
		def create_evlog_cmd2 = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 2 /T ERROR /D "2-This is another error event of LOGSTAT application"'
		def create_evlog_cmd3 = 'cmd /c eventcreate /L Application /SO LOGSTAT /ID 3 /T WARNING /D "3-This is a warning event of LOGSTAT application" '
		def proc = create_evlog_cmd.execute();
		proc.waitFor();
		proc = create_evlog_cmd2.execute();
		proc.waitFor();
		proc = create_evlog_cmd3.execute();
		proc.waitFor();

		currentDir = System.getProperty("user.dir");
		logs_test_dir = currentDir + "/src/test/resources/data_test/input/testEventlog";
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
			input_conf.put("event_log_type","Application");
			input_conf.put("from_time_generated",from_time_generated.toString());
			test_common.cleanData("src/test/resources/data_test/input/testEventlog/output/testEventlog0.output")
			def outFile = ["path":"src/test/resources/data_test/input/testEventlog/output/testEventlog0.output"]
			output_conf.put("config", outFile)	
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			//Check if data return is 3 records logs generated from @prepare step
			assertTrue(test_common.countLines("src/test/resources/data_test/input/testEventlog/output/testEventlog0.output") == 3)
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : Missing 'event_log_type' parameter
	 * Input : eventlogs generate from @prepare step  
	 * Expected : output file contains 3 records with format 
	 * {"source_name"=>"LOGSTAT", "type"=(>[event_type], "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 *             the event logs store in Application catagory
	 *             the evemt logs has the time generated >= @from_time_generate params
	 * details in \src\test\data_test\testEventlog\expected\testEventlog1.output
	 */
	@Test
	public void testEventlog1() {
		try{
			test_common.cleanData("src/test/resources/data_test/input/testEventlog/output/testEventlog1.output")
			def outFile = ["path":"src/test/resources/data_test/input/testEventlog/output/testEventlog1.output"]
			output_conf.put("config", outFile)	
			input_conf.put("from_time_generated",from_time_generated.toString());
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			//Check if data return is 3 records logs generated from @prepare step
			assertTrue(test_common.countLines("src/test/resources/data_test/input/testEventlog/output/testEventlog1.output") == 3)
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
			test_common.cleanData("src/test/resources/data_test/input/testEventlog/output/testEventlog2.output")
			def outFile = ["path":"src/test/resources/data_test/input/testEventlog/output/testEventlog2.output"]
			output_conf.put("config", outFile)
			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			println "conf " + conf
			svc.runLogStat(conf)
			assertTrue(test_common.countLines("src/test/resources/data_test/input/testEventlog/output/testEventlog1.output") == 3)
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : full parameters ,
	 * the filter was changed to get only eventlogs has the event source is 'LOGSTAT' and type is 'ERROR'
	 * Input : eventlogs generate from @prepare step
	 * Expected : output file contains 2 records with format
	 * {"source_name"=>"LOGSTAT", "type"=>"ERROR", "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 * details in \src\test\data_test\testEventlog\expected\testEventlog3.output
	 */
	@Test
	public void testEventlog3() {
		try{
			test_common.cleanData("src/test/resources/data_test/input/testEventlog/output/testEventlog3.output")
			def outFile = ["path":"src/test/resources/data_test/input/testEventlog/output/testEventlog3.output"]
			output_conf.put("config", outFile)	
			input_conf.put("from_time_generated",from_time_generated.toString());
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
			assertTrue(test_common.countLines("src/test/resources/data_test/input/testEventlog/output/testEventlog3.output") == 2)
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : full parameters with another eventlog file,
	 * Input : an eventlog create in "System" log file 
	 * Expected : output file contains 1 records with format
	 * {"source_name"=>"LOGSTAT", "type"=>"ERROR", "time"=>[the_time_logs_generated], "message"=>[the_log_message]}
	 * the output log get from customize event-log file created
	 * details in \src\test\data_test\testEventlog\expected\testEventlog4.output
	 */
	@Test
	public void testEventlog4() {
		try{
			test_common.cleanData("src/test/resources/data_test/input/testEventlog/output/testEventlog4.output")
			def outFile = ["path":"src/test/resources/data_test/input/testEventlog/output/testEventlog4.output"]
			output_conf.put("config", outFile)	
			input_conf.put("from_time_generated",from_time_generated.toString());
			def create_evlog_cmd = 'cmd /c eventcreate /L SYSTEM /SO LOGSTAT2 /ID 3 /T ERROR /D "1-This is a ERROR event in System log file" '
			def proc = create_evlog_cmd.execute();
			proc.waitFor();
			from_time_generated = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
			input_conf.put("event_log_type","System");
			input_conf.put("from_time_generated",from_time_generated.toString());
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
			assertTrue(test_common.countLines("src/test/resources/data_test/input/testEventlog/output/testEventlog4.output") >= 1)
		} catch(Exception ex){
			println ex
		}
	}
}

