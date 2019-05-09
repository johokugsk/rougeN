import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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
		 String userDic = "/home/kkajiyama/Downloads/dictionarys/wikipedia_addbyomei.csv";
		try {
			tokenizer = Tokenizer.builder().mode(mode).userDictionary(userDic).build();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	static int N = 1;
	
	// arg[0] = challenger summaryDir, args[1] = humanWrite summary, args[2] = outputResultCSVfileName, args[3] = N
	// N - gram
	public static void main(String[] args) {
		
		if (args.length != 4) {
			System.out.println(" arg[0] = challenger summaryDir, args[1] = humanWrite summary, args[2] = outputResultCSVfileName, args[3] = N ");
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
		File humanWrite = new File(args[1]);
		
		if (!humanWrite.exists() || !humanWrite.isDirectory()) {
			System.out.println("Jusutice path fault！");
			System.out.println(args[1]);
			return;
		}
		File[] chanllengerFiles = challenger.listFiles();
		File[] humanWrittenFiles = humanWrite.listFiles();
		
		
		Map<String, File> humanWrittenFile = new HashMap<>();
		
		for (File currentFile: humanWrittenFiles) {
			if (currentFile.getName().contains("Krt")) {
				continue;
			}
			System.out.println("current file is " + currentFile.getName() + ";");
			humanWrittenFile.put(currentFile.getName().split("Smr.")[0], currentFile);
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
			writer.write(",");
			writer.write("humanWriteSumSentenceLength");
			writer.write(",");
			writer.write("challengerSumSentenceLength");
			writer.write(",");
			writer.write("sentenceDuplicate");
			writer.write(",");
			writer.write("overlapped&GobiListA");
			writer.write(",");
			writer.write("containGobiListA&humanWritten");
			writer.write(",");
			writer.write("containGobiListA&challenger");
			writer.write(",");
			writer.write("overlapped&GobiListB");
			writer.write(",");
			writer.write("containGobiListB&humanWritten");
			writer.write(",");
			writer.write("containGobiListB&challenger");

			// specificWordCountも載せる
			
			writer.newLine();

			String fileName;
			for (File challengFile: chanllengerFiles) {
				fileName = challengFile.getName().split("Krt")[0];
				if (humanWrittenFile.containsKey(fileName)) {

					writer.write(fileName);
					writer.write(",");
					
					if (N == 1) {
						// word list
						Collection<String> challengerWords = makeUnigrmCollection(challengFile);
						Collection<String> humanWrittenWords = makeUnigrmCollection(humanWrittenFile.get(fileName));
						// sentence list
						Collection<String> challengeSentences = makeSentenceList(challengFile);
						Collection<String> humanWrittenSentences = makeSentenceList(humanWrittenFile.get(fileName));

						evaluate(challengerWords, humanWrittenWords, challengeSentences, humanWrittenSentences, writer);
					} else {
						// word list
						Collection<String> challengerNgramWords = makeNgrmList(challengFile);
						Collection<String> humanWrittenNgramWords = makeNgrmList(humanWrittenFile.get(fileName));
						// sentence list
						Collection<String> challengeSentences = makeSentenceList(challengFile);
						Collection<String> humanWrittenSentences = makeSentenceList(humanWrittenFile.get(fileName));

						evaluate(challengerNgramWords, humanWrittenNgramWords, challengeSentences, humanWrittenSentences,  writer);
					}
					

					
				}
			}
		} catch (IOException ioEx) {
			// TODO: handle exception
			ioEx.printStackTrace();
		}
		
		System.out.println("System.complete!");
	}
	
	static private Collection<String> makeUnigrmCollection(File readFile) {


		Collection<String> wordColleciton = new ArrayList<String>();

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

					wordColleciton.add(word);
				}

			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return wordColleciton;
	}
	
	static private List<String> makeUnigrmList(File readFile) {


		List<String> wordColleciton = new ArrayList<String>();

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

					wordColleciton.add(word);
				}

			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return wordColleciton;
	}
	
	
	static private Collection<String> makeNgrmList(File readFile) {


		Collection<String> wordList = new ArrayList<String>();

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
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return wordList;
	}

	static private Collection<String> makeSentenceList(File readFile) {
		Collection<String> sentenceList = new ArrayList<>();
		int sentencceCounter = 0;

		try(BufferedReader reader = Files.newBufferedReader(Paths.get(readFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

			String readLine = "";
			
			while ((readLine = reader.readLine()) != null) {
				
				// ここに文の数をカウントする処理
				String[] sentences = readLine.split("。");

				for (String sentence: sentences) {
					sentenceList.add(sentence);
				}

			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return sentenceList;

	} 
	
	static private void evaluate(Collection<String> challengerWords, Collection<String> humanWrittenWords, Collection<String> challengerSentences, Collection<String> humanWrittenSentences, BufferedWriter writer) {
		double precision, recall, F1;
		int co_occureenceCounter = 0;
		int humanWrittenWordSize = humanWrittenWords.size();

		
		for (String word:challengerWords) {
			if (humanWrittenWords.contains(word)) {
				humanWrittenWords.remove(word);
				co_occureenceCounter++;
			} 
		}

		// sentence
		int sentencesDuplicate = 0;

		Collection<String> specificWords_A = readCSV("/home/kkajiyama/Downloads/dictionarys/AuxilaryVerbs.csv");
		Collection<String> specificWords_B = readCSV("/home/kkajiyama/Downloads/dictionarys/forExtractSummarize.csv");

		int containSpecifficWord_A_humanWritten  = 0;
		int containSpecifficWord_A_challenger  = 0;
		int containSpecifficWord_A_overlap  = 0;

		int containSpecifficWord_B_humanWritten  = 0;
		int containSpecifficWord_B_challenger  = 0;
		int containSpecifficWord_B_overlap  = 0;

		List<Integer> wordListAResult = sentenceStatistics(challengerSentences, humanWrittenSentences, specificWords_A);
		
		sentencesDuplicate = wordListAResult.get(0);
		containSpecifficWord_A_overlap = wordListAResult.get(1);
		containSpecifficWord_A_humanWritten = wordListAResult.get(2);
		containSpecifficWord_A_challenger = wordListAResult.get(3);
		
		List<Integer> wordListBResult = sentenceStatistics(challengerSentences, humanWrittenSentences, specificWords_B);
		
		containSpecifficWord_B_overlap = wordListBResult.get(1);
		containSpecifficWord_B_humanWritten = wordListBResult.get(2);
		containSpecifficWord_B_challenger = wordListBResult.get(3);
	
		if (co_occureenceCounter == 0) {
			precision = 0.00000;
			recall = 0.00000;
			F1 = 0.00000;
		} else {
			recall = (double) co_occureenceCounter / humanWrittenWordSize;
			precision = (double) co_occureenceCounter / challengerWords.size();
			
			
			if (1 < recall || 1 < precision) {
				System.out.println("coOccur = " + co_occureenceCounter );
				System.out.println("humanWrittenWords.size() = " + humanWrittenWordSize);
				System.out.println("challengerWords.size() = " + challengerWords.size() );
			}
			
			F1 = 2*recall*precision / (recall + precision) ;
			
			
			
		}


		
		try {
			writer.write(String.valueOf(recall));
			writer.write(",");
			writer.write(String.valueOf(precision));
			writer.write(",");
			writer.write(String.valueOf(F1));
			writer.write(",");
			writer.write(String.valueOf(humanWrittenWordSize));
			writer.write(",");
			writer.write(String.valueOf(challengerWords.size()));
			writer.write(",");
			writer.write(String.valueOf(humanWrittenSentences.size()));
			writer.write(",");
			writer.write(String.valueOf(challengerSentences.size()));
			writer.write(",");
			writer.write(String.valueOf(sentencesDuplicate));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_A_overlap));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_A_humanWritten));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_A_challenger));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_B_overlap));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_B_humanWritten));
			writer.write(",");
			writer.write(String.valueOf(containSpecifficWord_B_challenger));
			

			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
	}

	private static List<String> readCSV(String csvFilenName) {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilenName), StandardCharsets.UTF_8)) {
			String readLine = "";
			List<String> wordList = new ArrayList<>();

			while ((readLine = reader.readLine()) != null) {
				wordList.add(readLine.split(" ")[0]);
			}
			return wordList;
		} catch (IOException e) {
			//TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param challengerSentences
	 * @param humanWrittenSentences
	 * @param specificWords
	 * @return [0]:sentenceDuplicate, [1]:containWordList&duplicateSentenece, [2]:containWordListInHumanWritten, [3]:containWordListInChallenger
	 */
	private static List<Integer> sentenceStatistics(Collection<String> challengerSentences, Collection<String> humanWrittenSentences, Collection<String> specificWords) {
		
		List<Integer> returnValues = new ArrayList<>();
		int dupliate = 0, containDuplicate = 0, containInChallenger = 0, containInHumanWritten = 0;
		
		for (String humanWrittenSentence : humanWrittenSentences) {
			for (String wordA : specificWords) { 
				if (humanWrittenSentence.contains(wordA)) {
					containInHumanWritten++;
					break;
				}

			}
		}

		for (String challengerSentence : challengerSentences) {
			for (String wordA : specificWords) { 
				if (challengerSentence.contains(wordA)) {
					containInChallenger++;
					break;
				}

			}
		}
		
		for (String sentence: challengerSentences) {
			if (humanWrittenSentences.contains(sentence)) {
				dupliate++;
				humanWrittenSentences.remove(sentence);
				for (String wordA : specificWords) {
					if (sentence.contains(wordA)) {
						containDuplicate++;
						break;
					}
				}
			}
		}
		
		returnValues.add(dupliate);
		returnValues.add(containDuplicate);
		returnValues.add(containInHumanWritten);
		returnValues.add(containInChallenger);

		
		return returnValues;
		
	}
}
