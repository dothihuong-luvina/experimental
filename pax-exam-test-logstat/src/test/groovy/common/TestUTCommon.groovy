package test.groovy.common;
import java.io.File;
public class TestUTCommon {
	public boolean compareData(String file_path1,String file_path2){

		try {
			println file_path1
			println file_path2
			def isEquals = false
			def file1 = new File(file_path1);
			def file2 = new File(file_path2);
			def data1 = file1.getText();
			def data2 = file2.getText();

			if(data1.equals(data2)){
				println "return true"
				return true;
			}else{
				println "return false"
				return false;
			}
		} catch (Exception ex){
			return false
		}
	}
	public boolean cleanData(String file_path){
		try{
			File file = new File(file_path);
			println file_path
			println file.exists();
			if(file.delete()){
				System.out.println(file.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public int countLines(String file_path){
		int count = 0 
		File file = new File(file_path);
		file.eachLine {
			if(it != null && it.trim() != ""){
				count++;
			}
		}
		return count
	}
}