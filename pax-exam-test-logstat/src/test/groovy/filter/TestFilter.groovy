package test.groovy.filter;
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
import test.groovy.common.TestUTCommon;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestFilter {
    public TestFilter() {}

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
	String logs_test_dir;
	TestUTCommon test_common = new TestUTCommon();	
	@Before
	public void prepare() {
		input_conf = new HashMap<String, Object>();
		output_conf = new HashMap<String, Object>();
		filter = new HashMap<String, Object>();
		conf = new HashMap<String, Object>();
		result = "";
		expected = "";
		logs_test_dir = wd + "/src/test/resources/data_test/filter/testFilter";
		// filter data of log
		filter = [
			"filter_type" : "match_field",
			"filter_conf" : [
				"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
				"time" : "[0-9]{2}:[0-9]{2}:[0-9]{2}",
				"message" : '^.*$'
			]
		]
		// get data of plaintext
		input_conf.put("input_type", "file");
		input_conf.put("path", logs_test_dir);
		input_conf.put("file_format", "plain_text");
		input_conf.put("monitor_type", "line");
		input_conf.put("start_pos", 3);
		//input_conf.put("asc_by_fname", true);
		// set output config
		output_conf.put("type", "file");		
		try {
			svc = context.getService(context.getServiceReference(LogStat.class.getName()));
		} catch (Exception e) {
			e.printStackTrace()
		}
	}

	@After
	public void finish() {
		//new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_01.log").delete();
	}
	
	//===========================Check data input is String===============================
	/**
	 * Check func with all of variable: typeRegx, mapFilterConf and list_logs is true.
	 * Value of typeRegx is 'match_field'.
	 * Record log mapping with mapFilterConf.
	 * Expected: return all of data corresponding to input data.  
	 */
   @Test
    public void testFilter_01() {
		input_conf.put("start_file_name", "result_testString_01.log");
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_01.output")		
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_01.log"]
		output_conf.put("config", outFile)
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_01.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testString_01.log")).text
    }
	
	/**
	 * Check func with value of typeRegx is 'match_field'. 
	 * One or more record does not mapping with all of variable into mapFilterConf.
	 * Expected: only return records are mapped to mapFilterConf.
	 */
	@Test
	public void testFilter_02() {
		input_conf.put("start_file_name", "result_testString_02.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_02.log"]
		output_conf.put("config", outFile)
		// filter data of log
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_02.output")
		
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_field",
			"filter_conf" : [
				"date": "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [0-9]{2} \\d{2}:\\d{2}:\\d{2}",
				"message": "(vmRHEL55x64).*"
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_02.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testString_02.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record'.
	 * Value of mapFilterConf['format_log'] and mapFilterConf['data'] is mapped to record log.
	 * Expected: return all of data corresponding to input data.  
	 */
	@Test
	public void testFilter_03() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_03.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_03.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_03.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_03.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testString_03.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record'.
	 * One or more record does not mapping with mapFilterConf['format_log'] and mapFilterConf['data'].
	 * Expected: only return records are mapped to mapFilterConf.
	 */
	@Test
	public void testFilter_04() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_04.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_04.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_04.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testString_04.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record' and mapFilterConf['format_log'] is empty.
	 * Expected: data cannot return => not create file output.log
	 */
	@Test
	public void testFilter_05() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_05.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_05.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : "",
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_05.log")).exists())
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record' and mapFilterConf['format_log'] is null.
	 * Expected: data cannot return => not create file output.log
	 */
	@Test
	public void testFilter_06() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_06.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_06.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : null,
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_06.log")).exists())
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record' and mapFilterConf['data'] is empty.
	 * Expected: data cannot return => not create file output.log
	 */
   @Test
	public void testFilter_07() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_07.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_07.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : []
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_07.log")).exists())
	}
	
	/**
	 * Check func with value of typeRegx is 'match_log_record' and mapFilterConf['data'] is null.
	 * Expected: data cannot return => not create file output.log
	 */
	@Test
	public void testFilter_08() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_08.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_08.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : null
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_08.log")).exists())
	}
	
	/**
	 * Check func with value of typeRegx is empty.
	 * Expected: return data is empty => not create file output.log
	 */
	@Test
	public void testFilter_09() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_09.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_09.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_09.log")).exists())
	}
	
	/**
	 * Check func with value of typeRegx is null.
	 * Expected: return data is empty => not create file output.log
	 */
	@Test
	public void testFilter_10() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_10.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_10.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : null,
			"filter_conf" : [
				"format_log" : "Toi la (\\w*), toi (\\d{2}) tuoi, que((\\s(\\w*))*)",
				"data" : [
					1 : "name",
					2 : "age",
					3 : "state"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_10.log")).exists())
	}
	
	/**
	 * Check func with value of mapFilterConf is empty.
	 * Expected: return data is empty => not create file output.log
	 */
	@Test
	public void testFilter_11() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_11.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_11.output")		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : []
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_11.log")).exists())
	}
	
	/**
	 * Check func with value of mapFilterConf is null.
	 * Expected: return data is empty => not create file output.log
	 */
	@Test
	public void testFilter_12() {
		input_conf.put("start_pos", 1);
		input_conf.put("start_file_name", "result_testString_04.log");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_12.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_12.output")
		
		// filter data of log
		filter = new HashMap<String, Object>();
		filter = [
			"filter_type" : "match_log_record",
			"filter_conf" : null
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_12.log")).exists())
	}
	
	//===========================Check data input is Map===================================
	/**
	 * Check func with all of variable: mapFilterConf and list_logs is true.
	 * Value of mapFilterConf contains: 'data_field' and 'filter' corresponding to record of log.
	 * Expected: return all of data corresponding to input data. 
	 */
	@Test
	public void testFilter_13() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_01.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_13.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_13.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [
					"date",
					"time",
					"message"
				],
				"filter" : [
					"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
					"time" : "[0-9]{2}:[0-9]{2}:[0-9]{2}",
					"message" : '^.*$'
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_13.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_01.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf does not contain 'data_field'.
	 * Value of mapFilterConf['filter'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'filter'.
	 */
	@Test
	public void testFilter_14() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_02.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_14.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_14.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"filter" : [
					"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
					"message" : '^.*$'
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_14.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_02.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf['data_field'] is list empty.
	 * Value of mapFilterConf['filter'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'filter'.
	 */
	@Test
	public void testFilter_15() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_02.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_15.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_15.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [],
				"filter" : [
					"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
					"message" : '^.*$'
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_15.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_02.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf['data_field'] is null.
	 * Value of mapFilterConf['filter'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'filter'.
	 */
	@Test
	public void testFilter_16() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_02.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_16.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_16.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : null,
				"filter" : [
					"date" : "^[0-9]{4}-[0-9]{2}-[0-9]{2}",
					"message" : '^.*$'
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_16.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_02.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf does not contain 'filter'.
	 * Value of mapFilterConf['data_field'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'data_field'.
	 */
	@Test
	public void testFilter_17() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_03.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_17.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_17.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [
					"date",
					"time",
					"message"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_17.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_03.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf['filter'] has no corresponding format to record of log.
	 * Expected: data cannot return => not create file output.log
	 */
	@Test
	public void testFilter_18() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_03.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_18.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_18.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [
					"date",
					"time",
					"message"
				],
				"filter" : [
					"date" : "(2014-01-01)",
					"time" : "(28:08:08)",
					"message" : "[0-9]{2}:[0-9]{2}:[0-9]{2}"
				]
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		assertFalse((new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_18.log")).exists())
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf['filter'] is map empty.
	 * Value of mapFilterConf['data_field'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'data_field'.
	 */
	@Test
	public void testFilter_19() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_03.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_19.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_19.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [
					"date",
					"time",
					"message"
				],
				"filter" : []
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_19.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_03.log")).text
		assertEquals(expected,result);
	}
	
	/**
	 * Check func with value of list_logs is true.
	 * Value of mapFilterConf['filter'] is null.
	 * Value of mapFilterConf['data_field'] corresponding to record of log.
	 * Expected: return all of data corresponding to 'data_field'.
	 */
	@Test
	public void testFilter_20() {
		input_conf.put("file_format", "csv");
		input_conf.put("start_file_name", "result_testMap_03.csv");
		def outFile = ["path": wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_20.log"]
		output_conf.put("config", outFile)
		test_common.cleanData("src/test/resources/data_test/input/testFilter/output/testFilter_20.output")
		
		// filter data of log
		filter = [
			"filter_type" : "",
			"filter_conf" : [
				"data_field" : [
					"date",
					"time",
					"message"
				],
				"filter" : null
			]
		]
		conf.put("input",input_conf);
		conf.put("filter",filter);
		conf.put("output",output_conf);
		svc.runLogStat(conf)
		// get output data of func
		result = (new File(wd + "/src/test/resources/data_test/filter/testFilter/output/testFilter_20.log")).text
		// get data expected to comparse
		expected = (new File(wd + "/src/test/resources/data_test/filter/testFilter/expected/expected_testMap_03.log")).text
		assertEquals(expected,result);
	}
}