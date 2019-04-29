import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atilika.kuromoji.Token;
import com.atilika.kuromoji.Tokenizer;
import com.atilika.kuromoji.Tokenizer.Mode;

public class Main {

	
	private static Tokenizer tokenizer;
	static {
		Mode mode = Mode.valueOf("NORMAL");
		// 使う辞書の指定
		 String userDic = "/home/kkajiyama/Downloads/kuromoji/user_dic/wikipedia.csv";
		try {
			tokenizer = Tokenizer.builder().mode(mode).userDictionary(userDic).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static int N;
	
	// arg[0] = challenger summaryDir, args[1] = justice summary, args[2] = outputResultCSVfileName, args[3] = N
	// N - gram
	public static void main(String[] args) {
		
		if (args.length != 4) {
			System.out.println(" arg[0] = challenger summaryDir, args[1] = justice summary, args[2] = outputResultCSVfileName, args[3] = N ");
			System.out.println("System end.");
			return;
		}
		
		N = Integer.valueOf(args[3]);
		
		File challenger = new File(args[0]);
		if (!challenger.exists() || !challenger.isDirectory()) {
			System.out.println("Challenger path fault！");
			System.out.println(args[0]);
			return;
		}
		File justice = new File(args[1]);
		
		if (!justice.exists() || !justice.isDirectory()) {
			System.out.println("Jusutice path fault！");
			System.out.println(args[1]);
			return;
		}
		File[] chanllengerFiles = challenger.listFiles();
		File[] justiceFiles = justice.listFiles();
		
		
		Map<String, File> justiceFile = new HashMap<>();
		
		for (File currentFile: justiceFiles) {
			if (currentFile.getName().contains("Krt")) {
				continue;
			}
			System.out.println("current file is " + currentFile.getName() + ";");
			justiceFile.put(currentFile.getName().split("Smr.")[0], currentFile);
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[2]), StandardCharsets.UTF_8)) {
			writer.write("fileName");
			writer.write(",");
			writer.write("recall");
			writer.write(",");
			writer.write("precision");
			writer.write(",");
			writer.write("F1");
			writer.write(",");
			writer.write("original_wordSum");
			writer.write(",");
			writer.write("ours_wordSum");
			writer.newLine();

			String fileName;
			for (File challengFile: chanllengerFiles) {
				fileName = challengFile.getName().split("Krt")[0];
				if (justiceFile.containsKey(fileName)) {

					writer.write(fileName);
					writer.write(",");
					
					if (N == 0) {
						List<String> challengerWords = makeUnigrmList(challengFile);
						List<String> justiceWords = makeUnigrmList(justiceFile.get(fileName));
						evaluate(challengerWords, justiceWords, writer);
					} else {
						List<String> challengerNgramWords = makeNgrmList(challengFile);
						List<String> justiceNgramWords = makeNgrmList(justiceFile.get(fileName));
						evaluate(challengerNgramWords, justiceNgramWords, writer);
					}
					
				}
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
		
		System.out.println("System.complete!");
	}
	
	static private List<String> makeUnigrmList(File readFile) {


		List<String> wordList = new ArrayList<>();

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(readFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

			String readLine = "";

			while ((readLine = reader.readLine()) != null) {
				for(Token tokens:tokenizer.tokenize(readLine)) {

					String word = tokens.getSurfaceForm();

					if (word.equals("")){
						continue;
					}
					if (word.contains(" ") || word.contains("\\s")) {
						word = word.replaceAll(" ", "/space/");
						word = word.replaceAll("\t", "/tag/");
						word = word.replaceAll("\\s", "/space/");
					}

					wordList.add(word);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return wordList;
	}
	
	static private List<String> makeNgrmList(File readFile) {


		List<String> wordList = new ArrayList<>();

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(readFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

			String ngramWord = "";

			List<String> unigramList = makeUnigrmList(readFile);
			
			for (int i = 0; i < unigramList.size() - N; i++) {
				for (int j = 0; j < N; j++) {
					ngramWord = ngramWord.concat(unigramList.get(i + j));
					
				}
				wordList.add(ngramWord);
				ngramWord = "";
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return wordList;
	}
	
	static private void evaluate(List<String> challengerWords, List<String> justiceWords, BufferedWriter writer) {
		double precision, recall, F1;
		int co_occureenceCounter = 0;
		
		for (String word:challengerWords) {
			if (justiceWords.contains(word)) {
				co_occureenceCounter++;
			} 
		}
		
		recall = (double) co_occureenceCounter / justiceWords.size();
		precision = (double) co_occureenceCounter / challengerWords.size();
		
		F1 = 2*recall*precision / (recall + precision) ;
		
		
		try {
			writer.write(String.valueOf(recall));
			writer.write(",");
			writer.write(String.valueOf(precision));
			writer.write(",");
			writer.write(String.valueOf(F1));
			writer.write(",");
			writer.write(String.valueOf(justiceWords.size()));
			writer.write(",");
			writer.write(String.valueOf(challengerWords.size()));
			writer.newLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
