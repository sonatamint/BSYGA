import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileMani {
	
	
	static String readFile(String path) throws Exception{
		File inputfile = new File(path);
		FileReader fin = new FileReader(inputfile);
		BufferedReader br = new BufferedReader(fin);
		String content ="";
		String templine = null;
		while((templine = br.readLine()) != null){
			content += templine.substring(templine.indexOf(":")+1) + "\r\n";
		}
		br.close();
		fin.close();
		return content;
	}
	
	static void writeFile(String pathto, String content) throws Exception {
		File fl = new File(pathto);
		File dir = fl.getParentFile();
		if(!dir.exists()){
			dir.mkdirs();
		}
		FileWriter fout = new FileWriter(fl);
		BufferedWriter bwr = new BufferedWriter(fout);
		bwr.write(content);
		bwr.close();
		fout.close();
	}
}
