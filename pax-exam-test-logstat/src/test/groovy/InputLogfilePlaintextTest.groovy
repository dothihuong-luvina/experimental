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
 * Testcase for process input from plaintext file 
 * @author nguyenxuanluong
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class InputLogfilePlaintextTest {
	public InputLogfilePlaintextTest() {
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
		currentDir = System.getProperty("user.dir");
		logs_test_dir = currentDir + "/src/test/resources/data_test/testPlainText";
		svc = context.getService(context.getServiceReference(LogStat.class.getName()));
		output_conf.put("type", "file");


		filter_conf = [
			"filter_type" : "match_field",
			"filter_conf" : [
				"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
				"time" : "[0-9]{2}:[0-9]{2}:[0-9]{2}",
				"message" : '^.*$'
			]
		]
		input_conf.put("input_type", "file");
	}

	@After
	public void finish() {
	}

	/**
	 * Test for case : full parameters with 'asc_by_fname' = true (logs file in folder sort by ASC)
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 17 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByLine0.output"
	 */
	@Test
	public void testPlainTextByLine0() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "line");
			input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("start_pos", 3);
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine0.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine0.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine0.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine0.output"))
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : missing 'monitor_type' parameter
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 17 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByLine1.output"
	 */
	@Test
	public void testPlainTextByLine1() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			//input_conf.put("monitor_type", "line");
			input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("start_pos", 3);
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine1.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine1.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine1.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine1.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : missing 'monitor_type','start_file_name' parameters
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 17 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByLine2.output"
	 */
	@Test
	public void testPlainTextByLine2() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			//input_conf.put("monitor_type", "line");
			//input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("start_pos", 3);
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine2.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine2.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine2.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine2.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : missing 'monitor_type','start_file_name' ,'start_pos' parameters
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 30 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByLine3.output"
	 */
	@Test
	public void testPlainTextByLine3() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			//input_conf.put("monitor_type", "line");
			//input_conf.put("start_file_name", "logfile2.log");
			//input_conf.put("start_pos", 3);
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine3.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine3.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine3.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine3.output"))
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : missing 'monitor_type','start_file_name' ,'start_pos', 'asc_by_fname' parameters
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : A error message println out:  "[Logstat]  : 'start_file_name' parameter must be required !"
	 * No output generated 
	 */
	@Test
	public void testPlainTextByLine4() {
		try{
			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			//input_conf.put("monitor_type", "line");
			//input_conf.put("start_file_name", "logfile2.log");
			//input_conf.put("start_pos", 3);
			//input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine4.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine4.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine4.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine4.output"))
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : full parameters with 'asc_by_fname' = false (logs file in folder sort by DESC)
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 14 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByLine5.output"
	 */
	@Test
	public void testPlainTextByLine5() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "line");
			input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("start_pos", 3);
			input_conf.put("asc_by_fname", false);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByLine5.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByLine5.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByLine5.output","src/test/resources/data_test/testPlainText/output/testPlainTextByLine5.output"))
		} catch(Exception ex){
			println ex
		}
	}

	//-----------------Test input log from plaintext by date-----------------------------------

	/**
	 * Test for case : full parameters with 'asc_by_fname' = true (logs file in folder sort by ASC)
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 16 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * The date in each record is >= 2014-02-05
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByDate0.output"
	 */
	@Test
	public void testPlainTextByDate0() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "date");
			input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("from_date", "2014-02-05");
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByDate0.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByDate0.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByDate0.output","src/test/resources/data_test/testPlainText/output/testPlainTextByDate0.output"))
		} catch(Exception ex){
			println ex
		}
	}
	//-----------------Test input log from plaintext by date-----------------------------------

	/**
	 * Test for case : full parameters with 'asc_by_fname' = false (logs file in folder sort by ASC)
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 16 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * The date in each record is <= 2014-02-05
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByDate1.output"
	 */
	@Test
	public void testPlainTextByDate1() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "date");
			input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("from_date", "2014-02-05");
			input_conf.put("asc_by_fname", false);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByDate1.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByDate1.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByDate1.output","src/test/resources/data_test/testPlainText/output/testPlainTextByDate1.output"))
		} catch(Exception ex){
			println ex
		}
	}

	/**
	 * Test for case : missing parameter 'start_file_name'
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 16 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * The date in each record is >= 2014-02-05
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByDate2.output"
	 */
	@Test
	public void testPlainTextByDate2() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "date");
			//input_conf.put("start_file_name", "logfile2.log");
			input_conf.put("from_date", "2014-02-05");
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByDate2.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByDate2.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByDate2.output","src/test/resources/data_test/testPlainText/output/testPlainTextByDate2.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : missing parameter 'start_file_name' & 'from_date'
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : output file contains 30 records with format
	 * {"date"=>[the_log_date], "time"=>[the_log_time], "message"=>[full_log_message]}
	 * Detail in "src/test/resources/data_test/testPlainText/expected/testPlainTextByDate3.output"
	 */
	@Test
	public void testPlainTextByDate3() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "date");
			//input_conf.put("start_file_name", "logfile2.log");
			//input_conf.put("from_date", "2014-02-05");
			input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByDate3.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByDate3.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertTrue(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByDate3.output","src/test/resources/data_test/testPlainText/output/testPlainTextByDate3.output"))
		} catch(Exception ex){
			println ex
		}
	}
	/**
	 * Test for case : missing parameters : 'start_file_name' ,'from_date',asc_by_fname
	 * Input : Log files in src/test/resources/data_test/testPlainText/*.log
	 * Expected : A error message println out:  "[Logstat]  : 'start_file_name' parameter must be required !"
	 * No output generated
	 */
	@Test
	public void testPlainTextByDate4() {
		try{

			input_conf.put("path", logs_test_dir);
			input_conf.put("file_format", "plain_text");
			input_conf.put("monitor_type", "date");
			//input_conf.put("start_file_name", "logfile2.log");
			//input_conf.put("from_date", "2014-02-05");
			//input_conf.put("asc_by_fname", true);
			test_common.cleanData("src/test/resources/data_test/testPlainText/output/testPlainTextByDate4.output")
			output_conf.put("destination", "src/test/resources/data_test/testPlainText/output/testPlainTextByDate4.output");

			conf.put("input",input_conf);
			conf.put("filter",filter_conf);
			conf.put("output",output_conf);
			svc.runLogStat(conf)
			assertFalse(test_common.compareData("src/test/resources/data_test/testPlainText/expected/testPlainTextByDate4.output","src/test/resources/data_test/testPlainText/output/testPlainTextByDate4.output"))
		} catch(Exception ex){
			println ex
		}
	}
}

